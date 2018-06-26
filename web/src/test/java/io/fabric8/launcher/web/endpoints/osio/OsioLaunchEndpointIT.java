package io.fabric8.launcher.web.endpoints.osio;

import java.io.File;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.fabric8.launcher.base.http.HttpClient;
import io.fabric8.launcher.osio.OsioConfigs;
import io.fabric8.launcher.osio.client.Space;
import io.fabric8.launcher.osio.client.Tenant;
import io.fabric8.launcher.service.git.Gits;
import io.fabric8.launcher.service.git.api.GitRepository;
import io.fabric8.launcher.service.git.github.KohsukeGitHubServiceFactory;
import io.fabric8.launcher.service.git.spi.GitServiceSpi;
import io.fabric8.launcher.service.openshift.impl.Fabric8OpenShiftServiceFactory;
import io.fabric8.launcher.service.openshift.impl.OpenShiftClusterRegistryImpl;
import io.fabric8.launcher.service.openshift.spi.OpenShiftServiceSpi;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.response.ResponseBodyExtractionOptions;
import io.restassured.specification.RequestSpecification;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import org.hamcrest.core.IsNull;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
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
public class OsioLaunchEndpointIT {

    private static final Logger LOG = Logger.getLogger(OsioLaunchEndpointIT.class.getName());

    private static final String TEST_ID = randomAlphanumeric(5).toLowerCase();

    private static final String SPACE_NAME = "space-osio-it-" + TEST_ID;

    private static final String LAUNCH_PROJECT_NAME = "project-osio-it-launch-" + TEST_ID;

    private static final String LAUNCH_EMPTY_PROJECT_NAME = "project-osio-it-launch-empty-" + TEST_ID;


    private static final String LAUNCH_MISSION = "rest-http";

    private static final String LAUNCH_RUNTIME = "vert.x";

    private static final String LAUNCH_RUNTIME_VERSION = "community";


    private static final List<String> REPOSITORY_TO_CLEAN = Arrays.asList(LAUNCH_PROJECT_NAME, LAUNCH_EMPTY_PROJECT_NAME);

    private static final List<String> PROJECT_TO_CLEAN = Arrays.asList(LAUNCH_PROJECT_NAME, LAUNCH_EMPTY_PROJECT_NAME);

    @Rule
    public final TemporaryFolder tmpFolder = new TemporaryFolder();

    @ArquillianResource
    private URI deploymentUri;

    private static Space space;
    private final OsioStatusClientEndpoint clientEndpoint = new OsioStatusClientEndpoint();


    @Deployment(testable = false)
    public static Archive<?> createDeployment() {
        return ShrinkWrap.createFromZipFile(WebArchive.class, new File("target/launcher-backend.war"));
    }

    @Test
    public void should_launch() throws Exception {
        //When: calling launch endpoints
        Map<String, String> params = new HashMap<>();
        params.put("mission", LAUNCH_MISSION);
        params.put("runtime", LAUNCH_RUNTIME);
        params.put("runtimeVersion", LAUNCH_RUNTIME_VERSION);
        params.put("pipeline", "maven-release");
        params.put("projectName", LAUNCH_PROJECT_NAME);
        params.put("projectVersion", "1.0.0");
        params.put("groupId", "io.fabric8.launcher.osio.it");
        params.put("artifactId", LAUNCH_PROJECT_NAME);
        params.put("space", space.getId());
        params.put("gitRepository", LAUNCH_PROJECT_NAME);

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
        assertThat(clientEndpoint.isGithubPushed()).isTrue();
    }

    @Test
    public void should_launch_analytics() throws Exception {
        //When: calling launch endpoints
        Map<String, String> params = new HashMap<>();
        params.put("mission", LAUNCH_MISSION);
        params.put("runtime", LAUNCH_RUNTIME);
        params.put("runtimeVersion", LAUNCH_RUNTIME_VERSION);
        params.put("pipeline", "maven-release");
        params.put("projectName", LAUNCH_EMPTY_PROJECT_NAME);
        params.put("projectVersion", "1.0.0");
        params.put("groupId", "io.fabric8.launcher.osio.it");
        params.put("artifactId", LAUNCH_EMPTY_PROJECT_NAME);
        params.put("space", space.getId());
        params.put("gitRepository", LAUNCH_EMPTY_PROJECT_NAME);
        params.put("emptyGitRepository", "true");

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
        assertThat(clientEndpoint.isGithubPushed()).isTrue();
    }

    private Map<String, String> createLaunchHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + getOsioIdentity().getToken());
        headers.put("X-App", "osio");
        headers.put("X-Git-Provider", "GitHub");
        return headers;
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
        });
        PROJECT_TO_CLEAN.forEach(p -> {
            try {
                LOG.info("Going to cleanup project: " + p);
                getOpenShiftService().deleteBuildConfig(defaultNamespace, p);
                LOG.info("Deleted build config in namespace " + defaultNamespace + " named " + p);
            } catch (final Exception e) {
                LOG.log(Level.SEVERE, "Could not delete build config in namespace " + defaultNamespace + " named " + p, e);
            }
        });
        try {
            getOpenShiftService().deleteConfigMap(defaultNamespace, gitOwner);
            LOG.info("Deleted jenkins config map in namespace " + defaultNamespace + " named " + gitOwner);
        } catch (final Exception e) {
            LOG.log(Level.SEVERE, "Could not delete jenkins config map in namespace " + defaultNamespace + " named " + gitOwner, e);
        }
        try {
            getWitClient().deleteSpace(space.getId());
            LOG.info("Deleted space " + space.getId() + " named " + space.getName());
        } catch (final Exception e) {
            LOG.log(Level.SEVERE, "Could not delete space " + space.getId() + " named " + space.getName(), e);
        }
    }

    private static String getDefaultNamespace() {
        final Tenant tenant = getWitClient().getTenant();
        return tenant.getDefaultUserNamespace().getName();
    }

    private RequestSpecification configureOsioEndpoint() {
        return new RequestSpecBuilder().setBaseUri(deploymentUri + "api/osio").build();
    }

    private RequestSpecification configureCatalogEndpoint() {
        return new RequestSpecBuilder().setBaseUri(deploymentUri + "api/booster-catalog").build();
    }

    private WebSocket webSocket;

    private CountDownLatch getSuccessLatch(final String statusLink) {
        OkHttpClient client = HttpClient.create().getClient();
        HttpUrl httpUrl = HttpUrl.get(deploymentUri).newBuilder(statusLink).build();

        webSocket = client.newWebSocket(new Request.Builder().url(httpUrl).build(), new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                clientEndpoint.onOpen();
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                clientEndpoint.onMessage(text);
            }
        });
        return clientEndpoint.getLatch();
    }

    @After
    public void closeWebSocket() {
        if (webSocket != null) {
            webSocket.cancel();
            webSocket.close(1000, null);
        }
    }

    private static OpenShiftServiceSpi getOpenShiftService() {
        return new Fabric8OpenShiftServiceFactory(new OpenShiftClusterRegistryImpl()).create(OsioConfigs.getOpenShiftCluster(), getOsioIdentity());
    }

    private static GitServiceSpi getGitService() {
        return (GitServiceSpi) new KohsukeGitHubServiceFactory().create();
    }

}
