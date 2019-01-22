package io.fabric8.launcher.osio.steps.booster;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GolangBoosterUtility {
    private static final Logger log = Logger.getLogger(GolangBoosterUtility.class.getName());

    private GolangBoosterUtility() {

    }

    /**
     * Gets the extension of the file.
     *
     * @param fileName
     *            The name of the file to get the extension for
     * @return The file extension if present or an empty string if the file
     *         contains no extension
     */
    public static String getFileExtension(String fileName) {
        int lastIndexOf = fileName.lastIndexOf('.');
        // If the file has no extension return
        // an empty string.
        if (lastIndexOf == -1) {
            return "";
        }
        return fileName.substring(lastIndexOf);
    }

    /**
     * Gets the contents of the file.
     *
     * @param file
     *            The file to retrieve the contents of
     * @return
     */
    public static List<String> getFileContents(File file) {
        List<String> content = new ArrayList<>();
        try {
            content = Files.readAllLines(file.toPath());
        } catch (IOException e) {
            log.log(Level.SEVERE, "Error while reading file", e);
        }
        return content;
    }

    /**
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
    public static boolean replaceContent(String line, List<String> content, int lineNumber, String substring, String replacement) {
        if (line.contains(substring)) {
            line = line.replaceAll(substring, replacement);
            content.set(lineNumber, line);
            return true;
        }
        return false;
    }
}
