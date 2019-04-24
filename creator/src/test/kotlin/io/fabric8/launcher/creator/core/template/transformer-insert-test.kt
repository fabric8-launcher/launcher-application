package io.fabric8.launcher.creator.core.template

import io.fabric8.launcher.creator.core.template.transformers.insertAfter
import io.fabric8.launcher.creator.core.template.transformers.insertAtEnd
import io.fabric8.launcher.creator.core.template.transformers.insertAtStart
import io.fabric8.launcher.creator.core.template.transformers.insertBefore
import org.assertj.core.api.Assertions
import org.junit.Test
import java.io.File

class TransformerInsertTest {
    val testContents = """The first line.
The second line.
The third line."""

    val insertedLine = listOf("The inserted line.")

    val insertedLines = listOf(
        "These are several",
        "inserted lines."
    )

    val resultAfterStringLine = """The first line.
The second line.
The inserted line.
The third line.
"""

    val resultAfterStringLines = """The first line.
The second line.
These are several
inserted lines.
The third line.
"""

    val resultAfterStringLineAtEnd = """The first line.
The second line.
The third line.
The inserted line.
"""

    val resultAfterRegExpLine = """The first line.
The second line.
The inserted line.
The third line.
The inserted line.
"""

    val resultBeforeStringLine = """The first line.
The inserted line.
The second line.
The third line.
"""

    val resultBeforeStringLines = """The first line.
These are several
inserted lines.
The second line.
The third line.
"""

    val resultBeforeStringLineAtStart = """The inserted line.
The first line.
The second line.
The third line.
"""

    val resultBeforeRegExpLine = """The first line.
The inserted line.
The second line.
The inserted line.
The third line.
"""

    val resultStartLine = """The inserted line.
The first line.
The second line.
The third line.
"""

    val resultStartLines = """These are several
inserted lines.
The first line.
The second line.
The third line.
"""

    val resultEndLine = """The first line.
The second line.
The third line.
The inserted line.
"""

    val resultEndLines = """The first line.
The second line.
The third line.
These are several
inserted lines.
"""

    @Test
    fun `transform insertAfter string line`() {
        test(insertAfter("second", insertedLine), resultAfterStringLine)
    }

    @Test
    fun `transform insertAfter string lines`() {
        test(insertAfter("second", insertedLines), resultAfterStringLines)
    }

    @Test
    fun `transform insertAfter string line at end`() {
        test(insertAfter("third", insertedLine), resultAfterStringLineAtEnd)
    }

    @Test
    fun `transform insertAfter regex line`() {
        test(insertAfter("d".toRegex(), insertedLine), resultAfterRegExpLine)
    }

    @Test
    fun `transform insertBefore string line`() {
        test(insertBefore("second", insertedLine), resultBeforeStringLine)
    }

    @Test
    fun `transform insertBefore string lines`() {
        test(insertBefore("second", insertedLines), resultBeforeStringLines)
    }

    @Test
    fun `transform insertBefore string line at start`() {
        test(insertBefore("first", insertedLine), resultBeforeStringLineAtStart)
    }

    @Test
    fun `transform insertBefore regex line`() {
        test(insertBefore("d".toRegex(), insertedLine), resultBeforeRegExpLine)
    }

    @Test
    fun `transform insertAtStart line`() {
        test(insertAtStart(insertedLine), resultStartLine)
    }

    @Test
    fun `transform insertAtStart lines`() {
        test(insertAtStart(insertedLines), resultStartLines)
    }

    @Test
    fun `transform insertAtEnd line`() {
        test(insertAtEnd(insertedLine), resultEndLine)
    }

    @Test
    fun `transform insertAtEnd lines`() {
        test(insertAtEnd(insertedLines), resultEndLines)
    }

    private fun test(tr: Transformer, expected: String) {
        // Write test file
        val tmp = File.createTempFile("test", "tmp")
        try {
            tmp.deleteOnExit()
            tmp.writeText(testContents)

            transform(tmp.toPath(), tmp.toPath(), tr)

            val current = tmp.readText()
            Assertions.assertThat(current).isEqualTo(expected)
        } finally {
            tmp.delete()
        }
    }
}
