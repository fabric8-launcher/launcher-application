package io.fabric8.launcher.core.spi;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Qualifier;

import static java.lang.annotation.ElementType.TYPE;

/**
 * Qualifies services belonging to different applications
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target(TYPE)
public @interface Application {

    String value();

    final class ApplicationLiteral extends AnnotationLiteral<Application> implements Application {

        private static final long serialVersionUID = 1L;

        private final String value;

        public static ApplicationLiteral of(String value) {
            return new ApplicationLiteral(value);
        }

        public String value() {
            return value;
        }

        private ApplicationLiteral(String value) {
            this.value = value;
        }

    }

}
