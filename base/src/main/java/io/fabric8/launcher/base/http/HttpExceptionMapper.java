package io.fabric8.launcher.base.http;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Provider
public class HttpExceptionMapper implements ExceptionMapper<HttpException> {
    @Override
    public Response toResponse(HttpException exception) {
        return Response.status(exception.getStatusCode()).build();
    }
}
