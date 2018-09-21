package io.fabric8.launcher.core.impl.preparers;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.enterprise.context.ApplicationScoped;
import io.fabric8.launcher.booster.catalog.rhoar.RhoarBooster;
import io.fabric8.launcher.core.api.ProjectileContext;
import io.fabric8.launcher.core.api.projectiles.context.BoosterCapable;
import io.fabric8.launcher.core.spi.ProjectilePreparer;

@ApplicationScoped
public class VSCodePreparer implements ProjectilePreparer {

    public static final String EXTENSIONS_JSON_FILE = "extensions.json";
    public static final String VSCODE_FOLDER = ".vscode";

    @Override
    public void prepare(Path projectPath, RhoarBooster booster, ProjectileContext context) {
        if (!(context instanceof BoosterCapable)) {
            return;
        }
        BoosterCapable createProjectileContext = (BoosterCapable) context;
        generateVSCodeRecommandations(projectPath, createProjectileContext.getRuntime().getId());
    }

    void generateVSCodeRecommandations(Path projectPath, String runtimeId) {
        if (needsVSCodeRecommandations(projectPath)) {
            try (InputStream stack = VSCodePreparer.class
                    .getResourceAsStream("/vscode/" + runtimeId + "/extensions.json")) {
                if (stack != null) {
                    Files.createDirectories(projectPath.resolve(VSCODE_FOLDER));
                    Files.copy(stack, projectPath.resolve(VSCODE_FOLDER).resolve(EXTENSIONS_JSON_FILE));
                }
            } catch (IOException e) {
                throw new UncheckedIOException("Error while traversing project " + projectPath, e);
            }
        }
    }

    private boolean needsVSCodeRecommandations(Path projectPath) {
        Path vscode = projectPath.resolve(VSCODE_FOLDER);
        return !vscode.toFile().exists() && !vscode.resolve(EXTENSIONS_JSON_FILE).toFile().exists();
    }
}
