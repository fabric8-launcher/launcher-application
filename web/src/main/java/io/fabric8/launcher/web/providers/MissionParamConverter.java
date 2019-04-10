package io.fabric8.launcher.web.providers;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.ws.rs.ext.ParamConverter;

import io.fabric8.launcher.booster.catalog.rhoar.Mission;
import io.fabric8.launcher.booster.catalog.rhoar.RhoarBoosterCatalog;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@ApplicationScoped
public class MissionParamConverter implements ParamConverter<Mission> {

    // Cannot use constructor-type injection (gives NPE in CdiInjectorFactory)
    @Inject
    Instance<RhoarBoosterCatalog> catalogInstance;

    @Override
    public Mission fromString(final String missionId) {
        if (missionId == null) {
            throw new IllegalArgumentException("Mission ID is required");
        } else {
            RhoarBoosterCatalog catalog = catalogInstance.get();
            return catalog.getMissions().stream()
                    .filter(mission -> mission.getId().equals(missionId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Mission does not exist: " + missionId));
        }
    }

    @Override
    public String toString(Mission mission) {
        if (mission == null) {
            throw new IllegalArgumentException("Mission is required");
        }
        return mission.getId();
    }
}
