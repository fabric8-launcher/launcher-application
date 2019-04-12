package io.fabric8.launcher.web.providers;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.databind.node.ArrayNode;
import io.fabric8.launcher.base.JsonUtils;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Provider
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        ArrayNode arrayNode = JsonUtils.createArrayNode();
        for (ConstraintViolation violation : exception.getConstraintViolations()) {
            String path = violation.getPropertyPath().toString();
            int idx = path.lastIndexOf('.');
            if (idx > -1) {
                path = path.substring(idx + 1);
            }
            arrayNode.addObject()
                .put("message", violation.getMessage())
                .put("source", path);
       }
        return Response.status(Response.Status.BAD_REQUEST)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .entity(arrayNode).build();
    }
}
