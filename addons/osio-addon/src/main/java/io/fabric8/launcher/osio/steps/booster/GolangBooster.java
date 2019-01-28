package io.fabric8.launcher.osio.steps.booster;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.math.NumberUtils;
import org.codehaus.plexus.util.FileUtils;

import io.fabric8.launcher.core.api.Projectile;
import io.fabric8.launcher.core.spi.ProjectileEnricher;
import io.fabric8.launcher.osio.projectiles.OsioLaunchProjectile;
import io.fabric8.launcher.service.git.api.GitService;

/**
 * Customizes golan booster files.
 *
 */
@ApplicationScoped
public class GolangBooster implements ProjectileEnricher {

    @Inject
    GitService gitService;

    private static final Logger log = Logger.getLogger(GolangBooster.class.getName());
    private static final String GO_FILE_EXTENSION = ".go";
    private static final String ENVIRONMENT = "environment";
    private static final String ASSEMBLE = "assemble";
    private static final String MAKEFILE = "Makefile";
    private static final String GIT = "git";
    private static final String URL = "url";
    private static final String SOURCE = "source";
    private static final String START_IMPORT = "import (";
    private static final String END_IMPORT = ")";
    private static final String NEW_LINE = "\n";
    private static final String NO_FILE_EXTENSION = "";
    private static final String SEPARATOR = "/";

    private Path projectLocation;
    private Map<String, Object> boosterData;
    private String gitUser;
    private Map<File, String> filesToPush = new HashMap<>();
    private String projectName;
    private String osioProjectName;
    private String gitOrg;

    @Override
    public void accept(Projectile arg0) {
        OsioLaunchProjectile proj = (OsioLaunchProjectile) arg0;
        this.projectLocation = proj.getProjectLocation();
        this.boosterData =  proj.getBooster().getData();
        this.gitUser = gitService.getLoggedUser().getLogin();
        this.projectName = proj.getGitRepositoryName();
        customize();
    }

    /**
     * Gets a list of files from a directory and writes the modified file contents.
     */
    public void customize() {
        try {
            Files.walk(this.projectLocation)
            .forEach(filePath -> {
              customizeBoosterFiles(filePath.toFile());
            });
        } catch (IOException e) {
            log.log(Level.SEVERE, "Error while walking files in repository", e);
        }

        for (Entry<File, String> entry : filesToPush.entrySet()) {
            try {
                Files.write(entry.getKey().toPath(), entry.getValue().getBytes());
            } catch (IOException e) {
                log.log(Level.SEVERE, "Error while replacing files in repository", e);
            }
        }
    }

    /**
     * If the file is either a .go file or either makefile or environment file
     * customize the file by replacing the git organization. Once customized
     * push the file to the repository.
     *
     * @param file
     *            File to template
     */
    private void customizeBoosterFiles(File file) {
        boolean pushFile = false;
        String extension = FileUtils.getExtension(file.getName());
        boolean isGoExtension = extension.equals(GO_FILE_EXTENSION);
        List<String> content = new ArrayList<>();

        if (isGoExtension || ((file.getName().equals(MAKEFILE) || file.getName().equals(ASSEMBLE) || file.getName().equals(ENVIRONMENT)) && extension.equals(NO_FILE_EXTENSION))) {
            content = getFileContents(file);
            pushFile = customizeFile(content, isGoExtension);
        }

        if (pushFile) {
            String fileContents = String.join(NEW_LINE, content);
            filesToPush.put(file, fileContents);
        }
    }

