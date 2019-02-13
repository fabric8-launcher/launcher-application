/**
 * Copyright 2005-2015 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version
 * 2.0 (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */
package io.fabric8.launcher.web.endpoints;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;

@QuarkusTest
public class BoosterCatalogEndpointIT {

    @BeforeEach
    void waitUntilEndpointIsReady() {
        given()
                .when()
                .get("/api/booster-catalog/wait")
                .then()
                .assertThat().statusCode(200);
    }

    @Test
    void shouldRespondWithCatalog() {
        given()
                .when()
                .get("/api/booster-catalog")
                .then()
                .assertThat().statusCode(200)
                .body("boosters", not(empty()))
                .body("runtimes", not(empty()))
                .body("missions", not(empty()));
    }

    @Test
    void reindexShouldBeSequential() {
        given()
                .when()
                .contentType("application/json")
                .post("/api/booster-catalog/reindex")
                .then()
                .assertThat().statusCode(200);
        // Second request should return 304 until the reindex completes
        given()
                .when()
                .contentType("application/json")
                .post("/api/booster-catalog/reindex")
                .then()
                .assertThat().statusCode(304);

        waitUntilEndpointIsReady();

        given()
                .when()
                .contentType("application/json")
                .post("/api/booster-catalog/reindex")
                .then()
                .assertThat().statusCode(200);

    }


}
