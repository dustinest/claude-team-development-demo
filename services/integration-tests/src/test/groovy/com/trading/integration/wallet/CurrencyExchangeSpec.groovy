package com.trading.integration.wallet

import com.trading.integration.BaseIntegrationSpec

import static io.restassured.RestAssured.given

class CurrencyExchangeSpec extends BaseIntegrationSpec {

    def "currency exchange converts USD to EUR correctly"() {
        given: "a user with 1000 USD"
        def user = createTestUser()
        depositToWallet(user.userId, "USD", 1000.00)

        when: "exchanging 500 USD to EUR"
        def response = given()
            .contentType("application/json")
            .body([
                fromCurrency: "USD",
                toCurrency: "EUR",
                amount: 500.00
            ])
            .post("${WALLET_SERVICE_URL}/api/v1/wallets/${user.userId}/exchange")
            .then()
            .statusCode(200)
            .extract()
            .response()

        then: "USD balance is reduced"
        def usdBalance = getWalletBalance(user.userId, "USD")
        usdBalance == 500.00

        and: "EUR balance is created with converted amount"
        def eurBalance = getWalletBalance(user.userId, "EUR")
        eurBalance > 0

        and: "both balances are stored in wallet_service schema"
        def dbBalances = queryDatabase(
            "SELECT currency, balance FROM wallet_service.wallet_balances WHERE user_id = '${user.userId}' ORDER BY currency"
        )
        dbBalances.size() == 2
    }

    def "currency exchange with insufficient funds is rejected"() {
        given: "a user with only 100 USD"
        def user = createTestUser()
        depositToWallet(user.userId, "USD", 100.00)

        when: "attempting to exchange 500 USD to EUR"
        def response = given()
            .contentType("application/json")
            .body([
                fromCurrency: "USD",
                toCurrency: "EUR",
                amount: 500.00
            ])
            .post("${WALLET_SERVICE_URL}/api/v1/wallets/${user.userId}/exchange")
            .then()
            .extract()
            .response()

        then: "request is rejected"
        response.statusCode() in [400, 409]
    }

    def "currency exchange supports multiple currencies"() {
        given: "a user with balances in multiple currencies"
        def user = createTestUser()
        depositToWallet(user.userId, "USD", 1000.00)
        depositToWallet(user.userId, "EUR", 800.00)

        when: "exchanging EUR to GBP"
        def response = given()
            .contentType("application/json")
            .body([
                fromCurrency: "EUR",
                toCurrency: "GBP",
                amount: 500.00
            ])
            .post("${WALLET_SERVICE_URL}/api/v1/wallets/${user.userId}/exchange")
            .then()
            .statusCode(200)
            .extract()
            .response()

        then: "EUR balance is reduced"
        def eurBalance = getWalletBalance(user.userId, "EUR")
        eurBalance == 300.00

        and: "GBP balance is created"
        def gbpBalance = getWalletBalance(user.userId, "GBP")
        gbpBalance > 0

        and: "USD balance remains unchanged"
        def usdBalance = getWalletBalance(user.userId, "USD")
        usdBalance == 1000.00
    }
}
