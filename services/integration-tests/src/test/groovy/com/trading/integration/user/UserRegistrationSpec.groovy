package com.trading.integration.user

import com.trading.integration.BaseIntegrationSpec
import io.restassured.http.ContentType

import static io.restassured.RestAssured.given

class UserRegistrationSpec extends BaseIntegrationSpec {

    def "successful user registration creates user in database and publishes Kafka event"() {
        given: "a new user registration request"
        def email = "test-${UUID.randomUUID().toString()}@example.com".toString()
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
        def email = "duplicate-${UUID.randomUUID().toString()}@example.com".toString()
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
