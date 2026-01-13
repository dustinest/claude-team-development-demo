package com.trading.integration.wallet

import com.trading.integration.BaseIntegrationSpec

import static io.restassured.RestAssured.given

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
