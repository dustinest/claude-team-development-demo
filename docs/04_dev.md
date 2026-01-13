# 04_dev.md - Developer Implementation Guide for Step 04

**Role:** Developer
**Phase:** Step 04 - Implement Spock Integration Tests
**From:** Senior Engineer
**Priority:** HIGH

---

## Overview

Implement comprehensive Spock integration tests per Senior Engineer's strategy in `docs/04_se.md`.

**Goal:** 75%+ test coverage, all tests passing, < 10 minute execution time.

---

## Step 1: Create Integration Tests Subproject

### 1.1 Create Directory Structure

```bash
mkdir -p services/integration-tests/src/test/groovy/com/trading/integration/{user,wallet,trading,portfolio,flows,infrastructure}
mkdir -p services/integration-tests/src/test/resources
```

### 1.2 Create build.gradle

**File:** `services/integration-tests/build.gradle`

```gradle
plugins {
    id 'groovy'
    id 'java'
}

dependencies {
    // Spock Framework
    testImplementation 'org.spockframework:spock-core:2.3-groovy-4.0'
    testImplementation 'org.codehaus.groovy:groovy-all:3.0.19'

    // REST-assured for API testing
    testImplementation 'io.rest-assured:rest-assured:5.3.2'
    testImplementation 'io.rest-assured:json-path:5.3.2'

    // TestContainers
    testImplementation 'org.testcontainers:testcontainers:1.19.3'
    testImplementation 'org.testcontainers:postgresql:1.19.3'
    testImplementation 'org.testcontainers:kafka:1.19.3'
    testImplementation 'org.testcontainers:spock:1.19.3'

    // PostgreSQL Driver
    testImplementation 'org.postgresql:postgresql:42.7.1'

    // Kafka Client
    testImplementation 'org.apache.kafka:kafka-clients:3.6.1'

    // Awaitility for async operations
    testImplementation 'org.awaitility:awaitility:4.2.0'

    // JSON processing
    testImplementation 'com.fasterxml.jackson.core:jackson-databind:2.16.0'
}

test {
    useJUnitPlatform()
    maxParallelForks = Runtime.runtime.availableProcessors().intdiv(2) ?: 1
    testLogging {
        events "passed", "skipped", "failed"
        exceptionFormat "full"
    }
}
```

### 1.3 Update settings.gradle

Add to root `settings.gradle`:
```gradle
include 'services:integration-tests'
```

---

## Step 2: Implement Base Test Specification

**File:** `services/integration-tests/src/test/groovy/com/trading/integration/BaseIntegrationSpec.groovy`

```groovy
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
        def testEmail = email ?: "test-${UUID.randomUUID()}@example.com"
        def testUsername = username ?: "user${UUID.randomUUID().toString().take(8)}"

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
        Thread.sleep(2000)

        return [
            userId: response.path("userId"),
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
```

---

## Step 3: Implement Test Specifications

### 3.1 Suite 1: User Registration (CRITICAL)

**File:** `services/integration-tests/src/test/groovy/com/trading/integration/user/UserRegistrationSpec.groovy`

```groovy
package com.trading.integration.user

import com.trading.integration.BaseIntegrationSpec
import io.restassured.http.ContentType

import static io.restassured.RestAssured.given

class UserRegistrationSpec extends BaseIntegrationSpec {

    def "successful user registration creates user in database and publishes Kafka event"() {
        given: "a new user registration request"
        def email = "test-${UUID.randomUUID()}@example.com"
        def username = "testuser"
        def phoneNumber = "+1234567890"

        when: "user signs up via API"
        def response = given()
            .contentType(ContentType.JSON)
            .body([
                email: email,
                username: username,
                phoneNumber: phoneNumber
            ])
            .post("${API_GATEWAY_URL}/api/v1/signup")
            .then()
            .statusCode(201)
            .extract()
            .response()

        and: "we wait for Kafka event processing"
        Thread.sleep(2000)

        then: "user is created with valid UUID"
        def userId = response.path("userId")
        userId != null
        UUID.fromString(userId)

        and: "user exists in user_service schema"
        def dbUser = queryDatabase("SELECT * FROM user_service.users WHERE id = '${userId}'")
        dbUser.size() == 1
        dbUser[0].email == email
        dbUser[0].username == username
    }

    def "duplicate email registration is rejected"() {
        given: "an existing user"
        def email = "duplicate-${UUID.randomUUID()}@example.com"
        createTestUser(email, "user1")

        when: "attempting to register with same email"
        def response = given()
            .contentType(ContentType.JSON)
            .body([
                email: email,
                username: "user2",
                phoneNumber: "+1234567890"
            ])
            .post("${API_GATEWAY_URL}/api/v1/signup")
            .then()
            .extract()
            .response()

        then: "registration is rejected"
        response.statusCode() in [400, 409]
    }

    def "invalid email format is rejected"() {
        when: "attempting to register with invalid email"
        def response = given()
            .contentType(ContentType.JSON)
            .body([
                email: "not-an-email",
                username: "testuser",
                phoneNumber: "+1234567890"
            ])
            .post("${API_GATEWAY_URL}/api/v1/signup")
            .then()
            .extract()
            .response()

        then: "registration is rejected with 400"
        response.statusCode() == 400
    }
}
```

### 3.2 Suite 2: Wallet Operations (CRITICAL)

**File:** `services/integration-tests/src/test/groovy/com/trading/integration/wallet/WalletDepositSpec.groovy`

