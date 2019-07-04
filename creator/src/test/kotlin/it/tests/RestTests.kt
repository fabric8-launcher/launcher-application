package it.tests

import io.restassured.RestAssured.get
import io.restassured.RestAssured.given
import it.Context
import it.IntegrationTests
import org.hamcrest.CoreMatchers.containsString
import org.junit.jupiter.api.DynamicTest

class RestTests(val context: Context) : IntegrationTests {
    val GREETING_PATH = "api/greeting"

    override fun integrationTests(): Iterable<DynamicTest> {
        return listOf(
            DynamicTest.dynamicTest("Rest - GreetingDefault", this::GreetingDefault),
            DynamicTest.dynamicTest("Rest - GreetingWithName", this::GreetingWithName)
        )
    }

    fun `GreetingDefault`() {
        get(GREETING_PATH)
        .then()
            .statusCode(200)
            .body("content", containsString("World"))
    }

    fun `GreetingWithName`() {
        given()
            .param("name", "Tako")
        .`when`()
            .get(GREETING_PATH)
        .then()
            .statusCode(200)
            .body("content", containsString("Tako"))
    }
}
