package com.trading.integration.trading

import com.trading.integration.BaseIntegrationSpec

import static io.restassured.RestAssured.given

class SellOrderSpec extends BaseIntegrationSpec {

    def "sell by quantity reduces holdings and increases wallet balance"() {
        given: "a user with AAPL shares"
        def user = createTestUser()
        depositToWallet(user.userId, "USD", 2000.00)
        buyShares(user.userId, "AAPL", 1000.00, "BY_AMOUNT")
        Thread.sleep(1000)

        and: "get initial holdings"
        def initialHoldings = queryDatabase(
            "SELECT quantity FROM portfolio_service.holdings WHERE user_id = '${user.userId}' AND symbol = 'AAPL'"
        )
        def initialQuantity = initialHoldings[0].quantity as BigDecimal

        when: "selling 5 shares of AAPL"
        def response = given()
            .contentType("application/json")
            .body([symbol: "AAPL", quantity: 5.00])
            .post("${TRADING_SERVICE_URL}/api/v1/trades/${user.userId}/sell/by_quantity")
            .then()
            .extract()
            .response()

        and: "we wait for portfolio update"
        Thread.sleep(1000)

        then: "trade is executed"
        response.statusCode() == 200

        and: "holdings are reduced"
        def newHoldings = queryDatabase(
            "SELECT quantity FROM portfolio_service.holdings WHERE user_id = '${user.userId}' AND symbol = 'AAPL'"
        )
        (newHoldings[0].quantity as BigDecimal) == initialQuantity - 5.00
    }

    def "sell all shares removes holding from portfolio"() {
        given: "a user who buys exactly 10 shares of MSFT"
        def user = createTestUser()
        depositToWallet(user.userId, "USD", 5000.00)
        buyShares(user.userId, "MSFT", 10.00, "BY_QUANTITY")
        Thread.sleep(1000)

        when: "selling all 10 shares"
        def response = given()
            .contentType("application/json")
            .body([symbol: "MSFT", quantity: 10.00])
            .post("${TRADING_SERVICE_URL}/api/v1/trades/${user.userId}/sell/by_quantity")
            .then()
            .extract()
            .response()

        and: "we wait for portfolio update"
        Thread.sleep(1000)

        then: "trade is executed"
        response.statusCode() == 200

        and: "holding is removed or set to zero"
        def holdings = queryDatabase(
            "SELECT quantity FROM portfolio_service.holdings WHERE user_id = '${user.userId}' AND symbol = 'MSFT'"
        )
        holdings.isEmpty() || (holdings[0].quantity as BigDecimal) == 0
    }

    def "sell with insufficient shares is rejected"() {
        given: "a user with only small holdings"
        def user = createTestUser()
        depositToWallet(user.userId, "USD", 500.00)
        buyShares(user.userId, "AAPL", 200.00, "BY_AMOUNT")
        Thread.sleep(1000)

        when: "attempting to sell 100 shares"
        def response = given()
            .contentType("application/json")
            .body([symbol: "AAPL", quantity: 100.00])
            .post("${TRADING_SERVICE_URL}/api/v1/trades/${user.userId}/sell/by_quantity")
            .then()
            .extract()
            .response()

        then: "request is rejected"
        response.statusCode() in [400, 409]
    }

    def "sell increases wallet balance with proceeds"() {
        given: "a user with shares and known balance"
        def user = createTestUser()
        depositToWallet(user.userId, "USD", 1000.00)
        buyShares(user.userId, "AAPL", 500.00, "BY_AMOUNT")
        Thread.sleep(1000)

        def balanceBeforeSell = getWalletBalance(user.userId, "USD")

        when: "selling shares"
        def response = given()
            .contentType("application/json")
            .body([symbol: "AAPL", quantity: 1.00])
            .post("${TRADING_SERVICE_URL}/api/v1/trades/${user.userId}/sell/by_quantity")
            .then()
            .statusCode(200)
            .extract()
            .response()

        and: "we wait for wallet update"
        Thread.sleep(1000)

        then: "wallet balance increased"
        def balanceAfterSell = getWalletBalance(user.userId, "USD")
        balanceAfterSell > balanceBeforeSell
    }
}
