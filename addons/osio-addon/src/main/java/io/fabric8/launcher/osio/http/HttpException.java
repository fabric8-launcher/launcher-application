package io.fabric8.launcher.osio.http;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class HttpException extends IllegalStateException {

    private int statusCode;

    public HttpException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public HttpException(String message, Throwable cause) {
        super(message, cause);
    }

    public int getStatusCode() {
        return statusCode;
    }
}

