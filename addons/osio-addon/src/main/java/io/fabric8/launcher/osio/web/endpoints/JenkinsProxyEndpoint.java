package io.fabric8.launcher.osio.web.endpoints;

import java.net.URL;
import java.util.Objects;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import io.fabric8.launcher.base.http.HttpClient;
import io.fabric8.launcher.base.http.HttpException;
import io.fabric8.launcher.base.identity.Identity;
import io.fabric8.launcher.core.api.security.Secured;
import io.fabric8.launcher.core.spi.Application;
import io.fabric8.launcher.osio.client.OsioIdentityProvider;
import io.fabric8.launcher.osio.client.Tenant;
import io.fabric8.launcher.service.openshift.api.OpenShiftProject;
import io.fabric8.launcher.service.openshift.api.OpenShiftService;
import okhttp3.Request;
import okhttp3.RequestBody;

import static io.fabric8.kubernetes.client.utils.URLUtils.pathJoin;
import static io.fabric8.launcher.base.http.Requests.securedRequest;
import static io.fabric8.launcher.core.spi.Application.ApplicationType.OSIO;

/**
 * Proxies Requests to the OSIO Jenkins.
 * This code will be replaced in the future by Build.next
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Path("/openshift/services/jenkins")
@RequestScoped
public class JenkinsProxyEndpoint {

    @Inject
    @Application(OSIO)
    private OpenShiftService openShiftService;

    @Inject
    @Application(OSIO)
    private OsioIdentityProvider identityProvider;

    @Inject
    private Tenant tenant;

    @Inject
    private HttpClient httpClient;

    @Context
    HttpHeaders headers;

    @Context
    UriInfo uriInfo;

    @GET
    @Path("/{namespace}/{path: .*}")
    @Secured
    public Response jenkins(
            @PathParam("namespace") String namespace,
            @PathParam("path") String path) {
        String serviceName = "jenkins";
        return proxyRequest(namespace, path, serviceName, "GET", null);

    }

    @POST
    @Path("/{namespace}/{path: .*}")
    @Secured
    public Response jenkinsPost(
            @PathParam("namespace") String namespace,
            @PathParam("path") String path,
            String body) {
        String serviceName = "jenkins";
        return proxyRequest(namespace, path, serviceName, "POST", body);
    }

    private Response proxyRequest(String namespace, String path, String serviceName, String method, String body) {
        OpenShiftProject project = openShiftService.findProject(namespace)
                .orElseThrow(() -> new IllegalStateException("OpenShift Project '" + namespace + "' cannot be found"));

        URL serviceURL = openShiftService.getServiceURL(serviceName, project);
        String query = uriInfo.getRequestUri().getQuery();
        StringBuilder fullUrl = new StringBuilder(pathJoin(serviceURL.toExternalForm(), path));
        if (query != null && !query.isEmpty()) {
            fullUrl.append("?").append(query);
        }

        Request.Builder builder = request()
                .url(fullUrl.toString())
                .method(method, RequestBody.create(null, Objects.toString(body, "")));

        httpClient.executeAndConsume(builder.build(), response -> {
            if (!response.isSuccessful()) {
                throw new HttpException(response.code(), response.message());
            }
        });
        return Response.ok().build();
    }


    private Request.Builder request() {
        // openshift.io#2034: Jenkins requires OSO authorization
        Identity osoToken = identityProvider.getIdentity(tenant.getIdentity(), tenant.getUserInfo().getCluster())
                .orElseThrow(() -> new IllegalStateException("OSO Token not found"));
        return securedRequest(osoToken);
    }

}