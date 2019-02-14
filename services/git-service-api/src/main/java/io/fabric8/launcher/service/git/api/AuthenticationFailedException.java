package io.fabric8.launcher.service.git.api;

/**
 * Thrown if a {@link GitServiceFactory} fails authentication when creating a {@link GitService}
 */
public class AuthenticationFailedException extends RuntimeException {

    public AuthenticationFailedException(String message) {
        super(message);
    }

    public AuthenticationFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
