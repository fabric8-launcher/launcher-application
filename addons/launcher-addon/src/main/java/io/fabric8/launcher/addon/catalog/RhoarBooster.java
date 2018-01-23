package io.fabric8.launcher.addon.catalog;

import java.util.Map;

import io.openshift.booster.catalog.Booster;
import io.openshift.booster.catalog.BoosterFetcher;

public class RhoarBooster extends Booster {
    private Mission mission;
    private Runtime runtime;
    private Version version;

    /**
     * @return the mission
     */
    public Mission getMission() {
        return mission;
    }

    /**
     * @param mission the mission to set
     */
    public void setMission(Mission mission) {
        this.mission = mission;
    }

    /**
     * @return the runtime
     */
    public Runtime getRuntime() {
        return runtime;
    }

    /**
     * @param runtime the runtime to set
     */
    public void setRuntime(Runtime runtime) {
        this.runtime = runtime;
    }

    /**
     * @return the version
     */
    public Version getVersion() {
        return version;
    }

    /**
     * @param version the version to set
     */
    public void setVersion(Version version) {
        this.version = version;
    }

    protected RhoarBooster(BoosterFetcher boosterFetcher) {
        super(boosterFetcher);
    }

    protected RhoarBooster(Map<String, Object> data, BoosterFetcher boosterFetcher) {
        super(data, boosterFetcher);
    }

    protected RhoarBooster newBooster(BoosterFetcher boosterFetcher) {
        return new RhoarBooster(boosterFetcher);
    }
    
    protected RhoarBooster merge(RhoarBooster booster) {
        super.merge(booster);
        if (booster.mission != null) mission = booster.mission;
        if (booster.runtime != null) runtime = booster.runtime;
        if (booster.version != null) version = booster.version;
        return this;
    }
    
}
