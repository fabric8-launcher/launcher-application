/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.fabric8.forge.generator.github;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.fabric8.forge.generator.AttributeMapKeys;
import io.fabric8.forge.generator.cache.CacheNames;
import io.fabric8.forge.generator.git.GitRepositoryDTO;
import io.fabric8.forge.generator.kubernetes.KubernetesClientFactory;
import io.fabric8.forge.generator.kubernetes.KubernetesClientHelper;
import io.fabric8.forge.generator.tenant.NamespaceDTO;
import io.fabric8.forge.generator.tenant.Tenants;
import io.fabric8.kubernetes.api.Controller;
import io.fabric8.kubernetes.api.KubernetesHelper;
import io.fabric8.openshift.api.model.BuildConfig;
import io.fabric8.openshift.client.OpenShiftClient;
import io.fabric8.utils.Strings;
import org.infinispan.Cache;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.context.UINavigationContext;
import org.jboss.forge.addon.ui.context.UIValidationContext;
import org.jboss.forge.addon.ui.input.UISelectMany;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.addon.ui.result.NavigationResult;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.wizard.UIWizardStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.fabric8.forge.generator.AttributeMapKeys.GIT_REPOSITORY_PATTERN;
import static io.fabric8.forge.generator.AttributeMapKeys.GIT_REPO_NAMES;
import static io.fabric8.forge.generator.keycloak.TokenHelper.getMandatoryAuthHeader;

/**
 * Lets the user configure the GitHub organisation and repo name that they want to pick for a new project
 */
public class GitHubImportPickRepositoriesStep extends AbstractGitHubStep implements UIWizardStep {
    final transient Logger LOG = LoggerFactory.getLogger(this.getClass());

    protected Cache<String, Collection<GitRepositoryDTO>> repositoriesCache;

    @Inject
    @WithAttributes(label = "Repository name pattern", required = true, description = "The regex pattern to match repository names")
    private UISelectMany<GitRepositoryDTO> gitRepositoryPattern;

    @Inject
    private GitHubFacadeFactory gitHubFacadeFactory;

    @Inject
    private KubernetesClientFactory kubernetesClientFactory;

    private GitHubFacade github;

    private KubernetesClientHelper kubernetesClientHelper;

    protected Cache<String, List<NamespaceDTO>> namespacesCache;

    private List<NamespaceDTO> namespaces;

    public void initializeUI(final UIBuilder builder) throws Exception {
        super.initializeUI(builder);
        kubernetesClientHelper = kubernetesClientFactory.createKubernetesClient(builder.getUIContext());
        namespacesCache = cacheManager.getCache(CacheNames.USER_NAMESPACES);
        final String key = kubernetesClientHelper.getUserCacheKey();
        namespaces = namespacesCache.computeIfAbsent(key, k -> Tenants.loadNamespaces(getMandatoryAuthHeader(builder.getUIContext())));

        repositoriesCache = cacheManager.getCache(CacheNames.GITHUB_REPOSITORIES_FOR_ORGANISATION);

        github = gitHubFacadeFactory.createGitHubFacade(builder.getUIContext());

        Map<Object, Object> attributeMap = builder.getUIContext().getAttributeMap();
        final String gitOrganisation = (String) attributeMap.get(AttributeMapKeys.GIT_ORGANISATION);

        String userKey = github.getDetails().getUserCacheKey();
        String orgKey = userKey + "/" + gitOrganisation;

        Collection<GitRepositoryDTO> repositoryNames = repositoriesCache.computeIfAbsent(orgKey, k -> github.getRepositoriesForOrganisation(gitOrganisation));

        gitRepositoryPattern.setValueChoices(repositoryNames);
        gitRepositoryPattern.setItemLabelConverter(GitRepositoryDTO::getId);
        builder.add(gitRepositoryPattern);
    }

    @Override
    public void validate(UIValidationContext context) {
        if (github == null || !github.isDetailsValid()) {
            // invoked too early before the github account is setup - lets return silently
            return;
        }
        Iterable<GitRepositoryDTO> value = gitRepositoryPattern.getValue();
        if (!value.iterator().hasNext()) {
            context.addValidationError(gitRepositoryPattern, "You must select a repository to import");
        }
        // Check for repos with already existing bc
        Controller controller = new Controller(kubernetesClientHelper.getKubernetesClient());
        OpenShiftClient openShiftClient = controller.getOpenShiftClientOrNull();
        if (openShiftClient == null) {
            context.addValidationError(gitRepositoryPattern, "Could not create OpenShiftClient. Maybe the Kubernetes server version is older than 1.7?");
        }
        Iterator<GitRepositoryDTO> it = value.iterator();
        String userNameSpace = Tenants.findDefaultUserNamespace(namespaces);
        if (userNameSpace == null) {
            // Tenant not yet initialised properly!
            return;
        }
        while (it.hasNext()) {
            GitRepositoryDTO repo = it.next();
            if (repo != null && repo.getName() != null) {
                BuildConfig oldBC = openShiftClient.buildConfigs().inNamespace(userNameSpace).withName(repo.getName().toLowerCase()).get();
                if (oldBC != null && Strings.isNotBlank(KubernetesHelper.getName(oldBC))) {
                    context.addValidationError(gitRepositoryPattern, "The repository " + repo.getName() + " has already a build config, please select another repo.");
                    break;
                }
            }
        }


    }

    @Override
    public NavigationResult next(UINavigationContext context) throws Exception {
        storeAttributes(context.getUIContext());
        return null;
    }

    @Override
    public Result execute(UIExecutionContext context) throws Exception {
        return storeAttributes(context.getUIContext());
    }

    protected Result storeAttributes(UIContext uiContext) {
        if (github == null) {
            return Results.fail("No github account setup");
        }
        List<String> repositories = new ArrayList<>();

        Iterable<GitRepositoryDTO> values = gitRepositoryPattern.getValue();
        for (GitRepositoryDTO repo : values) {
            String id = repo.getId();

            if (Strings.isNotBlank(id)) {
                repositories.add(id);
            }
/*
            Pattern regex;
            try {
                regex = Pattern.compile(pattern);
            } catch (Exception e) {
                return Results.fail("Invalid regular expression `" + pattern + "` due to: " + e, e);
            }

            for (String repositoryName : repositoryNames) {
                if (regex.matcher(repositoryName).matches()) {
                    repositories.add(repositoryName);
                }
            }
*/
        }
        String pattern = createPatternFromRepositories(repositories);
        uiContext.getAttributeMap().put(GIT_REPOSITORY_PATTERN, pattern);
        uiContext.getAttributeMap().put(GIT_REPO_NAMES, repositories);
        return Results.success();
    }

    private String createPatternFromRepositories(Iterable<String> value) {
        StringBuilder builder = new StringBuilder();
        for (String name : value) {
            if (builder.length() > 0) {
                builder.append("|");
            }
            builder.append(name);
        }
        return builder.toString();
    }
}
