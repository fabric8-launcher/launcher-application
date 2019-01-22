package io.fabric8.launcher.osio.steps.booster;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.math.NumberUtils;

/**
 * Customizes golan booster files.
 *
 */
public class GolangBooster {
    private static final Logger log = Logger.getLogger(GolangBooster.class.getName());
    private static final String GO_FILE_EXTENSION = ".go";
    private static final String ENVIRONMENT = "environment";
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

    /**
     * C'tor will set the project location, booster data and git user.
     *
     * @param projectLocation
     *            The project location where this projectile is
     * @param boosterData
     *            The booster data which this projectile refers to
     * @param gitUser
     *            The user logged into the GitService
     */
    public GolangBooster(Path projectLocation, Map<String, Object> boosterData, String gitUser) {
        this.projectLocation = projectLocation;
        this.boosterData = boosterData;
        this.gitUser = gitUser;
    }

    /**
     * Gets a list of files from a directory.
     *
     * @return a Map containing the modified file contents
     */
    public Map<File, String> customize() {
        File[] directoryContents = new File(this.projectLocation.toString()).listFiles();
        if (directoryContents != null) {
            getBoosterFiles(directoryContents);
        }

        return this.filesToPush;
    }

    /**
     * Recursively gets files to template.
     *
     * @param files
     *            Files to template
     */
    private void getBoosterFiles(File[] directoryContents) {
        for (File path : directoryContents) {
            // if the path is a directory, recusively call getBoosterFiles(...).
            if (path.isDirectory()) {
                getBoosterFiles(path.listFiles());
            }

            // Since the path is a file and not a directory, templatize the file.
            customizeBoosterFiles(path);
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
        String extension = GolangBoosterUtility.getFileExtension(file.getName());
        boolean isGoExtension = extension.equals(GO_FILE_EXTENSION);
        List<String> content = new ArrayList<>();

        if (isGoExtension || ((file.getName().equals(MAKEFILE) || file.getName().equals(ENVIRONMENT)) && extension.equals(NO_FILE_EXTENSION))) {
            content = GolangBoosterUtility.getFileContents(file);
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
            if (GolangBoosterUtility.replaceContent(line, content, i, getGitOrganization(boosterData), this.gitUser)) {
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
    @SuppressWarnings("unchecked")
    private String getGitOrganization(Map<String, Object> boosterData) {
        HashMap<String, HashMap<String, String>> boosterDataSource = (HashMap<String, HashMap<String, String>>) boosterData.get(SOURCE);
        HashMap<String, String> boosterGitData = boosterDataSource.get(GIT);
        String gitOrg = null;
        try {
            URL boosterURL = new URL(boosterGitData.get(URL));
            gitOrg = boosterURL.getPath().split(SEPARATOR)[NumberUtils.INTEGER_ONE];
        } catch (MalformedURLException e) {
            log.log(Level.SEVERE, "Error while parsing booster repository URL", e);
        }
        return gitOrg;
    }
}