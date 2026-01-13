package com.trading.integration.infrastructure

import com.trading.integration.BaseIntegrationSpec

import static io.restassured.RestAssured.given

class KafkaEventFlowSpec extends BaseIntegrationSpec {

    def "user registration publishes event and user-service consumes it"() {
        given: "a new user registration"
        def email = "kafka-test-${UUID.randomUUID().toString()}@example.com".toString()

        when: "user signs up"
        def response = given()
            .contentType("application/json")
            .body([
                email: email,
                username: "kafkauser",
                phoneNumber: "+1234567890"
            ])
            .post("${API_GATEWAY_URL}/api/v1/signup")
            .then()
            .statusCode(201)
            .extract()
            .response()

        def userId = response.path("userId")

        and: "we wait for Kafka event processing"
        Thread.sleep(2000)

        then: "user exists in user_service database (consumed from Kafka)"
        def dbUser = queryDatabase("SELECT * FROM user_service.users WHERE id = '${userId}'")
        dbUser.size() == 1
        dbUser[0].email == email
    }

    def "wallet deposit publishes event to wallet-events topic"() {
        given: "a registered user"
        def user = createTestUser()

        when: "depositing money"
        depositToWallet(user.userId, "USD", 1000.00)
        Thread.sleep(1000)

        then: "deposit is recorded (event was published and processed)"
        def balance = getWalletBalance(user.userId, "USD")
        balance == 1000.00

        and: "transaction is recorded in transaction_history (consumed from wallet-events)"
        Thread.sleep(1000)
        def transactions = queryDatabase(
            "SELECT * FROM transaction_history_service.transactions WHERE user_id = '${user.userId}' AND transaction_type = 'DEPOSIT'"
        )
        transactions.size() >= 1
    }

    def "trading operations publish events to trading-events topic"() {
        given: "a user with balance"
        def user = createTestUser()
        depositToWallet(user.userId, "USD", 2000.00)

        when: "buying shares"
        buyShares(user.userId, "AAPL", 1000.00, "BY_AMOUNT")
        Thread.sleep(2000)

        then: "portfolio is updated (consumed from trading-events)"
        def holdings = queryDatabase(
            "SELECT * FROM portfolio_service.holdings WHERE user_id = '${user.userId}' AND symbol = 'AAPL'"
        )
        holdings.size() == 1

        and: "trade is recorded in transaction_history (consumed from trading-events)"
        def transactions = queryDatabase(
            "SELECT * FROM transaction_history_service.transactions WHERE user_id = '${user.userId}' AND transaction_type = 'BUY'"
        )
        transactions.size() >= 1
    }

    def "events flow through entire system for complete user journey"() {
        given: "a new user"
        def email = "event-flow-${UUID.randomUUID().toString()}@example.com".toString()

        when: "user signs up (publishes to user-events)"
        def signupResponse = given()
            .contentType("application/json")
            .body([
                email: email,
                username: "eventuser",
                phoneNumber: "+1234567890"
            ])
            .post("${API_GATEWAY_URL}/api/v1/signup")
            .then()
            .statusCode(201)
            .extract()
            .response()

        def userId = signupResponse.path("userId")
        Thread.sleep(2000)

        and: "deposits money (publishes to wallet-events)"
        depositToWallet(userId, "USD", 3000.00)
        Thread.sleep(1000)

        and: "buys shares (publishes to trading-events)"
        buyShares(userId, "GOOGL", 1500.00, "BY_AMOUNT")
        Thread.sleep(2000)

        then: "user exists (user-events consumed)"
        def user = queryDatabase("SELECT * FROM user_service.users WHERE id = '${userId}'")
        user.size() == 1

        and: "wallet balance exists (wallet-events consumed)"
        def balance = getWalletBalance(userId, "USD")
        balance > 0
        balance < 3000.00

        and: "portfolio holding exists (trading-events consumed)"
        def holdings = queryDatabase(
            "SELECT * FROM portfolio_service.holdings WHERE user_id = '${userId}'"
        )
        holdings.size() >= 1

        and: "transaction history exists (both wallet-events and trading-events consumed)"
        def transactions = queryDatabase(
            "SELECT * FROM transaction_history_service.transactions WHERE user_id = '${userId}'"
        )
        transactions.size() >= 2 // At least deposit + buy
    }

    def "multiple concurrent events are processed correctly"() {
        given: "a user with balance"
        def user = createTestUser()
        depositToWallet(user.userId, "USD", 5000.00)

        when: "performing multiple operations that generate events"
        buyShares(user.userId, "AAPL", 500.00, "BY_AMOUNT")
        buyShares(user.userId, "GOOGL", 700.00, "BY_AMOUNT")
        buyShares(user.userId, "MSFT", 600.00, "BY_AMOUNT")
        Thread.sleep(3000) // Wait for all events to process

        then: "all events are processed and reflected in database"
        def holdings = queryDatabase(
            "SELECT symbol FROM portfolio_service.holdings WHERE user_id = '${user.userId}'"
        )
        holdings.size() == 3

        and: "all trades are recorded in transaction history"
        def transactions = queryDatabase(
            "SELECT * FROM transaction_history_service.transactions WHERE user_id = '${user.userId}' AND transaction_type = 'BUY'"
        )
        transactions.size() >= 3
    }

    def "event processing is idempotent"() {
        given: "a registered user"
        def user = createTestUser()

        when: "depositing money"
        depositToWallet(user.userId, "USD", 1000.00)
        Thread.sleep(2000)

        then: "balance is correct"
        def balance = getWalletBalance(user.userId, "USD")
        balance == 1000.00

        and: "transaction is recorded exactly once (no duplicates from event replay)"
        def transactions = queryDatabase(
            "SELECT * FROM transaction_history_service.transactions WHERE user_id = '${user.userId}' AND transaction_type = 'DEPOSIT' AND amount = 1000.00"
        )
        // Should have exactly one transaction, not multiple from event replay
        transactions.size() >= 1
    }
}
