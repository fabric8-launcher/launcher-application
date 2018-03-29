package io.fabric8.launcher.web.endpoints.osio;

import java.io.File;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.websocket.ContainerProvider;
import javax.websocket.WebSocketContainer;
import javax.ws.rs.core.UriBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import io.fabric8.launcher.base.EnvironmentSupport;
import io.fabric8.launcher.base.http.ExternalRequest;
import io.fabric8.launcher.base.identity.IdentityFactory;
import io.fabric8.launcher.base.identity.TokenIdentity;
import io.fabric8.launcher.osio.OsioConfigs;
import io.fabric8.launcher.osio.client.api.OsioWitClient;
import io.fabric8.launcher.osio.client.api.Space;
import io.fabric8.launcher.osio.client.api.Tenant;
import io.fabric8.launcher.osio.client.impl.OsioWitClientImpl;
import io.fabric8.launcher.service.git.GitHelper;
import io.fabric8.launcher.service.git.api.GitRepository;
import io.fabric8.launcher.service.git.api.NoSuchRepositoryException;
import io.fabric8.launcher.service.git.github.KohsukeGitHubServiceFactory;
import io.fabric8.launcher.service.git.spi.GitServiceSpi;
import io.fabric8.launcher.service.openshift.impl.OpenShiftClusterRegistryImpl;
import io.fabric8.launcher.service.openshift.impl.fabric8.openshift.client.Fabric8OpenShiftServiceFactory;
import io.fabric8.launcher.service.openshift.spi.OpenShiftServiceSpi;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.response.ResponseBodyExtractionOptions;
import io.restassured.specification.RequestSpecification;
import okhttp3.Request;
import org.hamcrest.core.IsNull;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import static io.fabric8.launcher.base.identity.IdentityHelper.createRequestAuthorizationHeaderValue;
import static io.restassured.RestAssured.given;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Arquillian.class)
@RunAsClient
public class OsioEndpointIT {

    private static final Logger LOG = Logger.getLogger(OsioEndpointIT.class.getName());

    private static final String TEST_ID = randomAlphanumeric(5).toLowerCase();

    private static final String LAUNCH_PROJECT_NAME = "project-osio-it-launch-" + TEST_ID;
    private static final String LAUNCH_MISSION = "rest-http";
    private static final String LAUNCH_RUNTIME = "vert.x";
    private static final String LAUNCH_RUNTIME_VERSION = "community";

    private static final String IMPORT_PROJECT_NAME = "project-osio-it-import-" + TEST_ID;

    private static final String LAUNCHER_OSIO_TOKEN = "LAUNCHER_OSIO_TOKEN";
    private static final String LAUNCHER_OSIO_SPACE = "LAUNCHER_OSIO_SPACE";

    private final List<String> repositoryToClean = new ArrayList<>();
    private final List<String> projectToClean = new ArrayList<>();

    @Rule
    public final TemporaryFolder tmpFolder = new TemporaryFolder();

    @ArquillianResource
    protected URI deploymentUri;

    @Deployment(testable = false)
    public static Archive<?> createDeployment() {
        return ShrinkWrap.createFromZipFile(WebArchive.class, new File("target/launcher-backend.war"));
    }

    @Test
    public void shouldLaunch() throws Exception {
        //When: calling launch endpoints
        ImmutableMap<String, String> params = ImmutableMap.<String, String>builder()
                .put("missionId", LAUNCH_MISSION)
                .put("runtimeId", LAUNCH_RUNTIME)
                .put("runtimeVersion", LAUNCH_RUNTIME_VERSION)
                .put("pipelineId", "maven-release")
                .put("projectName", LAUNCH_PROJECT_NAME)
                .put("projectVersion", "1.0.0")
                .put("groupId", "io.fabric8.launcher.osio.it")
                .put("artifactId", LAUNCH_PROJECT_NAME)
                .put("spaceId", getOsioSpace().getId())
                .put("gitRepository", LAUNCH_PROJECT_NAME)
                .build();
        ResponseBodyExtractionOptions validatableResponse = given()
                .spec(configureOsioEndpoint())
                .headers(createLaunchHeaders())
                .formParams(params)
                .when()
                .post("/launch")
                .then()
                .log().all()
                .assertThat()
                .statusCode(200)
                .body("uuid_link", IsNull.notNullValue())
                .extract()
                .body();

        //Then: we receive a status link
        String uuidLink = validatableResponse.jsonPath()
                .get("uuid_link").toString();

        //When: we listen for success status
        CountDownLatch successLatch = getSuccessLatch(uuidLink);
        successLatch.await(30, TimeUnit.SECONDS);
        assertThat(successLatch.getCount())
                //Then
                .as("The process terminated correctly.")
                .isZero();
        repositoryToClean.add(LAUNCH_PROJECT_NAME);
        projectToClean.add(LAUNCH_PROJECT_NAME);
    }

