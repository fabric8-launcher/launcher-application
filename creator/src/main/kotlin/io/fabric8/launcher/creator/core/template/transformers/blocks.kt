package io.fabric8.launcher.creator.core.template.transformers

import io.fabric8.launcher.creator.core.template.Transformer

/**
 * Looks for blocks of text starting with a line that matches the start
 * pattern and ends with a line that matches the end pattern. It then
 * passes that block to a filter and replaces the entire block with
 * the result returned by the filter function.
 * @param startPattern A Regular Expression that will be matched against the input
 * @param endPattern A Regular Expression that will be matched against the input
 * @param transformer The transformer to apply to the lines between start and end
 */
fun blocks(startPattern: String,
           endPattern: String,
           transformer: Transformer): Transformer {
    return blocks({ it.contains(startPattern) }, { it.contains(endPattern) }, transformer)
}

/**
 * Looks for blocks of text starting with a line that matches the start
 * pattern and ends with a line that matches the end pattern. It then
 * passes that block to a filter and replaces the entire block with
 * the result returned by the filter function.
 * @param startPattern A Regular Expression that will be matched against the input
 * @param endPattern A Regular Expression that will be matched against the input
 * @param transformer The transformer to apply to the lines between start and end
 */
fun blocks(startPattern: Regex,
           endPattern: Regex,
           transformer: Transformer): Transformer {
    return blocks({ it.contains(startPattern) }, { it.contains(endPattern) }, transformer)
}

/**
 * Looks for blocks of text starting with a line that matches the start
 * pattern and ends with a line that matches the end pattern. It then
 * passes that block to a filter and replaces the entire block with
 * the result returned by the filter function.
 * @param startMatcher A matcher function that will be passed each line of input at a time
 * @param endMatcher A matcher function that will be passed each line of input at a time
 * @param transform The transformer to apply to the lines between start and end
 */
fun blocks(startMatcher: (String) -> Boolean,
           endMatcher: (String) -> Boolean,
           transform: Transformer): Transformer {
    // Very naive and inefficient implementation, we should really be creating a special Sequence
    return { lines ->
        var inBlock = false
        val block = mutableListOf<String>()
        lines.flatMap { line ->
            if (inBlock) {
                inBlock = !endMatcher(line)
                if (inBlock) {
                    block.add(line)
                    sequenceOf()
                } else {
                    transform(block.asSequence()) + line
                }
            } else {
                inBlock = startMatcher(line)
                if (inBlock) {
                    block.clear()
                }
                sequenceOf(line)
            }
        }
    }
}

data class FirstLastValue<out T>(val value: T, val isFirst: Boolean, val isLast: Boolean)

private class WithFirstLastSequence<T>(private val sequence: Sequence<T>) : Sequence<FirstLastValue<T>> {
    override fun iterator(): Iterator<FirstLastValue<T>> = object : Iterator<FirstLastValue<T>> {
        private val iterator = sequence.iterator()
        private var bufHasNext = iterator.hasNext()
        private var bufNext: T? = null
        private var isFirst = true

        init {
            if (bufHasNext) {
                bufNext = iterator.next()
            }
        }

        override fun hasNext(): Boolean {
            return bufHasNext
        }

        override fun next(): FirstLastValue<T> {
            val value = bufNext
            bufHasNext = iterator.hasNext()
            bufNext = if (bufHasNext) iterator.next() else null
            val res = FirstLastValue(value as T, isFirst, !bufHasNext)
            isFirst = false
            return res
        }
    }

}

fun <T> Sequence<T>.withFirstLast(): Sequence<FirstLastValue<T>> = WithFirstLastSequence(this)

/**
 * Blocks filter that will insert the given lines at the start of any code block.
 * The filter will take into account that all lines in the block must be separated
 * by commas. The last line in a block will never have a comma.
 * @param text The lines to insert
 */
fun insertAtStartOfList(text: Iterable<String>): Transformer {
    return { lines ->
        (sequenceOf<String>() + text + lines)
            .withFirstLast().map { if (it.isLast) ensureNoComma(it.value) else ensureComma(it.value) }
    }
}

/**
 * Blocks filter that will insert the given lines at the end of any code block.
 * The filter will take into account that all lines in the block must be separated
 * by commas. The last line in a block will never have a comma.
 * @param text The lines to insert
 */
fun insertAtEndOfList(text: Iterable<String>): Transformer{
    return { lines ->
        (sequenceOf<String>() + lines + text)
            .withFirstLast().map { if (it.isLast) ensureNoComma(it.value) else ensureComma(it.value) }
    }
}

private fun ensureComma(ln: String): String {
    var line = ln.trimEnd()
    if (!line.endsWith(',')) {
        line += ','
    }
    return line
}

private fun ensureNoComma(ln: String): String {
    var line = ln.trimEnd()
    if (line.endsWith(',')) {
        line = line.substring(0, line.length - 1)
    }
    return line
}
