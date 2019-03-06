package io.fabric8.launcher.creator.core.template.transformers

import io.fabric8.launcher.creator.core.template.Transformer

/**
 * Inserts the given line(s) before any lines encountered that match the pattern
 * @param pattern A string that matches any part of the line
 * @param text Either a single string or an array of strings to be inserted
 */
fun insertBefore(pattern: String, text: Iterable<String>): Transformer {
    return insertBefore(text) { it.contains(pattern) }
}

/**
 * Inserts the given line(s) before any lines encountered that match the pattern
 * @param pattern A Regular Expression that will be matched against the input
 * @param text Either a single string or an array of strings to be inserted
 */
fun insertBefore(pattern: Regex, text: Iterable<String>): Transformer {
    return insertBefore(text) { it.contains(pattern) }
}

/**
 * Inserts the given line(s) before any lines encountered that match the pattern
 * @param matcher A matcher function that will be passed each line of input at a time
 * @param text Either a single string or an array of strings to be inserted
 */
fun insertBefore(text: Iterable<String>, matcher: (String) -> Boolean): Transformer {
    return { lines ->
        lines.flatMap { line ->
            if (matcher(line)) {
                sequenceOf<String>() + text + line
            } else {
                sequenceOf(line)
            }
        }
    }
}

/**
 * Inserts the given line(s) after any lines encountered that match the pattern
 * @param pattern A string that matches any part of the line
 * @param text Either a single string or an array of strings to be inserted
 */
fun insertAfter(pattern: String, text: Iterable<String>): Transformer {
    return insertAfter(text) { it.contains(pattern) }
}

/**
 * Inserts the given line(s) after any lines encountered that match the pattern
 * @param pattern A Regular Expression that will be matched against the input
 * @param text Either a single string or an array of strings to be inserted
 */
fun insertAfter(pattern: Regex, text: Iterable<String>): Transformer {
    return insertAfter(text) { it.contains(pattern) }
}

/**
 * Inserts the given line(s) after any lines encountered that match the pattern
 * @param matcher A matcher function that will be passed each line of input at a time
 * @param text Either a single string or an array of strings to be inserted
 */
fun insertAfter(text: Iterable<String>, matcher: (String) -> Boolean): Transformer {
    return { lines ->
        lines.flatMap { line ->
            if (matcher(line)) {
                sequenceOf<String>() + line + text
            } else {
                sequenceOf(line)
            }
        }
    }
}

/**
 * Inserts the given line(s) at the start of the stream
 * @param text Either a single string or an array of strings to be inserted
 */
fun insertAtStart(text: Iterable<String>): Transformer {
    return { lines -> sequenceOf<String>() + text + lines }
}

/**
 * Inserts the given line(s) at the end of the stream
 * @param text Either a single string or an array of strings to be inserted
 */
fun insertAtEnd(text: Iterable<String>): Transformer {
    return { lines -> lines + text }
}
