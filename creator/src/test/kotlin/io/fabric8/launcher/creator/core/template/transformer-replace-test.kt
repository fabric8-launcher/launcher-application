package io.fabric8.launcher.creator.core.template

import io.fabric8.launcher.creator.core.template.transformers.replace
import org.assertj.core.api.Assertions
import org.junit.Test
import java.io.File

class TransformerReplaceTest {
    val testContents = """The first line.
The second line.
The third line."""

    val replacedLine = listOf("The replaced line.")

    val replacedLines = listOf(
        "These are several",
        "replaced lines."
    )

    val replacedEmpty = listOf<String>()

    val resultReplaceStringLine = """The first line.
The replaced line.
The third line.
"""

    val resultReplaceStringLines = """The first line.
These are several
replaced lines.
The third line.
"""

    val resultReplaceStringLineAtStart = """The replaced line.
The second line.
The third line.
"""

    val resultReplaceStringLineAtEnd = """The first line.
The second line.
The replaced line.
"""

    val resultReplaceRegExpLine = """The first line.
The replaced line.
The replaced line.
"""

    val resultReplaceRegExpLines = """These are several
replaced lines.
These are several
replaced lines.
These are several
replaced lines.
"""

    val resultReplaceRegExpEmpty = ""

    @Test
    fun `transform replace string line`() {
        test(replace("second", replacedLine), resultReplaceStringLine)
    }

    @Test
    fun `transform replace string lines`() {
        test(replace("second", replacedLines), resultReplaceStringLines)
    }

    @Test
    fun `transform replace string line at start`() {
        test(replace("first", replacedLine), resultReplaceStringLineAtStart)
    }

    @Test
    fun `transform replace string line at end`() {
        test(replace("third", replacedLine), resultReplaceStringLineAtEnd)
    }

    @Test
    fun `transform replace regex line`() {
        test(replace("d".toRegex(), replacedLine), resultReplaceRegExpLine)
    }

    @Test
    fun `transform replace regex lines`() {
        test(replace("line".toRegex(), replacedLines), resultReplaceRegExpLines)
    }

    @Test
    fun `transform replace regex empty`() {
        test(replace("line".toRegex(), replacedEmpty), resultReplaceRegExpEmpty)
    }

    private fun test(tr: Transformer, expected: String, contents: String = testContents) {
        // Write test file
        val tmp = File.createTempFile("test", "tmp")
        try {
            tmp.deleteOnExit()
            tmp.writeText(contents)

            transform(tmp.toPath(), tmp.toPath(), tr)

            val current = tmp.readText()
            Assertions.assertThat(current).isEqualTo(expected)
        } finally {
            tmp.delete()
        }
    }
}
