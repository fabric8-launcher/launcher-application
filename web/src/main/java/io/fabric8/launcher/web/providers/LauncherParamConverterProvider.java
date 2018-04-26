package io.fabric8.launcher.web.providers;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import javax.ws.rs.ext.Provider;

import io.fabric8.launcher.booster.catalog.rhoar.Mission;
import io.fabric8.launcher.booster.catalog.rhoar.Runtime;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Provider
@ApplicationScoped
public class LauncherParamConverterProvider implements ParamConverterProvider {

    // Cannot use constructor-type injection (gives NPE in CdiInjectorFactory)
    @Inject
    private MissionParamConverter missionParamConverter;

    // Cannot use constructor-type injection (gives NPE in CdiInjectorFactory)
    @Inject
    private RuntimeParamConverter runtimeParamConverter;

    @Override
    @SuppressWarnings("unchecked")
    public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {
        ParamConverter<T> result = null;
        if (rawType == Mission.class) {
            result = (ParamConverter<T>) missionParamConverter;
        } else if (rawType == Runtime.class) {
            result = (ParamConverter<T>) runtimeParamConverter;
        }
        return result;
    }
}