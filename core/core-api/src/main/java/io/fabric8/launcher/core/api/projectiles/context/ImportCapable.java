package io.fabric8.launcher.core.api.projectiles.context;

import javax.annotation.Nullable;

public interface ImportCapable {
    String getApplicationName();

    String getGitImportUrl();

    @Nullable
    String getGitImportBranch();

    @Nullable
    String getBuilderImage();

    @Nullable
    String getBuilderLanguage();
}
