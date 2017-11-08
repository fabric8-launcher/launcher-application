package io.openshift.appdev.missioncontrol.core.api;

import java.util.UUID;

/**
 * Launch event with all the information necessary for tracking
 */
public class LaunchEvent {
    private String user;
    private UUID id;
    private String githubRepo;
    private String openshiftProjectName;
    private String mission;
    private String runtime;
    
    public LaunchEvent(String user, UUID id, String githubRepo, String openshiftProjectName, String mission, String runtime) {
        super();
        this.user = user;
        this.id = id;
        this.githubRepo = githubRepo;
        this.openshiftProjectName = openshiftProjectName;
        this.mission = mission;
        this.runtime = runtime;
    }

    public String getUser() {
        return user;
    }

    public UUID getId() {
        return id;
    }

    public String getGithubRepo() {
        return githubRepo;
    }

    public String getOpenshiftProjectName() {
        return openshiftProjectName;
    }

    public String getMission() {
        return mission;
    }

    public String getRuntime() {
        return runtime;
    }

}
