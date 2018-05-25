package io.fabric8.launcher.osio.providers;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import javax.ws.rs.ext.Provider;

import org.apache.maven.model.Dependency;

@Provider
@ApplicationScoped
public class DependencyParamConverterProvider implements ParamConverterProvider {
    @Inject
    private DependencyParamConverter dependencyParamConverter;

    @Override
    @SuppressWarnings("unchecked")
    public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {
        if (rawType == Dependency.class) {
            return (ParamConverter<T>) dependencyParamConverter;
        }
        return null;
    }
}
