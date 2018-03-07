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

import javax.ws.rs.core.UriBuilder;

import io.fabric8.launcher.web.BaseResourceIT;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.hasItem;

@RunWith(Arquillian.class)
@RunAsClient
public class GitEndpointIT extends BaseResourceIT {


    private RequestSpecification configureEndpoint() {
        return new RequestSpecBuilder().setBaseUri(UriBuilder.fromUri(deploymentUri).path("api").path("/services/git").build()).build();
    }

    @Test
    public void shouldRespondWithSupportedGitProviders() {
        given()
                .spec(configureEndpoint())
        .when()
                .get("/providers")
        .then()
                .assertThat().statusCode(200)
                .body(".", hasItem("GITHUB"));

    }
}
