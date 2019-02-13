package io.fabric8.launcher.osio.jenkins;


import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;

@Disabled
public class JenkinsPipelineEndpointIT {

    @Test
    public void should_send_pipelines_response() {
        given()
                .when()
                .get("/api/services/jenkins/pipelines")
                .then()
                .assertThat().statusCode(200)
                .body("name[0]", is("Release and Stage"));

    }

    @Test
    public void should_send_jenkinsfile_response() {
        given()
                .when()
                .get("/api/services/jenkins/pipelines/maven-releasestageapproveandpromote/jenkinsfile")
                .then()
                .assertThat().statusCode(200)
                .body(containsString("#!/usr/bin/groovy"));

    }
}
