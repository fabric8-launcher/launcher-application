package io.fabric8.launcher.web.providers;

import javax.json.JsonArrayBuilder;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Provider
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        JsonArrayBuilder arrayBuilder = createArrayBuilder();
        for (ConstraintViolation violation : exception.getConstraintViolations()) {
            String path = violation.getPropertyPath().toString();
            int idx = path.lastIndexOf('.');
            if (idx > -1) {
                path = path.substring(idx + 1);
            }
            arrayBuilder.add(createObjectBuilder()
                                     .add("message", violation.getMessage())
                                     .add("source", path));
        }
        return Response.status(Response.Status.BAD_REQUEST)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .entity(arrayBuilder.build()).build();
    }
}
