package io.fabric8.launcher.web.cdi;

import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Named;

import io.fabric8.launcher.web.providers.GitServiceProducer;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */ // Remove when we update to CDI 2.0
public final class NamedLiteral extends AnnotationLiteral<Named> implements Named {

    private static final long serialVersionUID = 1L;

    private final String value;

    public static NamedLiteral of(String value) {
        return new NamedLiteral(value);
    }

    public String value() {
        return value;
    }

    private NamedLiteral(String value) {
        this.value = value;
    }

}