```groovy
package com.trading.integration.wallet

import com.trading.integration.BaseIntegrationSpec

class WalletDepositSpec extends BaseIntegrationSpec {

    def "deposit creates wallet balance with correct amount"() {
        given: "a registered user"
        def user = createTestUser()

        when: "depositing 1000 USD"
        def result = depositToWallet(user.userId, "USD", 1000.00)

        then: "wallet balance is created"
        result.balance == 1000.00
        result.currency == "USD"

        and: "balance is stored in wallet_service schema"
        def dbBalance = queryDatabase(
            "SELECT balance FROM wallet_service.wallet_balances WHERE user_id = '${user.userId}' AND currency = 'USD'"
        )
        dbBalance.size() == 1
        dbBalance[0].balance == 1000.00
    }

    def "multiple deposits accumulate balance"() {
        given: "a user with existing balance"
        def user = createTestUser()
        depositToWallet(user.userId, "USD", 500.00)

        when: "depositing additional 300 USD"
        depositToWallet(user.userId, "USD", 300.00)

        then: "balance is updated to 800"
        def balance = getWalletBalance(user.userId, "USD")
        balance == 800.00
    }

    def "deposit with negative amount is rejected"() {
        given: "a registered user"
        def user = createTestUser()

        when: "attempting to deposit negative amount"
        def response = given()
            .contentType("application/json")
            .body([currency: "USD", amount: -100.00])
            .post("${WALLET_SERVICE_URL}/api/v1/wallets/${user.userId}/deposit")
            .then()
            .extract()
            .response()

        then: "request is rejected"
        response.statusCode() == 400
    }
}
```

### 3.3 Suite 3: Trading Operations (CRITICAL)

**File:** `services/integration-tests/src/test/groovy/com/trading/integration/trading/BuyOrderSpec.groovy`

```groovy
package com.trading.integration.trading

import com.trading.integration.BaseIntegrationSpec

class BuyOrderSpec extends BaseIntegrationSpec {

    def "buy by amount purchases fractional shares and updates portfolio"() {
        given: "a user with 1000 USD in wallet"
        def user = createTestUser()
        depositToWallet(user.userId, "USD", 1000.00)

        when: "buying AAPL shares with 500 USD"
        def trade = buyShares(user.userId, "AAPL", 500.00, "BY_AMOUNT")

        and: "we wait for portfolio update"
        Thread.sleep(1000)

        then: "trade is executed"
        trade.symbol == "AAPL"
        trade.totalCost > 0

        and: "portfolio holding is created"
        def holdings = queryDatabase(
            "SELECT * FROM portfolio_service.holdings WHERE user_id = '${user.userId}' AND symbol = 'AAPL'"
        )
        holdings.size() == 1
        holdings[0].quantity > 0

        and: "wallet balance is reduced"
        def balance = getWalletBalance(user.userId, "USD")
        balance < 1000.00
    }

    def "buy with insufficient funds is rejected"() {
        given: "a user with only 100 USD"
        def user = createTestUser()
        depositToWallet(user.userId, "USD", 100.00)

        when: "attempting to buy 1000 USD worth of shares"
        def response = buyShares(user.userId, "AAPL", 1000.00, "BY_AMOUNT")

        then: "trade is rejected"
        // Verify error response
        response != null
    }
}
```

---

## Step 4: Implement Remaining Test Suites

Continue implementing the following specs following the patterns above:

1. ✅ `user/UserRegistrationSpec.groovy` (shown above)
2. ✅ `wallet/WalletDepositSpec.groovy` (shown above)
3. **`wallet/WalletWithdrawSpec.groovy`** - withdrawal operations
4. **`wallet/CurrencyExchangeSpec.groovy`** - currency exchange
5. ✅ `trading/BuyOrderSpec.groovy` (shown above)
6. **`trading/SellOrderSpec.groovy`** - selling shares
7. **`trading/FractionalSharesSpec.groovy`** - fractional precision
8. **`portfolio/PortfolioTrackingSpec.groovy`** - portfolio accuracy
9. **`flows/CompleteUserJourneySpec.groovy`** - end-to-end flow
10. **`infrastructure/SchemaIsolationSpec.groovy`** - schema isolation
11. **`infrastructure/KafkaEventFlowSpec.groovy`** - event flow

---

## Step 5: Run Tests

```bash
# Run all integration tests
./gradlew :services:integration-tests:test

# Run specific test class
./gradlew :services:integration-tests:test --tests "*UserRegistrationSpec"

# Run with detailed output
./gradlew :services:integration-tests:test --info

# Generate coverage report (if jacoco configured)
./gradlew :services:integration-tests:jacocoTestReport
```

---

## Definition of Done

- [ ] All 11 test specifications implemented
- [ ] All tests passing (100% success rate)
- [ ] Test coverage ≥ 75% average
- [ ] Tests run in < 10 minutes
- [ ] No failures or flaky tests
- [ ] Code committed and pushed
- [ ] Ready for Q/A validation

---

## Troubleshooting

**Issue: Database connection fails**
- Ensure PostgreSQL running: `docker-compose ps postgres`
- Verify connection: `docker exec -it postgres psql -U trading -d trading`

**Issue: Services not responding**
- Start all services: `docker-compose up -d`
- Check health: `curl http://localhost:8080/q/health`

**Issue: Kafka events not consumed**
- Increase wait time: `Thread.sleep(3000)`
- Check Kafka: `docker-compose logs kafka`

---

**Ready for implementation. Follow the patterns shown above for remaining test specifications.**
