package io.fabric8.launcher.core.api.inject;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Qualifier;

import io.fabric8.launcher.core.api.StatusEventType;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * CDI qualifier for step number
 */
@Qualifier
@Retention(RUNTIME)
@Target({PARAMETER})
public @interface Step {
    StatusEventType value();

    final class Literal extends AnnotationLiteral<Step> implements Step {
        public Literal(StatusEventType statusEventType) {
            this.statusEventType = statusEventType;
        }

        private static final long serialVersionUID = 1L;

        private final StatusEventType statusEventType;

        @Override
        public StatusEventType value() {
            return statusEventType;
        }
    }
}
