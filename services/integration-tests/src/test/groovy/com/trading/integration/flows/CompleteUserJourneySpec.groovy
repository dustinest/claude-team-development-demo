package com.trading.integration.flows

import com.trading.integration.BaseIntegrationSpec

import static io.restassured.RestAssured.given

class CompleteUserJourneySpec extends BaseIntegrationSpec {

    def "complete user journey from signup to trading"() {
        given: "a new user"
        def email = "journey-test-${UUID.randomUUID().toString()}@example.com".toString()
        def username = "journeyuser"

        when: "user signs up"
        def signupResponse = given()
            .contentType("application/json")
            .body([
                email: email,
                username: username,
                phoneNumber: "+1234567890"
            ])
            .post("${API_GATEWAY_URL}/api/v1/signup")
            .then()
            .statusCode(201)
            .extract()
            .response()

        def userId = signupResponse.path("userId")
        Thread.sleep(2000) // Wait for Kafka processing

        then: "user is registered in database"
        def dbUser = queryDatabase("SELECT * FROM user_service.users WHERE id = '${userId}'")
        dbUser.size() == 1

        when: "user deposits money"
        depositToWallet(userId, "USD", 5000.00)

        then: "wallet balance is created"
        def balance = getWalletBalance(userId, "USD")
        balance == 5000.00

        when: "user buys multiple stocks"
        buyShares(userId, "AAPL", 1000.00, "BY_AMOUNT")
        Thread.sleep(1000)
        buyShares(userId, "GOOGL", 1500.00, "BY_AMOUNT")
        Thread.sleep(1000)
        buyShares(userId, "MSFT", 1000.00, "BY_AMOUNT")
        Thread.sleep(1000)

        then: "portfolio contains all holdings"
        def holdings = queryDatabase(
            "SELECT symbol FROM portfolio_service.holdings WHERE user_id = '${userId}'"
        )
        holdings.size() == 3
        holdings*.symbol.containsAll(["AAPL", "GOOGL", "MSFT"])

        and: "wallet balance is reduced"
        def remainingBalance = getWalletBalance(userId, "USD")
        remainingBalance < 5000.00
        remainingBalance > 0

        when: "user sells some shares"
        sellShares(userId, "AAPL", 1.0, "BY_QUANTITY")
        Thread.sleep(1000)

        then: "wallet balance increases from sale proceeds"
        def balanceAfterSale = getWalletBalance(userId, "USD")
        balanceAfterSale > remainingBalance

        when: "checking transaction history"
        def transactions = queryDatabase(
            "SELECT * FROM transaction_history_service.transactions WHERE user_id = '${userId}' ORDER BY timestamp"
        )

        then: "all transactions are recorded"
        transactions.size() >= 5 // 1 deposit + 3 buys + 1 sell
    }

    def "user can manage multiple currency balances"() {
        given: "a registered user"
        def user = createTestUser()

        when: "depositing multiple currencies"
        depositToWallet(user.userId, "USD", 5000.00)
        depositToWallet(user.userId, "EUR", 4000.00)
        depositToWallet(user.userId, "GBP", 3000.00)

        then: "all balances are maintained separately"
        def usdBalance = getWalletBalance(user.userId, "USD")
        def eurBalance = getWalletBalance(user.userId, "EUR")
        def gbpBalance = getWalletBalance(user.userId, "GBP")

        usdBalance == 5000.00
        eurBalance == 4000.00
        gbpBalance == 3000.00

        when: "exchanging currencies"
        given()
            .contentType("application/json")
            .body([
                fromCurrency: "USD",
                toCurrency: "EUR",
                amount: 1000.00
            ])
            .post("${WALLET_SERVICE_URL}/api/v1/wallets/${user.userId}/exchange")
            .then()
            .statusCode(200)

        then: "balances are updated correctly"
        def newUsdBalance = getWalletBalance(user.userId, "USD")
        def newEurBalance = getWalletBalance(user.userId, "EUR")

        newUsdBalance == 4000.00
        newEurBalance > 4000.00
    }

    def "concurrent operations are handled correctly"() {
        given: "a user with sufficient balance"
        def user = createTestUser()
        depositToWallet(user.userId, "USD", 10000.00)

        when: "performing multiple operations rapidly"
        buyShares(user.userId, "AAPL", 500.00, "BY_AMOUNT")
        buyShares(user.userId, "GOOGL", 700.00, "BY_AMOUNT")
        buyShares(user.userId, "MSFT", 600.00, "BY_AMOUNT")
        buyShares(user.userId, "TSLA", 800.00, "BY_AMOUNT")
        Thread.sleep(3000) // Wait for all to process

        then: "all operations complete successfully"
        def holdings = queryDatabase(
            "SELECT symbol FROM portfolio_service.holdings WHERE user_id = '${user.userId}'"
        )
        holdings.size() == 4

        and: "wallet balance reflects all purchases"
        def balance = getWalletBalance(user.userId, "USD")
        balance < 10000.00
        balance > 0
    }
}
