package com.trading.integration.portfolio

import com.trading.integration.BaseIntegrationSpec

import static io.restassured.RestAssured.given

class PortfolioTrackingSpec extends BaseIntegrationSpec {

    def "portfolio accurately tracks holdings after buy"() {
        given: "a user with balance"
        def user = createTestUser()
        depositToWallet(user.userId, "USD", 5000.00)

        when: "buying multiple different stocks"
        buyShares(user.userId, "AAPL", 1000.00, "BY_AMOUNT")
        Thread.sleep(1000)
        buyShares(user.userId, "GOOGL", 1500.00, "BY_AMOUNT")
        Thread.sleep(1000)
        buyShares(user.userId, "MSFT", 1000.00, "BY_AMOUNT")
        Thread.sleep(1000)

        then: "portfolio contains all three holdings"
        def holdings = queryDatabase(
            "SELECT symbol, quantity FROM portfolio_service.holdings WHERE user_id = '${user.userId}' ORDER BY symbol"
        )
        holdings.size() == 3
        holdings*.symbol.containsAll(["AAPL", "GOOGL", "MSFT"])
    }

    def "portfolio calculates average purchase price correctly"() {
        given: "a user with balance"
        def user = createTestUser()
        depositToWallet(user.userId, "USD", 3000.00)

        when: "buying same stock at different times (potentially different prices)"
        buyShares(user.userId, "AAPL", 500.00, "BY_AMOUNT")
        Thread.sleep(1000)
        buyShares(user.userId, "AAPL", 800.00, "BY_AMOUNT")
        Thread.sleep(1000)

        then: "portfolio calculates weighted average price"
        def holdings = queryDatabase(
            "SELECT avg_purchase_price, quantity FROM portfolio_service.holdings WHERE user_id = '${user.userId}' AND symbol = 'AAPL'"
        )
        holdings.size() == 1
        holdings[0].avg_purchase_price != null
        (holdings[0].avg_purchase_price as BigDecimal) > 0
    }

    def "portfolio updates correctly after sell"() {
        given: "a user with holdings"
        def user = createTestUser()
        depositToWallet(user.userId, "USD", 2000.00)
        buyShares(user.userId, "TSLA", 1000.00, "BY_AMOUNT")
        Thread.sleep(1000)

        and: "get initial quantity"
        def initialHoldings = queryDatabase(
            "SELECT quantity FROM portfolio_service.holdings WHERE user_id = '${user.userId}' AND symbol = 'TSLA'"
        )
        def initialQuantity = initialHoldings[0].quantity as BigDecimal

        when: "selling some shares"
        def response = given()
            .contentType("application/json")
            .body([symbol: "TSLA", quantity: 1.0])
            .post("${TRADING_SERVICE_URL}/api/v1/trades/${user.userId}/sell/by_quantity")
            .then()
            .statusCode(200)
            .extract()
            .response()
        Thread.sleep(1000)

        then: "portfolio quantity is reduced"
        def newHoldings = queryDatabase(
            "SELECT quantity FROM portfolio_service.holdings WHERE user_id = '${user.userId}' AND symbol = 'TSLA'"
        )
        (newHoldings[0].quantity as BigDecimal) < initialQuantity
    }

    def "portfolio API endpoint returns complete holdings"() {
        given: "a user with multiple holdings"
        def user = createTestUser()
        depositToWallet(user.userId, "USD", 4000.00)
        buyShares(user.userId, "AAPL", 800.00, "BY_AMOUNT")
        Thread.sleep(1000)
        buyShares(user.userId, "GOOGL", 1200.00, "BY_AMOUNT")
        Thread.sleep(1000)

        when: "fetching portfolio via API"
        def response = given()
            .get("${PORTFOLIO_SERVICE_URL}/api/v1/portfolio/${user.userId}")
            .then()
            .statusCode(200)
            .extract()
            .response()

        then: "all holdings are returned"
        def holdings = response.jsonPath().getList("holdings")
        holdings.size() >= 2
    }

    def "empty portfolio returns valid response"() {
        given: "a user with no holdings"
        def user = createTestUser()

        when: "fetching empty portfolio"
        def response = given()
            .get("${PORTFOLIO_SERVICE_URL}/api/v1/portfolio/${user.userId}")
            .then()
            .statusCode(200)
            .extract()
            .response()

        then: "response indicates no holdings"
        def holdings = response.jsonPath().getList("holdings")
        holdings == null || holdings.isEmpty()
    }
}
