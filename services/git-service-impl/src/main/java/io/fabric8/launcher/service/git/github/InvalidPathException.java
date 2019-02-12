package io.fabric8.launcher.service.git.github;

import java.net.URISyntaxException;

/**
 * Exception thrown when the path to a repository on GitHub is invalid
 */
class InvalidPathException extends IllegalArgumentException {

    /**
     * Constructor.
     *
     * @param message the exception message
     * @param cause   the underlying cause
     */
    InvalidPathException(String message, URISyntaxException cause) {
        super(message, cause);
    }

    private static final long serialVersionUID = 450790118418275921L;


}
