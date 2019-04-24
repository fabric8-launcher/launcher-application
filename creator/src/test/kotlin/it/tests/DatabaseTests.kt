package it.tests

import io.fabric8.launcher.creator.core.propsOf
import io.restassured.RestAssured.*
import it.Context
import it.IntegrationTests
import org.hamcrest.CoreMatchers.*
import org.junit.jupiter.api.DynamicTest
import java.net.HttpURLConnection

class DatabaseTests(val context: Context) : IntegrationTests {
    private val fruitsPath = "api/fruits"

    private val itemApple = propsOf("id" to 1, "name" to "Apple", "stock" to 10)
    private val itemOrange = propsOf("id" to 2, "name" to "Orange", "stock" to 10)

    private val updateCherry = propsOf("name" to "Cherry", "stock" to 15)
    private val itemCherry = propsOf("id" to 3, "name" to "Cherry", "stock" to 15)

    private val illegal = propsOf("foo" to "Dummy", "bar" to true)

    private val insertBanana = propsOf("name" to "Banana", "stock" to 1)

    override fun integrationTests(): Iterable<DynamicTest> {
        return listOf(
            DynamicTest.dynamicTest("Database - DatabaseGetAll", this::DatabaseGetAll),
            DynamicTest.dynamicTest("Database - DatabaseGetOneOk", this::DatabaseGetOneOk),
            DynamicTest.dynamicTest("Database - DatabaseGetOneUnknown", this::DatabaseGetOneUnknown),
            DynamicTest.dynamicTest("Database - DatabaseUpdateOk", this::DatabaseUpdateOk),
            DynamicTest.dynamicTest("Database - DatabaseUpdateUnknown", this::DatabaseUpdateUnknown),
            DynamicTest.dynamicTest("Database - DatabaseUpdateNotJson", this::DatabaseUpdateNotJson),
            DynamicTest.dynamicTest("Database - DatabaseUpdateIllegalPayload", this::DatabaseUpdateIllegalPayload),
            DynamicTest.dynamicTest("Database - DatabaseInsertDeleteOk", this::DatabaseInsertDeleteOk),
            DynamicTest.dynamicTest("Database - DatabaseInsertNotJson", this::DatabaseInsertNotJson),
            DynamicTest.dynamicTest("Database - DatabaseInsertIllegalPayload", this::DatabaseInsertIllegalPayload),
            DynamicTest.dynamicTest("Database - DatabaseDeleteUnknown", this::DatabaseDeleteUnknown)
        )
    }

    fun DatabaseGetAll() {
        get(fruitsPath)
            .then()
            .statusCode(HttpURLConnection.HTTP_OK)
            .body("", hasItems(itemApple, itemOrange))
    }

    fun DatabaseGetOneOk() {
        get("$fruitsPath/1")
            .then()
            .statusCode(HttpURLConnection.HTTP_OK)
            .body("", equalTo(itemApple))
    }

    fun DatabaseGetOneUnknown() {
        get("$fruitsPath/42")
            .then()
            .statusCode(HttpURLConnection.HTTP_NOT_FOUND)
    }

    fun DatabaseUpdateOk() {
        given()
            .contentType("application/json")
            .body(updateCherry)
            .put("$fruitsPath/3")
        .then()
            .statusCode(HttpURLConnection.HTTP_OK)
            .body("", equalTo(itemCherry))
    }

    fun DatabaseUpdateUnknown() {
        given()
            .contentType("application/json")
            .body(updateCherry)
            .put("$fruitsPath/42")
        .then()
            .statusCode(HttpURLConnection.HTTP_NOT_FOUND)
    }

    fun DatabaseUpdateNotJson() {
        given()
            .contentType("text/plain")
            .body("dummy")
            .put("$fruitsPath/1")
        .then()
            .statusCode(HttpURLConnection.HTTP_UNSUPPORTED_TYPE)
    }

    fun DatabaseUpdateIllegalPayload() {
        given()
            .contentType("application/json")
            .body(illegal)
            .put("$fruitsPath/1")
        .then()
            .statusCode(anyOf(equalTo(422 /* UNPROCESSABLE_ENTITY */), equalTo(HttpURLConnection.HTTP_BAD_REQUEST)))
    }

    fun DatabaseInsertDeleteOk() {
        val id = given()
            .contentType("application/json")
            .body(insertBanana)
            .post(fruitsPath)
        .then()
            .statusCode(HttpURLConnection.HTTP_CREATED)
        .extract()
            .path<Int>("id")
        delete("$fruitsPath/$id")
            .then()
            .statusCode(HttpURLConnection.HTTP_NO_CONTENT)
    }

    fun DatabaseInsertNotJson() {
        given()
            .contentType("text/plain")
            .body("dummy")
            .post(fruitsPath)
        .then()
            .statusCode(HttpURLConnection.HTTP_UNSUPPORTED_TYPE)
    }

    fun DatabaseInsertIllegalPayload() {
        given()
            .contentType("application/json")
            .body(illegal)
            .post(fruitsPath)
        .then()
            .statusCode(anyOf(equalTo(422 /* UNPROCESSABLE_ENTITY */), equalTo(HttpURLConnection.HTTP_BAD_REQUEST)))
    }

    fun DatabaseDeleteUnknown() {
        delete("$fruitsPath/42")
            .then()
            .statusCode(HttpURLConnection.HTTP_NOT_FOUND)
    }
}
