package io.fabric8.launcher.core.api.security;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.ws.rs.NameBinding;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Used in JAX-RS endpoints to indicate that it must contain a valid Authorization header
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@NameBinding
@Retention(RUNTIME)
@Target({TYPE, METHOD})
public @interface Secured {
}