    /**
     * Customizes the import section of .go files as well as the makefile and
     * environment file to find and replace the git organization value.
     *
     * @param content
     *            The content of the file
     * @param isGoFile
     *            Flag indicating whether the file is a .go file or not
     * @return True if the file contents were modified, false otherwise
     */
    private boolean customizeFile(List<String> content, boolean isGoFile) {
        boolean pushFile = false;
        int z = NumberUtils.INTEGER_ZERO;
        // If the file is a .go file, look for where the import starts
        // to replace the git organization and set the index
        // for the next loop.
        if (isGoFile) {
            int i;
            for (i = NumberUtils.INTEGER_ZERO; i < content.size(); i++) {
                String line = content.get(i);
                if (line.contains(START_IMPORT)) {
                    break;
                }
            }

            z = i + NumberUtils.INTEGER_ONE;
        }

        // If we are processing imports in a .go file, break out of this
        // loop as soon as we reach the end of imports. Otherwise, process
        // the entire file searching to replace the git organization.
        for (int i = z; i < content.size(); i++) {
            String line = content.get(i);
            if (isGoFile && line.contains(END_IMPORT)) {
                break;
            }

            // If at any point the git organization was replaced the file
            // must be pushed.
            if (replaceContent(line, content, i, getGitOrganization(boosterData), this.gitUser)) {
                pushFile = true;
            }

            if (replaceContent(content.get(i), content, i, getProjectName(boosterData), this.projectName)) {
                pushFile = true;
            }
        }

        return pushFile;
    }

    /**
     * Extracts the boosters git organization.
     *
     * @param boosterData
     *            The booster data to extract the git organization from.
     * @return The git organization to replace while templatizing the booster.
     */
    private String getGitOrganization(Map<String, Object> boosterData) {
        if (this.gitOrg != null) {
            return this.gitOrg;
        }
        this.gitOrg = parseBoosterData(boosterData, NumberUtils.INTEGER_ONE);
        return this.gitOrg;
    }

    /**
     * Extracts the boosters project name in osio.
     *
     * @param boosterData
     *            The booster data to extract the osio project name from.
     * @return The osio project name to replace while templatizing the booster.
     */
    private String getProjectName(Map<String, Object> boosterData) {
        if (this.osioProjectName != null) {
            return this.osioProjectName;
        }
        this.osioProjectName = parseBoosterData(boosterData, NumberUtils.INTEGER_TWO);
        return this.osioProjectName;
    }

    /**
     * Extracts the boosters project name in osio.
     *
     * @param boosterData
     *            The booster data to extract the osio project name from.
     * @param segmentNum
     *            The segment to return.
     * @return The booster url segment to return.
     */
    @SuppressWarnings("unchecked")
    private String parseBoosterData(Map<String, Object> boosterData, int segmentNum) {
        HashMap<String, HashMap<String, String>> boosterDataSource = (HashMap<String, HashMap<String, String>>) boosterData.get(SOURCE);
        HashMap<String, String> boosterGitData = boosterDataSource.get(GIT);
        String segmentValue = null;
        try {
            URL boosterURL = new URL(boosterGitData.get(URL));
            segmentValue = boosterURL.getPath().split(SEPARATOR)[segmentNum];
        } catch (MalformedURLException e) {
            log.log(Level.SEVERE, "Error while parsing booster repository URL", e);
        }

        return segmentValue;
    }

    /**
     * Gets the contents of the file.
     *
     * @param file
     *            The file to retrieve the contents of
     * @return
     */
    private static List<String> getFileContents(File file) {
        List<String> content = new ArrayList<>();
        try (Stream<String> stream = Files.lines(file.toPath())) {
            content = stream.collect(Collectors.toList());
        } catch (IOException e) {
            log.log(Level.SEVERE, "Error while reading file", e);
        }
        return content;
    }

    /**
     * Replaces a substring within a line.
     *
     * @param line
     *            The line to check and replace
     * @param content
     *            The content to set the modified line
     * @param lineNumber
     *            The line number in the file to replace
     * @param substring
     *            The substring to replace in the line
     * @param replacement
     *            The data to replace the substring with
     * @return True if the line was modified, false otherwise
     */
    private static boolean replaceContent(String line, List<String> content, int lineNumber, String substring, String replacement) {
        if (line.contains(substring)) {
            line = line.replaceAll(substring, replacement);
            content.set(lineNumber, line);
            return true;
        }
        return false;
    }
}