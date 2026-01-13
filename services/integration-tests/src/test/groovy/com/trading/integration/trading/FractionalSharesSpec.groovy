package com.trading.integration.trading

import com.trading.integration.BaseIntegrationSpec

class FractionalSharesSpec extends BaseIntegrationSpec {

    def "system supports fractional shares with 0.01 precision"() {
        given: "a user with sufficient balance"
        def user = createTestUser()
        depositToWallet(user.userId, "USD", 1000.00)

        when: "buying shares by amount (results in fractional shares)"
        buyShares(user.userId, "AAPL", 177.50, "BY_AMOUNT")
        Thread.sleep(1000)

        then: "holdings are stored with fractional precision"
        def holdings = queryDatabase(
            "SELECT quantity FROM portfolio_service.holdings WHERE user_id = '${user.userId}' AND symbol = 'AAPL'"
        )
        def quantity = holdings[0].quantity as BigDecimal
        quantity.scale() <= 2
        quantity > 0
    }

    def "fractional shares can be sold"() {
        given: "a user with fractional shares"
        def user = createTestUser()
        depositToWallet(user.userId, "USD", 1000.00)
        buyShares(user.userId, "GOOGL", 350.75, "BY_AMOUNT")
        Thread.sleep(1000)

        and: "get initial quantity"
        def initialHoldings = queryDatabase(
            "SELECT quantity FROM portfolio_service.holdings WHERE user_id = '${user.userId}' AND symbol = 'GOOGL'"
        )
        def initialQuantity = initialHoldings[0].quantity as BigDecimal

        when: "selling 0.5 shares (fractional)"
        def response = given()
            .contentType("application/json")
            .body([symbol: "GOOGL", quantity: 0.50])
            .post("${TRADING_SERVICE_URL}/api/v1/trades/${user.userId}/sell/by_quantity")
            .then()
            .statusCode(200)
            .extract()
            .response()

        and: "we wait for portfolio update"
        Thread.sleep(1000)

        then: "holdings are reduced by fractional amount"
        def newHoldings = queryDatabase(
            "SELECT quantity FROM portfolio_service.holdings WHERE user_id = '${user.userId}' AND symbol = 'GOOGL'"
        )
        def newQuantity = newHoldings[0].quantity as BigDecimal
        (initialQuantity - newQuantity) == 0.50
    }

    def "multiple fractional purchases accumulate correctly"() {
        given: "a user with sufficient balance"
        def user = createTestUser()
        depositToWallet(user.userId, "USD", 2000.00)

        when: "buying fractional amounts multiple times"
        buyShares(user.userId, "TSLA", 123.45, "BY_AMOUNT")
        Thread.sleep(1000)
        buyShares(user.userId, "TSLA", 234.56, "BY_AMOUNT")
        Thread.sleep(1000)
        buyShares(user.userId, "TSLA", 345.67, "BY_AMOUNT")
        Thread.sleep(1000)

        then: "all fractional quantities are accumulated"
        def holdings = queryDatabase(
            "SELECT quantity FROM portfolio_service.holdings WHERE user_id = '${user.userId}' AND symbol = 'TSLA'"
        )
        holdings.size() == 1
        def totalQuantity = holdings[0].quantity as BigDecimal
        totalQuantity > 0
        totalQuantity.scale() <= 2
    }

    def "fractional precision is maintained in database"() {
        given: "a user buying shares"
        def user = createTestUser()
        depositToWallet(user.userId, "USD", 500.00)

        when: "buying shares that result in fractional quantity"
        buyShares(user.userId, "AAPL", 299.99, "BY_AMOUNT")
        Thread.sleep(1000)

        then: "database stores fractional values correctly"
        def holdings = queryDatabase(
            "SELECT quantity FROM portfolio_service.holdings WHERE user_id = '${user.userId}' AND symbol = 'AAPL'"
        )
        def quantity = holdings[0].quantity as BigDecimal

        // Verify precision
        quantity > 0
        quantity.scale() <= 2

        // Verify it's actually fractional (not a whole number)
        quantity != quantity.setScale(0, BigDecimal.ROUND_DOWN)
    }
}
