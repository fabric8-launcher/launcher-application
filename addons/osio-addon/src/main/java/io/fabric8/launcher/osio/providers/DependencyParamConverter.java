package io.fabric8.launcher.osio.providers;

import org.apache.maven.model.Dependency;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ParamConverter;

import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;

@ApplicationScoped
public class DependencyParamConverter implements ParamConverter<Dependency> {

    @Override
    public Dependency fromString(final String dep) {
        Dependency dependency = new Dependency();
        try {
            String[] groupIdArtifactIdVersion = dep.split(":");
            dependency.setGroupId(groupIdArtifactIdVersion[0]);
            dependency.setArtifactId(groupIdArtifactIdVersion[1]);
            dependency.setVersion(groupIdArtifactIdVersion[2]);
            return dependency;
        } catch (Exception e) {
            Response response = Response.status(Response.Status.BAD_REQUEST)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                    .entity(createArrayBuilder()
                            .add(createObjectBuilder().add("message", "Dependency is required in the format `groupId:artifactId:version`"))
                            .build())
                    .build();
            throw new WebApplicationException(response);
        }
    }

    @Override
    public String toString(final Dependency dep) {
        return dep.toString();
    }
}
