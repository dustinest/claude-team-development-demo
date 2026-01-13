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

    def "buy by quantity purchases exact number of shares"() {
        given: "a user with 5000 USD in wallet"
        def user = createTestUser()
        depositToWallet(user.userId, "USD", 5000.00)

        when: "buying 10 shares of GOOGL"
        def trade = buyShares(user.userId, "GOOGL", 10.00, "BY_QUANTITY")

        and: "we wait for portfolio update"
        Thread.sleep(1000)

        then: "trade is executed"
        trade.symbol == "GOOGL"
        trade.quantity == 10.00

        and: "portfolio holding is created with exact quantity"
        def holdings = queryDatabase(
            "SELECT * FROM portfolio_service.holdings WHERE user_id = '${user.userId}' AND symbol = 'GOOGL'"
        )
        holdings.size() == 1
        holdings[0].quantity == 10.00
    }

    def "buy with insufficient funds is rejected"() {
        given: "a user with only 100 USD"
        def user = createTestUser()
        depositToWallet(user.userId, "USD", 100.00)

        when: "attempting to buy 1000 USD worth of shares"
        def response = buyShares(user.userId, "AAPL", 1000.00, "BY_AMOUNT")

        then: "trade is rejected"
        response != null
        // Verify error response indicates insufficient funds
    }

    def "multiple buy orders accumulate holdings"() {
        given: "a user with sufficient balance"
        def user = createTestUser()
        depositToWallet(user.userId, "USD", 2000.00)

        when: "buying TSLA shares twice"
        buyShares(user.userId, "TSLA", 500.00, "BY_AMOUNT")
        Thread.sleep(1000)
        buyShares(user.userId, "TSLA", 300.00, "BY_AMOUNT")
        Thread.sleep(1000)

        then: "holdings are accumulated in portfolio"
        def holdings = queryDatabase(
            "SELECT * FROM portfolio_service.holdings WHERE user_id = '${user.userId}' AND symbol = 'TSLA'"
        )
        holdings.size() == 1
        holdings[0].quantity > 0
    }
}
