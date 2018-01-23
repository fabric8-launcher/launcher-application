package io.fabric8.launcher.addon.catalog;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import io.openshift.booster.catalog.Booster;

public abstract class BoosterFilters {
    private BoosterFilters() {}

    public static Predicate<RhoarBooster> runtimes(Runtime runtime) {
        return (RhoarBooster b) -> runtime == null || runtime.equals(b.getRuntime());
    }

    public static Predicate<RhoarBooster> missions(Mission mission) {
        return (RhoarBooster b) -> mission == null || mission.equals(b.getMission());
    }

    public static Predicate<RhoarBooster> versions(Version version) {
        return (RhoarBooster b) -> version == null || version.equals(b.getVersion());
    }
    
    public static Predicate<RhoarBooster> runsOn(String clusterType) {
        return (RhoarBooster b) -> isSupported(b.getMetadata("runsOn"), clusterType);
    }

    @SuppressWarnings("unchecked")
    private static boolean isSupported(Object supportedTypes, String clusterType) {
        if (clusterType != null && supportedTypes != null) {
            // Make sure we have a list of strings
            Set<String> types;
            if (supportedTypes instanceof List) {
                types = ((List<String>)supportedTypes)
                        .stream()
                        .map(Objects::toString)
                        .collect(Collectors.toSet());
            } else {
                types = Collections.singleton(supportedTypes.toString());
            }

            for (String supportedType : types) {
                if (supportedType.equalsIgnoreCase("all")
                        || supportedType.equalsIgnoreCase("*")
                        || supportedType.equalsIgnoreCase(clusterType)) {
                    return true;
                } else if (supportedType.equalsIgnoreCase("none")
                        || supportedType.equalsIgnoreCase("!*")
                        || supportedType.equalsIgnoreCase("!" + clusterType)) {
                    return false;
                }
            }
            return false;
        } else {
            return true;
        }
    }
}
