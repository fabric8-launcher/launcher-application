package io.openshift.booster;


import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.arquillian.cube.openshift.impl.enricher.AwaitRoute;
import org.arquillian.cube.openshift.impl.enricher.RouteURL;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.URL;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.delete;
import static io.restassured.RestAssured.get;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.hamcrest.core.Is.is;

/**
 * Check the behavior of the application when running in OpenShift.
 */
@RunWith(Arquillian.class)
public class OpenShiftIT {

  @AwaitRoute
  @RouteURL("${app.name}")
  private URL route;

  @Before
  public void setup() {
    RestAssured.baseURI = route.toString() + "api/fruits";

    String s = get().asString();
    JsonArray array = new JsonArray(s);
    for (int i = 0; i < array.size(); i++) {
      JsonObject json = array.getJsonObject(i);
      long id = json.getLong("id");
      delete("/" + id);
    }
  }

  @Test
  public void testRetrieveWhenNoFruits() {
    get()
      .then()
      .assertThat().statusCode(200).body(is("[ ]"));
  }

  @Test
  public void testWithOneFruit() {
    given()
      .body(new JsonObject().put("name", "apple").put("stock", 5).encode())
      .post()
      .then().assertThat().statusCode(201);

    String payload = get()
      .then()
      .assertThat().statusCode(200).extract().asString();
    JsonArray json = new JsonArray(payload);
    assertThat(json).hasSize(1);
    assertThat(json.getJsonObject(0).getMap()).contains(entry("name", "apple"), entry("stock", 5));
    Long id = json.getJsonObject(0).getLong("id");
    assertThat(id).isNotNull().isGreaterThanOrEqualTo(0);

    payload = get("/" + id).then().assertThat().statusCode(200).extract().asString();
    JsonObject obj = new JsonObject(payload);
    assertThat(obj.getMap()).contains(entry("name", "apple"), entry("stock", 5));
    assertThat(obj.getLong("id")).isNotNull().isGreaterThanOrEqualTo(id);

  }

  @Test
  public void testCreatingAFruit() {
    Response response = given()
      .body(new JsonObject().put("name", "apple").put("stock", 5).encode())
      .post()
      .then().assertThat().statusCode(201).extract().response();

    assertThat(response.header("Location")).isNotBlank();
    JsonObject result = new JsonObject(response.asString());
    assertThat(result.getLong("id")).isGreaterThanOrEqualTo(0);
    assertThat(result.getString("name")).isEqualTo("apple");
    assertThat(result.getInteger("stock")).isEqualTo(5);

    String payload = get()
      .then()
      .assertThat().statusCode(200).extract().asString();
    JsonArray json = new JsonArray(payload);
    assertThat(json).hasSize(1);
    assertThat(json.getJsonObject(0).getMap()).contains(entry("name", "apple"), entry("stock", 5));
    assertThat(json.getJsonObject(0).getLong("id")).isNotNull().isGreaterThanOrEqualTo(0);
  }

  @Test
  public void testCreatingAFruitWithoutAName() {
    Response response = given()
      .body(new JsonObject().put("stock", 5).encode())
      .post()
      .then().assertThat().statusCode(422).extract().response();

    JsonObject result = new JsonObject(response.asString());
    assertThat(result.getString("error")).isNotBlank();
    assertThat(result.getString("path")).isEqualTo("/api/fruits");
  }

  @Test
  public void testCreatingAFruitWithoutAStock() {
    Response response = given()
      .body(new JsonObject().put("name", "Banana").encode())
      .post()
      .then().assertThat().statusCode(422).extract().response();

    JsonObject result = new JsonObject(response.asString());
    assertThat(result.getString("error")).isNotBlank();
    assertThat(result.getString("path")).isEqualTo("/api/fruits");
  }

  @Test
  public void testCreatingAFruitWithStockLowerThanZero() {
    Response response = given()
      .body(new JsonObject().put("name", "Banana").put("stock",-1).encode())
      .post()
      .then().assertThat().statusCode(422).extract().response();

    JsonObject result = new JsonObject(response.asString());
    assertThat(result.getString("error")).isNotBlank();
    assertThat(result.getString("path")).isEqualTo("/api/fruits");
  }

  @Test
  public void testCreatingAFruitWithAnId() {
    Response response = given()
      .body(new JsonObject().put("stock", 5).put("name", "apple").put("id", 2456).encode())
      .post()
      .then().assertThat().statusCode(422).extract().response();

    JsonObject result = new JsonObject(response.asString());
    assertThat(result.getString("error")).isNotBlank();
    assertThat(result.getString("path")).isEqualTo("/api/fruits");
  }

  @Test
  public void testCreatingWithNoPayload() {
    Response response = given()
      .body("")
      .post()
      .then().assertThat().statusCode(415).extract().response();

    JsonObject result = new JsonObject(response.asString());
    assertThat(result.getString("error")).isNotBlank();
    assertThat(result.getString("path")).isEqualTo("/api/fruits");
  }

  @Test
  public void testCreatingWithBrokenPayload() {
    Response response = given()
      .body("<name>apple</name><stock>22</stock>")
      .post()
      .then().assertThat().statusCode(415).extract().response();

    JsonObject result = new JsonObject(response.asString());
    assertThat(result.getString("error")).isNotBlank();
    assertThat(result.getString("path")).isEqualTo("/api/fruits");
  }

