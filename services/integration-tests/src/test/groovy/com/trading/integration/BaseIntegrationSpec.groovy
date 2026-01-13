package com.trading.integration

import groovy.sql.Sql
import io.restassured.RestAssured
import io.restassured.http.ContentType
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName
import spock.lang.Shared
import spock.lang.Specification

import java.time.Duration

import static io.restassured.RestAssured.given
import static org.awaitility.Awaitility.await
import static java.util.concurrent.TimeUnit.SECONDS

abstract class BaseIntegrationSpec extends Specification {

    @Shared
    static PostgreSQLContainer postgres

    @Shared
    static KafkaContainer kafka

    @Shared
    static Sql sql

    // Service base URLs (update if services run on different ports in tests)
    static final String API_GATEWAY_URL = "http://localhost:8080"
    static final String USER_SIGNUP_URL = "http://localhost:8084"
    static final String USER_SERVICE_URL = "http://localhost:8085"
    static final String WALLET_SERVICE_URL = "http://localhost:8086"
    static final String TRADING_SERVICE_URL = "http://localhost:8087"
    static final String PORTFOLIO_SERVICE_URL = "http://localhost:8088"

    def setupSpec() {
        // Note: For now, tests assume services are running via docker-compose
        // Future: Start services with TestContainers

        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails()
    }

    def setup() {
        // Clean database between tests
        cleanDatabase()
    }

    def cleanDatabase() {
        // Connect to local PostgreSQL (update connection if needed)
        def dbUrl = "jdbc:postgresql://localhost:5432/trading"
        def dbUser = "trading"
        def dbPassword = "trading"

        sql = Sql.newInstance(dbUrl, dbUser, dbPassword, "org.postgresql.Driver")

        // Truncate all tables in all schemas
        sql.execute("TRUNCATE TABLE user_service.users CASCADE")
        sql.execute("TRUNCATE TABLE wallet_service.wallet_balances CASCADE")
        sql.execute("TRUNCATE TABLE trading_service.trades CASCADE")
        sql.execute("TRUNCATE TABLE portfolio_service.holdings CASCADE")
        sql.execute("TRUNCATE TABLE transaction_history_service.transactions CASCADE")

        sql.close()
    }

    // Helper: Create test user
    Map createTestUser(String email = null, String username = null) {
        def testEmail = email ?: "test-${UUID.randomUUID().toString()}@example.com".toString()
        def testUsername = username ?: "user${UUID.randomUUID().toString().take(8)}".toString()

        def response = given()
            .contentType(ContentType.JSON)
            .body([
                email: testEmail,
                username: testUsername,
                phoneNumber: "+1234567890"
            ])
            .post("${API_GATEWAY_URL}/api/v1/signup")
            .then()
            .statusCode(201)
            .extract()
            .response()

        // Wait for Kafka event processing
        Thread.sleep(5000)

        return [
            userId: response.path("userId") as String,
            email: testEmail,
            username: testUsername
        ]
    }

    // Helper: Deposit to wallet
    Map depositToWallet(String userId, String currency, BigDecimal amount) {
        def response = given()
            .contentType(ContentType.JSON)
            .body([currency: currency, amount: amount])
            .post("${WALLET_SERVICE_URL}/api/v1/wallets/${userId}/deposit")
            .then()
            .statusCode(200)
            .extract()
            .response()

        return response.body().as(Map)
    }

    // Helper: Get wallet balance
    BigDecimal getWalletBalance(String userId, String currency) {
        def response = given()
            .get("${WALLET_SERVICE_URL}/api/v1/wallets/${userId}/balances/${currency}")
            .then()
            .statusCode(200)
            .extract()
            .response()

        return response.path("balance") as BigDecimal
    }

    // Helper: Buy shares
    Map buyShares(String userId, String symbol, BigDecimal amount, String orderType = "BY_AMOUNT") {
        def body = orderType == "BY_AMOUNT" ?
            [symbol: symbol, amount: amount] :
            [symbol: symbol, quantity: amount]

        def response = given()
            .contentType(ContentType.JSON)
            .body(body)
            .post("${TRADING_SERVICE_URL}/api/v1/trades/${userId}/buy/${orderType.toLowerCase()}")
            .then()
            .extract()
            .response()

        return response.body().as(Map)
    }

    // Helper: Query database
    List<Map> queryDatabase(String query) {
        def dbUrl = "jdbc:postgresql://localhost:5432/trading"
        def dbUser = "trading"
        def dbPassword = "trading"

        sql = Sql.newInstance(dbUrl, dbUser, dbPassword, "org.postgresql.Driver")
        def rows = sql.rows(query)
        sql.close()

        return rows
    }
}