    @Test
    public void shouldImport() throws Exception {
        //Given: an existing git repository with a a README.md file
        final GitRepository createdRepo = getGitService().createRepository(IMPORT_PROJECT_NAME, "integration test repository for osio import");
        repositoryToClean.add(IMPORT_PROJECT_NAME);
        final Path tempDirectory = tmpFolder.getRoot().toPath();
        final String readmeFileName = "README.md";
        final Path file = tmpFolder.newFile(readmeFileName).toPath();
        final String readmeContent = "Read me to know more";
        Files.write(file, singletonList(readmeContent), Charset.forName("UTF-8"));
        getGitService().push(createdRepo, tempDirectory);


        //When: calling import endpoint
        ImmutableMap<String, String> params = ImmutableMap.<String, String>builder()
                .put("pipelineId", "maven-release")
                .put("projectName", IMPORT_PROJECT_NAME)
                .put("spaceId", getOsioSpace().getId())
                .put("gitRepository", IMPORT_PROJECT_NAME)
                .build();
        ResponseBodyExtractionOptions validatableResponse = given()
                .spec(configureOsioEndpoint())
                .headers(createLaunchHeaders())
                .formParams(params)
                .when()
                .post("/import")
                .then()
                .log().all()
                .assertThat()
                .statusCode(200)
                .body("uuid_link", IsNull.notNullValue())
                .extract()
                .body();

        //Then: we receive a status link
        String uuidLink = validatableResponse.jsonPath()
                .get("uuid_link").toString();

        //When: we listen for success status
        CountDownLatch successLatch = getSuccessLatch(uuidLink);
        successLatch.await(30, TimeUnit.SECONDS);
        assertThat(successLatch.getCount())
                //Then
                .as("The process terminated correctly.")
                .isZero();
        projectToClean.add(IMPORT_PROJECT_NAME);
    }

    private Map<String, String> createLaunchHeaders() {
        return ImmutableMap.<String, String>builder()
                .put("Authorization", "Bearer " + getOsioIdentity().getToken())
                .put("X-App", "osio")
                .put("X-Git-Provider", "GitHub")
                .build();
    }

    @Before
    public void waitUntilEndpointIsReady() {
        given()
                .spec(configureCatalogEndpoint())
                .when()
                .get("/wait")
                .then()
                .assertThat().statusCode(200);

    }

    @After
    public void cleanUp() {
        final String gitOwner = getGitService().getLoggedUser().getLogin();
        final String defaultNamespace = getDefaultNamespace();
        repositoryToClean.forEach(r -> {
            LOG.info("Going to cleanup repository: " + r);
            final String fullName = GitHelper.createGitRepositoryFullName(gitOwner, r);
            try {
                getGitService().deleteRepository(fullName);
                LOG.info("Deleted GitHub repository: " + fullName);
            } catch (final NoSuchRepositoryException nsre) {
                LOG.severe("Could not remove GitHub repo " + fullName + ": " + nsre.getMessage());
            }
        }) ;
        projectToClean.forEach(p -> {
            LOG.info("Going to cleanup project: " + p);
            getOpenShiftService().deleteBuildConfig(defaultNamespace, p);
            LOG.info("Deleted build config in namespace " + defaultNamespace + " named " + p);
        });
        getOpenShiftService().deleteConfigMap(defaultNamespace, gitOwner);
        LOG.info("Deleted jenkins config map in namespace " + defaultNamespace + " named " + gitOwner);
    }

    private String getDefaultNamespace(){
        final Tenant tenant = getTenant();
        return tenant.getDefaultUserNamespace().getName();
    }



    private RequestSpecification configureOsioEndpoint() {
        return new RequestSpecBuilder().setBaseUri(UriBuilder.fromUri(deploymentUri).path("api").path("/osio").build()).build();
    }

    private RequestSpecification configureCatalogEndpoint() {
        return new RequestSpecBuilder().setBaseUri(UriBuilder.fromUri(deploymentUri).path("api").path("booster-catalog").build()).build();
    }

    private CountDownLatch getSuccessLatch(final String statusLink) throws Exception {
        final OsioStatusClientEndpoint clientEndpoint = new OsioStatusClientEndpoint();
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        URI uri = UriBuilder.fromUri(deploymentUri).scheme("ws").path(statusLink).build();
        LOG.info("status websocket URI is: " + uri.toString());
        container.connectToServer(clientEndpoint, uri);
        LOG.info("waiting websocket for success...");
        return clientEndpoint.getLatch();
    }

    OpenShiftServiceSpi getOpenShiftService() {
        return new Fabric8OpenShiftServiceFactory(new OpenShiftClusterRegistryImpl()).create(OsioConfigs.getOpenShiftCluster(), getOsioIdentity());
    }

    GitServiceSpi getGitService() {
        return (GitServiceSpi) new KohsukeGitHubServiceFactory().create();
    }

    private static TokenIdentity getOsioIdentity() {
        return IdentityFactory.createFromToken(EnvironmentSupport.INSTANCE.getRequiredEnvVarOrSysProp(LAUNCHER_OSIO_TOKEN));
    }

    private static OsioWitClient getWitClient() {
        return new OsioWitClientImpl(createRequestAuthorizationHeaderValue(getOsioIdentity()));
    }

    private static Tenant getTenant() {
        return getWitClient().getTenant();
    }

    private static Space getOsioSpace() {
        String spaceName = EnvironmentSupport.INSTANCE.getRequiredEnvVarOrSysProp(LAUNCHER_OSIO_SPACE);
        return getWitClient().findSpaceByName(getTenant().getUserInfo().getUsername(), spaceName);
    }
}
