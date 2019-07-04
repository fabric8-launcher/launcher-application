package io.fabric8.launcher.creator.core.template.transformers

import io.fabric8.launcher.creator.core.template.Transformer

/**
 * Replaces any lines encountered that match the pattern with the given line(s)
 * @param pattern A string that matches any part of the line
 * @param text Either a single string or an array of strings to be inserted
 */
fun replace(pattern: String, text: List<String>): Transformer {
    return replace(text) { it.contains(pattern) }
}

/**
 * Replaces any lines encountered that match the pattern with the given line(s)
 * @param pattern A Regular Expression that will be matched against the input
 * @param text Either a single string or an array of strings to be inserted
 */
fun replace(pattern: Regex, text: List<String>): Transformer {
    return replace(text) { it.contains(pattern) }
}

/**
 * Replaces any lines encountered that match the pattern with the given line(s)
 * @param matcher A matcher function that will be passed each line of input at a time
 * @param text Either a single string or an array of strings to be inserted
 */
fun replace(text: Iterable<String>, matcher: (String) -> Boolean): Transformer {
    return { lines ->
        lines.flatMap { line ->
            if (matcher(line)) {
                sequenceOf<String>() + text
            } else {
                sequenceOf(line)
            }
        }
    }
}
