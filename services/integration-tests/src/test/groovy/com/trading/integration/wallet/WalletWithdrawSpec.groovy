package com.trading.integration.wallet

import com.trading.integration.BaseIntegrationSpec

import static io.restassured.RestAssured.given

class WalletWithdrawSpec extends BaseIntegrationSpec {

    def "withdraw reduces wallet balance correctly"() {
        given: "a user with 1000 USD"
        def user = createTestUser()
        depositToWallet(user.userId, "USD", 1000.00)

        when: "withdrawing 400 USD"
        def response = given()
            .contentType("application/json")
            .body([currency: "USD", amount: 400.00])
            .post("${WALLET_SERVICE_URL}/api/v1/wallets/${user.userId}/withdraw")
            .then()
            .statusCode(200)
            .extract()
            .response()

        then: "balance is reduced to 600"
        def balance = getWalletBalance(user.userId, "USD")
        balance == 600.00

        and: "balance is updated in wallet_service schema"
        def dbBalance = queryDatabase(
            "SELECT balance FROM wallet_service.wallet_balances WHERE user_id = '${user.userId}' AND currency = 'USD'"
        )
        dbBalance[0].balance == 600.00
    }

    def "withdraw with insufficient funds is rejected"() {
        given: "a user with only 100 USD"
        def user = createTestUser()
        depositToWallet(user.userId, "USD", 100.00)

        when: "attempting to withdraw 500 USD"
        def response = given()
            .contentType("application/json")
            .body([currency: "USD", amount: 500.00])
            .post("${WALLET_SERVICE_URL}/api/v1/wallets/${user.userId}/withdraw")
            .then()
            .extract()
            .response()

        then: "request is rejected"
        response.statusCode() in [400, 409]
    }

    def "withdraw with negative amount is rejected"() {
        given: "a user with balance"
        def user = createTestUser()
        depositToWallet(user.userId, "USD", 1000.00)

        when: "attempting to withdraw negative amount"
        def response = given()
            .contentType("application/json")
            .body([currency: "USD", amount: -100.00])
            .post("${WALLET_SERVICE_URL}/api/v1/wallets/${user.userId}/withdraw")
            .then()
            .extract()
            .response()

        then: "request is rejected"
        response.statusCode() == 400
    }

    def "withdraw from non-existent currency creates error"() {
        given: "a user with no EUR balance"
        def user = createTestUser()

        when: "attempting to withdraw EUR"
        def response = given()
            .contentType("application/json")
            .body([currency: "EUR", amount: 100.00])
            .post("${WALLET_SERVICE_URL}/api/v1/wallets/${user.userId}/withdraw")
            .then()
            .extract()
            .response()

        then: "request is rejected"
        response.statusCode() in [400, 404, 409]
    }
}
