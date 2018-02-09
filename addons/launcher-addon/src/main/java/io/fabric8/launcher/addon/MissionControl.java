/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.fabric8.launcher.addon;

import java.net.ConnectException;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import io.fabric8.launcher.base.EnvironmentSupport;
import io.fabric8.launcher.core.spi.IdentityProvider;

/**
 * Facade for the Mission Control component
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Singleton
@Deprecated
public class MissionControl {
    public static final String VALIDATION_MESSAGE_OK = "OK";

    @Inject
    public MissionControl() {
        this(getEnvVarOrSysProp(LAUNCHER_MISSIONCONTROL_SERVICE_HOST, "localhost"),
             Integer.parseInt(getEnvVarOrSysProp(LAUNCHER_MISSIONCONTROL_SERVICE_PORT, "8080")));
    }

    public MissionControl(String host, int port) {
        missionControlValidationURI = UriBuilder.fromPath("/api/validate").host(host).scheme("http")
                .port(port).build();
        missionControlOpenShiftURI = UriBuilder.fromPath("/api/openshift").host(host).scheme("http")
                .port(port).build();
    }

    private static final String LAUNCHER_MISSIONCONTROL_SERVICE_HOST = "LAUNCHER_MISSIONCONTROL_SERVICE_HOST";

    private static final String LAUNCHER_MISSIONCONTROL_SERVICE_PORT = "LAUNCHER_MISSIONCONTROL_SERVICE_PORT";

    private static final Logger log = Logger.getLogger(MissionControl.class.getName());

    private final URI missionControlValidationURI;

    private final URI missionControlOpenShiftURI;

    /**
     * Validates if the OpenShift project exists
     *
     * @param authHeader
     * @param project
     * @return a validation message, returns {@link #VALIDATION_MESSAGE_OK} if the project does not exist
     */
    public String validateOpenShiftProjectExists(String authHeader, String project, String cluster) {
        String validationMessage;
        try {
            UriBuilder builder = UriBuilder.fromUri(missionControlValidationURI).path("/project/" + project);
            if (cluster != null) {
                builder.queryParam("cluster", cluster);
            }
            URI targetURI = builder.build();
            if (head(targetURI, authHeader, cluster) == Response.Status.OK.getStatusCode()) {
                validationMessage = "OpenShift Project '" + project + "' already exists";
            } else {
                validationMessage = VALIDATION_MESSAGE_OK;
            }
        } catch (Exception e) {
            String message = e.getMessage();
            Throwable root = rootException(e);
            if (root instanceof UnknownHostException || root instanceof ConnectException) {
                validationMessage = "Mission Control is offline and cannot validate the OpenShift Project Name";
            } else {
                if (root.getMessage() != null) {
                    message = root.getMessage();
                }
                validationMessage = "Error while validating OpenShift Project Name: " + message;
            }
        }
        return validationMessage;
    }

    public String validateGitHubRepositoryExists(String authHeader, String repository) {
        String validationMessage;
        try {
            URI targetURI = UriBuilder.fromUri(missionControlValidationURI).path("/repository/" + repository).build();
            if (head(targetURI, authHeader, null) == Response.Status.OK.getStatusCode()) {
                validationMessage = "GitHub Repository '" + repository + "' already exists";
            } else {
                validationMessage = VALIDATION_MESSAGE_OK;
            }
        } catch (Exception e) {
            String message = e.getMessage();
            Throwable root = rootException(e);
            if (root instanceof UnknownHostException || root instanceof ConnectException) {
                validationMessage = "Mission Control is offline and cannot validate the GitHub Repository Name";
            } else {
                if (root.getMessage() != null) {
                    message = root.getMessage();
                }
                validationMessage = "Error while validating GitHub Repository Name: " + message;
            }
        }
        return validationMessage;
    }

    public String validateOpenShiftTokenExists(String authHeader, String cluster) {
        String validationMessage;
        try {
            UriBuilder builder = UriBuilder.fromUri(missionControlValidationURI).path("/token/openshift");
            if (cluster != null) {
                builder.queryParam("cluster", cluster);
            }
            URI targetURI = builder.build();
            if (head(targetURI, authHeader, cluster) == Response.Status.OK.getStatusCode()) {
                validationMessage = VALIDATION_MESSAGE_OK;
            } else {
                validationMessage = "OpenShift Token does not exist";
            }
        } catch (Exception e) {
            String message = e.getMessage();
            Throwable root = rootException(e);
            if (root instanceof UnknownHostException || root instanceof ConnectException) {
                validationMessage = "Mission Control is offline and cannot validate if the OpenShift token exists";
            } else {
                if (root.getMessage() != null) {
                    message = root.getMessage();
                }
                validationMessage = "Error while validating if the OpenShift Token exists: " + message;
            }
        }
        return validationMessage;
    }

    public String validateGitHubTokenExists(String authHeader) {
        String validationMessage;
        try {
            URI targetURI = UriBuilder.fromUri(missionControlValidationURI).path("/token/github").build();
            if (head(targetURI, authHeader, null) == Response.Status.OK.getStatusCode()) {
                validationMessage = VALIDATION_MESSAGE_OK;
            } else {
                validationMessage = "GitHub Token does not exist";
            }
        } catch (Exception e) {
            String message = e.getMessage();
            Throwable root = rootException(e);
            if (root instanceof UnknownHostException || root instanceof ConnectException) {
                validationMessage = "Mission Control is offline and cannot validate if the GitHub token exists";
            } else {
                if (root.getMessage() != null) {
                    message = root.getMessage();
                }
                validationMessage = "Error while validating if the GitHub Token exists: " + message;
            }
        }
        return validationMessage;
    }

    public List<String> getOpenShiftClusters(String authHeader) {
        URI targetURI = UriBuilder.fromUri(missionControlOpenShiftURI).path("/clusters").build();
        try {
            return perform(client -> client
                    .target(targetURI)
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .header(HttpHeaders.AUTHORIZATION, authHeader)
                    .get().readEntity(new GenericType<List<String>>() {
                    }));
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error while returning openshift clusters", e);
            return Collections.emptyList();
        }
    }

    private static String getEnvVarOrSysProp(String name, String defaultValue) {
        return EnvironmentSupport.INSTANCE.getEnvVarOrSysProp(name, defaultValue);
    }

    private Throwable rootException(Exception e) {
        Throwable root = e;
        while (root.getCause() != null) {
            root = root.getCause();
        }
        return root;
    }

    private int head(URI targetURI, String authHeader, String openShiftCluster) throws ProcessingException {
        return perform(client -> client.target(targetURI).request()
                .header("X-OpenShift-Cluster", Objects.toString(openShiftCluster, IdentityProvider.ServiceType.OPENSHIFT))
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .head().getStatus());
    }

    private <T> T perform(Function<Client, T> request) {
        Client client = null;
        try {
            client = ClientBuilder.newClient();
            return request.apply(client);
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

}
