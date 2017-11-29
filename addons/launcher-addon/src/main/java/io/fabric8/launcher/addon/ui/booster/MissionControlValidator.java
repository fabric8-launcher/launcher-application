/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.fabric8.launcher.addon.ui.booster;

import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.HttpHeaders;

import io.fabric8.launcher.addon.MissionControl;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIValidationContext;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@SuppressWarnings("unchecked")
@ApplicationScoped
public class MissionControlValidator {
    @Inject
    private MissionControl missionControlFacade;

    public List<String> getOpenShiftClusters(UIContext context) {
        Map<Object, Object> attributeMap = context.getAttributeMap();
        return (List<String>) attributeMap.computeIfAbsent("openShiftClusters", key -> {
            List<String> authList = (List<String>) attributeMap.get(HttpHeaders.AUTHORIZATION);
            String authHeader = (authList == null || authList.isEmpty()) ? null : authList.get(0);
            return missionControlFacade.getOpenShiftClusters(authHeader);
        });
    }

    public boolean validateGitHubTokenExists(UIValidationContext context) {
        Map<Object, Object> attributeMap = context.getUIContext().getAttributeMap();
        String validationMessage = (String) attributeMap.computeIfAbsent("token_github_exists", key -> {
            List<String> authList = (List<String>) attributeMap.get(HttpHeaders.AUTHORIZATION);
            String authHeader = (authList == null || authList.isEmpty()) ? null : authList.get(0);
            return missionControlFacade.validateGitHubTokenExists(authHeader);
        });
        if (validationMessage != null && !MissionControl.VALIDATION_MESSAGE_OK.equals(validationMessage)) {
            context.addValidationError(context.getCurrentInputComponent(), validationMessage);
            return false;
        }
        return true;
    }

    public boolean validateOpenShiftTokenExists(UIValidationContext context, String cluster) {
        Map<Object, Object> attributeMap = context.getUIContext().getAttributeMap();
        String validationMessage = (String) attributeMap.computeIfAbsent("token_openshift_exists", key -> {
            List<String> authList = (List<String>) attributeMap.get(HttpHeaders.AUTHORIZATION);
            String authHeader = (authList == null || authList.isEmpty()) ? null : authList.get(0);
            return missionControlFacade.validateOpenShiftTokenExists(authHeader, cluster);
        });
        if (validationMessage != null && !MissionControl.VALIDATION_MESSAGE_OK.equals(validationMessage)) {
            context.addValidationError(context.getCurrentInputComponent(), validationMessage);
            return false;
        }
        return true;
    }

    public void validateGitHubRepositoryExists(UIValidationContext context, String repository) {
        Map<Object, Object> attributeMap = context.getUIContext().getAttributeMap();
        String validationMessage = (String) attributeMap.computeIfAbsent("validate_repo_" + repository, key -> {
            List<String> authList = (List<String>) attributeMap.get(HttpHeaders.AUTHORIZATION);
            String authHeader = (authList == null || authList.isEmpty()) ? null : authList.get(0);
            return missionControlFacade.validateGitHubRepositoryExists(authHeader, repository);
        });
        if (validationMessage != null && !MissionControl.VALIDATION_MESSAGE_OK.equals(validationMessage)) {
            context.addValidationError(context.getCurrentInputComponent(), validationMessage);
        }
    }

    public void validateOpenShiftProjectExists(UIValidationContext context, String project, String cluster) {
        Map<Object, Object> attributeMap = context.getUIContext().getAttributeMap();
        String validationMessage = (String) attributeMap.computeIfAbsent("validate_project_" + project, key -> {
            List<String> authList = (List<String>) attributeMap.get(HttpHeaders.AUTHORIZATION);
            String authHeader = (authList == null || authList.isEmpty()) ? null : authList.get(0);
            return missionControlFacade.validateOpenShiftProjectExists(authHeader, project, cluster);
        });
        if (validationMessage != null && !MissionControl.VALIDATION_MESSAGE_OK.equals(validationMessage)) {
            context.addValidationWarning(context.getCurrentInputComponent(), validationMessage);
        }
    }
}