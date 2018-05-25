package io.fabric8.launcher.osio.providers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ParamConverter;

import org.apache.maven.model.Dependency;

import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;

@ApplicationScoped
public class DependencyParamConverter implements ParamConverter<Dependency> {

    private static final Pattern DEPENDENCY_PATTERN = Pattern
            .compile("([^: ]+):([^: ]+)(:([^: ]*)(:([^: ]+))?)?(:([^: ]+))?");

    @Override
    public Dependency fromString(final String dep) {
        Dependency dependency = new Dependency();
        try {
            final Matcher match = DEPENDENCY_PATTERN.matcher(dep);
            if (!match.matches()) {
                throw new IllegalArgumentException();
            }
            String[] groupIdArtifactIdVersion = dep.split(":");
            dependency.setGroupId(groupIdArtifactIdVersion[0]);
            dependency.setArtifactId(groupIdArtifactIdVersion[1]);
            if (groupIdArtifactIdVersion.length > 2) {
                dependency.setVersion(groupIdArtifactIdVersion[2]);
            }
            return dependency;
        } catch (Exception e) {
            Response response = Response.status(Response.Status.BAD_REQUEST)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                    .entity(createArrayBuilder()
                            .add(createObjectBuilder()
                                    .add("message", "Dependency is required in the format `<groupId>:<artifactId>:[version]` and got: " + dep))
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