  @Test
  public void testEditingAFruit() {
    Response response = given()
      .body(new JsonObject().put("name", "apple").put("stock", 5).encode())
      .post()
      .then().assertThat().statusCode(201).extract().response();

    JsonObject result = new JsonObject(response.asString());
    long id = result.getLong("id");
    assertThat(id).isGreaterThanOrEqualTo(0);
    assertThat(result.getString("name")).isEqualTo("apple");
    assertThat(result.getInteger("stock")).isEqualTo(5);

    response = given()
      .body(new JsonObject().put("name", "pear").put("stock", 10).encode())
      .put("/" + id)
      .then().assertThat().statusCode(200).extract().response();

    result = new JsonObject(response.asString());
    assertThat(result.getLong("id")).isEqualTo(id);
    assertThat(result.getString("name")).isEqualTo("pear");
    assertThat(result.getInteger("stock")).isEqualTo(10);

    String payload = get()
      .then()
      .assertThat().statusCode(200).extract().asString();
    JsonArray json = new JsonArray(payload);
    assertThat(json).hasSize(1);
    assertThat(json.getJsonObject(0).getMap()).contains(entry("id", (int) id), entry("name", "pear"), entry("stock",
      10));
  }

  @Test
  public void testEditingAnUnknownFruit() {
    Response response = given()
      .body(new JsonObject().put("name", "pear").put("stock", 10).encode())
      .put("/" + 22222222)
      .then().assertThat().statusCode(404).extract().response();

    JsonObject result = new JsonObject(response.asString());
    assertThat(result.getString("error")).isNotBlank();
    assertThat(result.getString("path")).isEqualTo("/api/fruits/22222222");
  }

  @Test
  public void testEditingAnUnknownFruitWithStringId() {
    Response response = given()
      .body(new JsonObject().put("name", "pear").put("stock", 10).encode())
      .put("/999999")
      .then().assertThat().statusCode(404).extract().response();

    JsonObject result = new JsonObject(response.asString());
    assertThat(result.getString("error")).isNotBlank();
    assertThat(result.getString("path")).isEqualTo("/api/fruits/999999");
  }

  @Test
  public void testEditingAFruitWithEmptyPayload() {
    Response response = given()
      .body(new JsonObject().put("name", "apple").put("stock", 5).encode())
      .post()
      .then().assertThat().statusCode(201).extract().response();

    JsonObject result = new JsonObject(response.asString());
    long id = result.getLong("id");

    response = given()
      .body("")
      .put("/" + id)
      .then().assertThat().statusCode(415).extract().response();

    result = new JsonObject(response.asString());
    assertThat(result.getString("error")).isNotBlank();
    assertThat(result.getString("path")).isEqualTo("/api/fruits/" + id);
  }

  @Test
  public void testEditingAFruitWithBrokenPayload() {
    Response response = given()
      .body(new JsonObject().put("name", "apple").put("stock", 5).encode())
      .post()
      .then().assertThat().statusCode(201).extract().response();

    JsonObject result = new JsonObject(response.asString());
    long id = result.getLong("id");

    response = given()
      .body("{\"name\":\"pear\", \"stock\":") // not complete on purpose.
      .put("/" + id)
      .then().assertThat().statusCode(415).extract().response();

    result = new JsonObject(response.asString());
    assertThat(result.getString("error")).isNotBlank();
    assertThat(result.getString("path")).isEqualTo("/api/fruits/" + id);
  }

  @Test
  public void testEditingAFruitWithInvalidPayload() {
    Response response = given()
      .body(new JsonObject().put("name", "apple").put("stock", 5).encode())
      .post()
      .then().assertThat().statusCode(201).extract().response();

    JsonObject result = new JsonObject(response.asString());
    long id = result.getLong("id");

    response = given()
      .body(new JsonObject().put("name", "pear").put("stock", 5).put("id", id + 1).encode())
      .put("/" + id)
      .then().assertThat().statusCode(422).extract().response();

    result = new JsonObject(response.asString());
    assertThat(result.getString("error")).isNotBlank();
    assertThat(result.getString("path")).isEqualTo("/api/fruits/" + id);
  }

  @Test
  public void testEditingAFruitWithoutAStock() {
    Response response = given()
      .body(new JsonObject().put("name", "Banana").put("stock", 5).encode())
      .post()
      .then().assertThat().statusCode(201).extract().response();

    JsonObject result = new JsonObject(response.asString());
    long id = result.getLong("id");
    response = given()
      .body(new JsonObject().put("name", "Banana").encode())
      .put("/" + id)
      .then().assertThat().statusCode(422).extract().response();

    result = new JsonObject(response.asString());
    assertThat(result.getString("error")).isNotBlank();
    assertThat(result.getString("path")).isEqualTo("/api/fruits/" + id);
  }

  @Test
  public void testEditingAFruitWithStockLowerThanZero() {
    Response response = given()
      .body(new JsonObject().put("name", "Banana").put("stock", 5).encode())
      .post()
      .then().assertThat().statusCode(201).extract().response();

   JsonObject result = new JsonObject(response.asString());
    long id = result.getLong("id");
    response = given()
      .body(new JsonObject().put("name", "Banana").put("stock",-1).encode())
      .put("/" + id)
      .then().assertThat().statusCode(422).extract().response();

    result = new JsonObject(response.asString());
    assertThat(result.getString("error")).isNotBlank();
    assertThat(result.getString("path")).isEqualTo("/api/fruits/" + id);
  }

  @Test
  public void testDeletingAFruit() {
    Response response = given()
      .body(new JsonObject().put("name", "apple").put("stock", 5).encode())
      .post()
      .then().assertThat().statusCode(201).extract().response();

    JsonObject result = new JsonObject(response.asString());
    long id = result.getLong("id");

    delete("/" + id)
      .then().assertThat().statusCode(204);

    get()
      .then()
      .assertThat().statusCode(200).body(is("[ ]"));
  }

  @Test
  public void testDeletingAnUnknownFruit() {
    delete("/99999")
      .then().assertThat().statusCode(404);

    get()
      .then()
      .assertThat().statusCode(200).body(is("[ ]"));
  }

}
