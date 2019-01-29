package io.fabric8.launcher.osio.enrichers;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import io.fabric8.launcher.core.api.Projectile;
import io.fabric8.launcher.core.spi.ProjectileEnricher;
import io.fabric8.launcher.osio.projectiles.OsioLaunchProjectile;
import io.fabric8.launcher.service.git.api.GitService;
import org.apache.commons.io.FilenameUtils;

import static java.util.Arrays.asList;

/**
 * Replaces the import statements with the new Git parameters.
 */
@Dependent
public class GitUserGoEnricher implements ProjectileEnricher {

    private static final Logger log = Logger.getLogger(GitUserGoEnricher.class.getName());

    private static final String IMPORT_START = "import (";

    private static final String IMPORT_END = ")";

    private static final List<String> VALID_EXTENSIONS = asList("go");

    private static final List<String> VALID_FILENAMES = asList("Makefile", "assemble", "environment");

    private final GitService gitService;

    @Inject
    public GitUserGoEnricher(GitService gitService) {
        this.gitService = gitService;
    }

    @Override
    public void accept(Projectile projectile) {
        if (!(projectile instanceof OsioLaunchProjectile)) {
            // If projectile isn't an OsioLaunchProjectile, skip it
            return;
        }
        OsioLaunchProjectile proj = (OsioLaunchProjectile) projectile;
        // If no organization specified, use the owner location instead
        final String gitOrganization = proj.getGitOrganization() == null ?
                gitService.getLoggedUser().getLogin() :
                proj.getGitOrganization();
        final String gitRepositoryName = proj.getGitRepositoryName();
        String[] split = proj.getBooster().getGitRepo().split("/");
        final String boosterGitOrganization = split[split.length - 2];
        final String boosterGitRepository = split[split.length - 1];
        try {
            Files.walkFileTree(proj.getProjectLocation(), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    String fileName = file.getFileName().toString();
                    if (VALID_FILENAMES.contains(fileName) ||
                            VALID_EXTENSIONS.contains(FilenameUtils.getExtension(fileName))) {
                        customizeFile(file, gitOrganization, gitRepositoryName, boosterGitOrganization, boosterGitRepository);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            log.log(Level.SEVERE, "Error while walking files in repository", e);
        }
    }

    /**
     * Customizes the import section of .go files as well as the makefile and
     * environment file to find and replace the git organization value.
     */
    private void customizeFile(Path path, String newGitOrg, String newGitRepo, String oldGitOrg, String oldGitRepo) throws IOException {
        String contents = new String(Files.readAllBytes(path));
        int importStartIdx = contents.indexOf(IMPORT_START);
        if (importStartIdx < 0) {
            String newContents = contents.replaceAll(oldGitOrg, newGitOrg).replaceAll(oldGitRepo, newGitRepo);
            Files.write(path, newContents.getBytes());
        } else {
            int importEndIdx = contents.indexOf(IMPORT_END, importStartIdx);
            String importContents = contents.substring(importStartIdx + IMPORT_START.length(), importEndIdx);
            String newImportContents = importContents.replaceAll(oldGitOrg + "/" + oldGitRepo, newGitOrg + "/" + newGitRepo);
            StringBuilder sb = new StringBuilder(contents);
            String newContents = sb.replace(importStartIdx + IMPORT_START.length(), importEndIdx, newImportContents).toString();
            Files.write(path, newContents.getBytes());
        }
    }
}