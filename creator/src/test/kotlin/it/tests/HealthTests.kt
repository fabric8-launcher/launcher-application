package it.tests

import org.junit.jupiter.api.DynamicTest
import io.restassured.RestAssured.get
import it.Context
import it.IntegrationTests

class HealthTests(val context: Context): IntegrationTests {
    val HEALTH_PATH = "health"

    override fun integrationTests(): Iterable<DynamicTest> {
        return listOf(
            DynamicTest.dynamicTest("Health - testOk", this::testOk)
        )
    }

    fun testOk() {
        get(HEALTH_PATH)
        .then()
            .statusCode(200)
    }
}
