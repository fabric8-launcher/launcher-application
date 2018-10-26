package io.fabric8.launcher.core.spi;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Objects;

import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Qualifier;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;

/**
 * Qualifies services belonging to different applications
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({TYPE, FIELD, PARAMETER, METHOD})
public @interface Application {

    String APP_HEADER = "X-App";

    enum ApplicationType {

        LAUNCHER,
        OSIO;

        /**
         * Converts X-App HTTP header to corresponding ApplicationType instance based on case insensitive name.
         * If header does not exist it defaults to LAUNCHER.
         *
         * @param value Header value to extract application name from
         * @return instance of ApplicationType
         * @throws IllegalArgumentException if the header has wrong application name
         */
        public static ApplicationType fromHeaderValue(String value) {
            // If X-App is not specified, assume fabric8-launcher
            final String app = Objects.toString(value, "launcher").toUpperCase();
            try {
                return valueOf(app);
            } catch (IllegalArgumentException iae) {
                throw new IllegalArgumentException("Unrecognized application. Header 'X-App' has an invalid value: " + app, iae);
            }
        }
    }

    ApplicationType value();

    final class ApplicationLiteral extends AnnotationLiteral<Application> implements Application {

        private static final long serialVersionUID = 1L;

        private final ApplicationType value;

        public static ApplicationLiteral of(ApplicationType value) {
            return new ApplicationLiteral(value);
        }

        public ApplicationType value() {
            return value;
        }

        private ApplicationLiteral(ApplicationType value) {
            this.value = value;
        }

    }


}
