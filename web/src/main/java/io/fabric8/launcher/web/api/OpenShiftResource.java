package io.fabric8.launcher.web.api;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import io.fabric8.forge.generator.EnvironmentVariables;
import io.fabric8.launcher.base.identity.IdentityFactory;
import io.fabric8.launcher.service.keycloak.api.KeycloakService;
import io.fabric8.launcher.service.openshift.api.OpenShiftCluster;
import io.fabric8.launcher.service.openshift.api.OpenShiftClusterRegistry;
import io.fabric8.launcher.service.openshift.api.OpenShiftProject;
import io.fabric8.launcher.service.openshift.api.OpenShiftService;
import io.fabric8.launcher.service.openshift.api.OpenShiftServiceFactory;
import io.fabric8.utils.Strings;
import io.fabric8.utils.URLUtils;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Path(OpenShiftResource.PATH_RESOURCE)
@ApplicationScoped
public class OpenShiftResource {

    private static final String OPENSHIFT_API_URL = System.getenv(EnvironmentVariables.OPENSHIFT_API_URL);

    static final String PATH_RESOURCE = "/openshift";

    private static final Logger log = Logger.getLogger(OpenShiftResource.class.getName());


    @Inject
    private OpenShiftServiceFactory openShiftServiceFactory;

    @Inject
    private OpenShiftClusterRegistry clusterRegistry;

    @Inject
    private Instance<KeycloakService> keycloakServiceInstance;


    @GET
    @Path("/clusters")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonArray getSupportedOpenShiftClusters(@HeaderParam(HttpHeaders.AUTHORIZATION) final String authorization,
                                                   @Context HttpServletRequest request) {
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        Set<OpenShiftCluster> clusters = clusterRegistry.getClusters();
        if (request.getParameterMap().containsKey("all") || openShiftServiceFactory.getDefaultIdentity().isPresent()) {
            // TODO: Remove this since getAllOpenShiftClusters already does this
            // Return all clusters
            clusters
                    .stream()
                    .map(OpenShiftCluster::getId)
                    .forEach(arrayBuilder::add);
        } else {
            final KeycloakService keycloakService = this.keycloakServiceInstance.get();
            clusters.parallelStream().map(OpenShiftCluster::getId)
                    .forEach(clusterId ->
                                     keycloakService.getIdentity(clusterId, authorization)
                                             .ifPresent(token -> arrayBuilder.add(clusterId)));
        }

        return arrayBuilder.build();
    }

    @GET
    @Path("/clusters/all")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getAllOpenShiftClusters() {
        Set<OpenShiftCluster> clusters = clusterRegistry.getClusters();
        // Return all clusters
        return clusters.stream().map(OpenShiftCluster::getId).collect(Collectors.toList());
    }

    @GET
    @javax.ws.rs.Path("/services/jenkins/{namespace}/{path: .*}")
    public Response jenkins(
            @PathParam("namespace") String namespace,
            @PathParam("path") String path,
            @Context HttpHeaders headers,
            @Context UriInfo uriInfo) {
        String serviceName = "jenkins";
        return proxyRequest(namespace, path, headers, uriInfo, serviceName, "GET", null);

    }

    @POST
    @javax.ws.rs.Path("/services/jenkins/{namespace}/{path: .*}")
    public Response jenkinsPost(
            @PathParam("namespace") String namespace,
            @PathParam("path") String path,
            @Context HttpHeaders headers,
            @Context UriInfo uriInfo,
            String body) {
        String serviceName = "jenkins";
        return proxyRequest(namespace, path, headers, uriInfo, serviceName, "POST", body);
    }

    private Response proxyRequest(String namespace, String path,
                                  HttpHeaders headers, UriInfo uriInfo, String serviceName, String method, String body) {
        String authorization = headers.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (Strings.isNullOrBlank(authorization)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        String token = authorization;
        int idx = token.indexOf(' ');
        if (idx >= 0) {
            token = token.substring(idx + 1);
        }
        if (Strings.isNullOrBlank(token)) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Empty token").build();
        }
        OpenShiftCluster cluster = new OpenShiftCluster("openshift", OPENSHIFT_API_URL, OPENSHIFT_API_URL);
        OpenShiftService openShiftService = openShiftServiceFactory.create(cluster, IdentityFactory.createFromToken(token));
        OpenShiftProject project = openShiftService.findProject(namespace)
                .orElseThrow(() -> new IllegalStateException("OpenShift Project '" + namespace + "' cannot be found"));

        URL serviceURL = openShiftService.getServiceURL(serviceName, project);
        String query = uriInfo.getRequestUri().getQuery();
        String fullUrl = URLUtils.pathJoin(serviceURL.toExternalForm(), path);
        if (!Strings.isNullOrBlank(query)) {
            fullUrl += "?" + query;
        }

        log.info("Invoking " + method + " on " + fullUrl);

        HttpURLConnection connection = null;
        try {
            URL url = new URL(fullUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);
            MultivaluedMap<String, String> requestHeaders = headers.getRequestHeaders();
            for (Map.Entry<String, List<String>> entry : requestHeaders.entrySet()) {
                String headerName = entry.getKey();
                List<String> values = entry.getValue();
                if (values != null) {
                    for (String value : values) {
                        connection.setRequestProperty(headerName, value);
                    }
                }
            }
            if (body != null) {
                connection.setDoOutput(true);
                OutputStreamWriter out = new OutputStreamWriter(
                        connection.getOutputStream());
                out.write(body);

                out.close();
            }
            int status = connection.getResponseCode();
            String message = connection.getResponseMessage();
            log.info("Got response code from : " + status + " message: " + message);
            return Response.status(status).entity(connection.getInputStream()).build();
        } catch (Exception e) {
            log.log(Level.SEVERE, "Failed to invoke url " + fullUrl + ". " + e, e);
            return Response.serverError().entity("Failed to invoke " + fullUrl + " due to " + e).build();

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

}