package io.fabric8.launcher.web.endpoints.osio;

import java.io.File;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.websocket.ContainerProvider;
import javax.websocket.WebSocketContainer;
import javax.ws.rs.core.UriBuilder;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.fabric8.launcher.osio.OsioConfigs;
import io.fabric8.launcher.osio.client.Space;
import io.fabric8.launcher.osio.client.Tenant;
import io.fabric8.launcher.service.git.Gits;
import io.fabric8.launcher.service.git.api.GitRepository;
import io.fabric8.launcher.service.git.github.KohsukeGitHubServiceFactory;
import io.fabric8.launcher.service.git.spi.GitServiceSpi;
import io.fabric8.launcher.service.openshift.impl.OpenShiftClusterRegistryImpl;
import io.fabric8.launcher.service.openshift.impl.Fabric8OpenShiftServiceFactory;
import io.fabric8.launcher.service.openshift.spi.OpenShiftServiceSpi;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.response.ResponseBodyExtractionOptions;
import io.restassured.specification.RequestSpecification;
import org.hamcrest.core.IsNull;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import static io.fabric8.launcher.web.endpoints.osio.OsioTests.getOsioIdentity;
import static io.fabric8.launcher.web.endpoints.osio.OsioTests.getWitClient;
import static io.restassured.RestAssured.given;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Arquillian.class)
@RunAsClient
public class OsioEndpointIT {

    private static final Logger LOG = Logger.getLogger(OsioEndpointIT.class.getName());

    private static final String TEST_ID = randomAlphanumeric(5).toLowerCase();

    private static final String SPACE_NAME = "space-osio-it-" + TEST_ID;

    private static final String LAUNCH_PROJECT_NAME = "project-osio-it-launch-" + TEST_ID;
    private static final String LAUNCH_MISSION = "rest-http";
    private static final String LAUNCH_RUNTIME = "vert.x";
    private static final String LAUNCH_RUNTIME_VERSION = "community";

    private static final String IMPORT_PROJECT_NAME = "project-osio-it-import-" + TEST_ID;



    private static final List<String> REPOSITORY_TO_CLEAN = ImmutableList.of(LAUNCH_PROJECT_NAME, IMPORT_PROJECT_NAME);
    private static final List<String> PROJECT_TO_CLEAN = ImmutableList.of(LAUNCH_PROJECT_NAME, IMPORT_PROJECT_NAME);

    @Rule
    public final TemporaryFolder tmpFolder = new TemporaryFolder();

    @ArquillianResource
    protected URI deploymentUri;

    private static Space space;

    @Deployment(testable = false)
    public static Archive<?> createDeployment() {
        return ShrinkWrap.createFromZipFile(WebArchive.class, new File("target/launcher-backend.war"));
    }

    @Test
    public void shouldLaunch() throws Exception {
        //When: calling launch endpoints
        ImmutableMap<String, String> params = ImmutableMap.<String, String>builder()
                .put("mission", LAUNCH_MISSION)
                .put("runtime", LAUNCH_RUNTIME)
                .put("runtimeVersion", LAUNCH_RUNTIME_VERSION)
                .put("pipeline", "maven-release")
                .put("projectName", LAUNCH_PROJECT_NAME)
                .put("projectVersion", "1.0.0")
                .put("groupId", "io.fabric8.launcher.osio.it")
                .put("artifactId", LAUNCH_PROJECT_NAME)
                .put("space", space.getId())
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

    }

    @Test
    public void shouldImport() throws Exception {
        //Given: an existing git repository with a a README.md file
        final GitRepository createdRepo = getGitService().createRepository(IMPORT_PROJECT_NAME, "integration test repository for osio import");
        final Path tempDirectory = tmpFolder.getRoot().toPath();
        final String readmeFileName = "README.md";
        final Path file = tmpFolder.newFile(readmeFileName).toPath();
        final String readmeContent = "Read me to know more";
        Files.write(file, singletonList(readmeContent), Charset.forName("UTF-8"));
        getGitService().push(createdRepo, tempDirectory);


        //When: calling import endpoint
        ImmutableMap<String, String> params = ImmutableMap.<String, String>builder()
                .put("pipeline", "maven-release")
                .put("projectName", IMPORT_PROJECT_NAME)
                .put("space", space.getId())
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

    @BeforeClass
    public static void createSpace() {
        space = getWitClient().createSpace(SPACE_NAME);
        LOG.info("Created space with name: " + space.getName() + " and id: " + space.getId());
    }

    @AfterClass
    public static void cleanUp() {
        final String gitOwner = getGitService().getLoggedUser().getLogin();
        final String defaultNamespace = getDefaultNamespace();
        REPOSITORY_TO_CLEAN.forEach(r -> {
            LOG.info("Going to cleanup repository: " + r);
            final String fullName = Gits.createGitRepositoryFullName(gitOwner, r);
            try {
                getGitService().deleteRepository(fullName);
                LOG.info("Deleted GitHub repository: " + fullName);
            } catch (final Exception e) {
                LOG.severe("Could not remove GitHub repo " + fullName + ": " + e.getMessage());
            }
        }) ;
        PROJECT_TO_CLEAN.forEach(p -> {
            try {
                LOG.info("Going to cleanup project: " + p);
                getOpenShiftService().deleteBuildConfig(defaultNamespace, p);
                LOG.info("Deleted build config in namespace " + defaultNamespace + " named " + p);
            } catch (final Exception e) {
                LOG.severe("Could not delete build config in namespace " + defaultNamespace + " named " + p);
            }
        });
        try {
            getOpenShiftService().deleteConfigMap(defaultNamespace, gitOwner);
            LOG.info("Deleted jenkins config map in namespace " + defaultNamespace + " named " + gitOwner);
        } catch (final Exception e) {
            LOG.severe("Could not delete jenkins config map in namespace " + defaultNamespace + " named " + gitOwner);
        }
        try {
            assertThat(getWitClient().deleteSpace(space.getId())).isTrue();
            LOG.info("Deleted space " + space.getId() + " named " + space.getName());
        } catch (final Exception e) {
            LOG.severe("Could not delete space " + space.getId() + " named " + space.getName());
        }
    }

    private static String getDefaultNamespace(){
        final Tenant tenant = getWitClient().getTenant();
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

    static OpenShiftServiceSpi getOpenShiftService() {
        return new Fabric8OpenShiftServiceFactory(new OpenShiftClusterRegistryImpl()).create(OsioConfigs.getOpenShiftCluster(), getOsioIdentity());
    }

    static GitServiceSpi getGitService() {
        return (GitServiceSpi) new KohsukeGitHubServiceFactory().create();
    }

}
