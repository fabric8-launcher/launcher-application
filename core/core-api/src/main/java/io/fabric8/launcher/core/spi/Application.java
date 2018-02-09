package io.fabric8.launcher.core.spi;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

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

    enum ApplicationType {
        LAUNCHER,
        OSIO
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
