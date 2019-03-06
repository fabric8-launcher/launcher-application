/*
 * Copyright 2016-2017 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.openshift.booster;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.isEmptyString;
import static org.junit.Assert.assertFalse;

import io.openshift.booster.service.Fruit;
import io.openshift.booster.service.FruitRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BoosterApplicationIT {

    private static final String FRUITS_PATH = "api/fruits";

    @Value("${local.server.port}")
    private int port;

    @Autowired
    private FruitRepository fruitRepository;

    @Before
    public void beforeTest() {
        fruitRepository.deleteAll();
        RestAssured.baseURI = String.format("http://localhost:%d/" + FRUITS_PATH, port);
    }

    @Test
    public void testGetAll() {
        Fruit apple = fruitRepository.save(new Fruit("Apple"));
        Fruit orange = fruitRepository.save(new Fruit("Orange"));
        requestSpecification()
                .get()
                .then()
                .statusCode(200)
                .body("id", hasItems(apple.getId(), orange.getId()))
                .body("name", hasItems(apple.getName(), orange.getName()));
    }

    @Test
    public void testGetEmptyArray() {
        requestSpecification()
                .get()
                .then()
                .statusCode(200)
                .body(is("[]"));
    }

    @Test
    public void testGetOne() {
        Fruit apple = fruitRepository.save(new Fruit("Apple"));
        requestSpecification()
                .get(String.valueOf(apple.getId()))
                .then()
                .statusCode(200)
                .body("id", is(apple.getId()))
                .body("name", is(apple.getName()));
    }

    @Test
    public void testGetNotExisting() {
        requestSpecification()
                .get("0")
                .then()
                .statusCode(404);
    }

    @Test
    public void testPost() {
        requestSpecification()
                .contentType(ContentType.JSON)
                .body(Collections.singletonMap("name", "Apple"))
                .post()
                .then()
                .statusCode(201)
                .body("id", not(isEmptyString()))
                .body("name", is("Apple"));
    }

    @Test
    public void testPostWithWrongPayload() {
        requestSpecification()
                .contentType(ContentType.JSON)
                .body(Collections.singletonMap("id", 0))
                .when()
                .post()
                .then()
                .statusCode(422);
    }

    @Test
    public void testPostWithNonJsonPayload() {
        requestSpecification()
                .contentType(ContentType.XML)
                .when()
                .post()
                .then()
                .statusCode(415);
    }

    @Test
    public void testPostWithEmptyPayload() {
        requestSpecification()
                .contentType(ContentType.JSON)
                .when()
                .post()
                .then()
                .statusCode(415);
    }

    @Test
    public void testPut() {
        Fruit apple = fruitRepository.save(new Fruit("Apple"));
        requestSpecification()
                .contentType(ContentType.JSON)
                .body(Collections.singletonMap("name", "Lemon"))
                .when()
                .put(String.valueOf(apple.getId()))
                .then()
                .statusCode(200)
                .body("id", is(apple.getId()))
                .body("name", is("Lemon"));

    }

    @Test
    public void testPutNotExisting() {
        requestSpecification()
                .contentType(ContentType.JSON)
                .body(Collections.singletonMap("name", "Lemon"))
                .when()
                .put("/0")
                .then()
                .statusCode(404);
    }

    @Test
    public void testPutWithWrongPayload() {
        Fruit apple = fruitRepository.save(new Fruit("Apple"));
        requestSpecification()
                .contentType(ContentType.JSON)
                .body(Collections.singletonMap("id", 0))
                .when()
                .put(String.valueOf(apple.getId()))
                .then()
                .statusCode(422);
    }

    @Test
    public void testPutWithNonJsonPayload() {
        Fruit apple = fruitRepository.save(new Fruit("Apple"));
        requestSpecification()
                .contentType(ContentType.XML)
                .when()
                .put(String.valueOf(apple.getId()))
                .then()
                .statusCode(415);
    }

    @Test
    public void testPutWithEmptyPayload() {
        Fruit apple = fruitRepository.save(new Fruit("Apple"));
        requestSpecification()
                .contentType(ContentType.JSON)
                .when()
                .put(String.valueOf(apple.getId()))
                .then()
                .statusCode(415);
    }

    @Test
    public void testDelete() {
        Fruit apple = fruitRepository.save(new Fruit("Apple"));
        requestSpecification()
                .delete(String.valueOf(apple.getId()))
                .then()
                .statusCode(204);
        assertFalse(fruitRepository.exists(apple.getId()));
    }

    @Test
    public void testDeleteNotExisting() {
        requestSpecification()
                .delete("/0")
                .then()
                .statusCode(404);
    }


    private RequestSpecification requestSpecification() {
        return given().baseUri(String.format("http://localhost:%d/%s", port, FRUITS_PATH));
    }
}
