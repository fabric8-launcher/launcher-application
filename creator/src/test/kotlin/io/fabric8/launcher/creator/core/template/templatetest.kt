package io.fabric8.launcher.creator.core.template

import io.fabric8.launcher.creator.core.compose
import org.assertj.core.api.Assertions.*
import org.junit.Test
import org.junit.jupiter.api.TestInstance
import java.io.File
import java.nio.file.Files

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TemplateTest {
    val testContents =
        """0123456789
56789
56789
0123456789

0123456789
0123456789

0123456789
56789
"""

    @Test
    fun `transform identity`() {
        // Write test file
        val tmp = File.createTempFile("test", "tmp")
        try {
            tmp.deleteOnExit()
            tmp.writeText(testContents)

            // ID "transformer" that just returns the input unchanged
            val id: Transformer = { lines -> lines }

            transform(tmp.toPath(), tmp.toPath(), id)

            val current = tmp.readText()
            val expected = testContents
            assertThat(current).isEqualTo(expected)
        } finally {
            tmp.delete()
        }
    }

    @Test
    fun `no blank lines`() {
        // Write test file
        val tmp = File.createTempFile("test", "tmp")
        try {
            tmp.deleteOnExit()
            tmp.writeText(testContents)

            // Transformer that only returns non-empty lines
            val nonempty: Transformer = { lines -> lines.filter { !it.isBlank() } }

            transform(tmp.toPath(), tmp.toPath(), nonempty)

            val current = tmp.readText()
            val expected = """0123456789
56789
56789
0123456789
0123456789
0123456789
0123456789
56789
"""
            assertThat(current).isEqualTo(expected)
        } finally {
            tmp.delete()
        }
    }

    @Test
    fun `transform chaining`() {
        // Write test file
        val tmp = File.createTempFile("test", "tmp")
        try {
            tmp.deleteOnExit()
            tmp.writeText(testContents)

            // Transformer that removes duplicate consecutive lines (leaving only one)
            var prev: String? = null
            val dedup: Transformer = { lines ->
                lines.filter { line ->
                    if (line != prev) {
                        prev = line
                        true
                    } else {
                        false
                    }
                }
            }
            // Transformer that only returns non-empty lines
            val nonempty: Transformer = { lines -> lines.filter { !it.isBlank() } }
            // Transformer that is the composition of the previous two
            val comp = compose(dedup, nonempty)

            transform(tmp.toPath(), tmp.toPath(), comp)

            val current = tmp.readText()
            val expected = """0123456789
56789
0123456789
56789
"""
            assertThat(current).isEqualTo(expected)
        } finally {
            tmp.delete()
        }
    }

    @Test
    fun `transform empty`() {
        // Write test file
        val tmp = File.createTempFile("test", "tmp")
        try {
            tmp.deleteOnExit()
            tmp.writeText(testContents)

            // Nil "transformer" that drops everything
            val id: Transformer = { _ -> sequenceOf() }

            transform(tmp.toPath(), tmp.toPath(), id)

            val current = tmp.readText()
            val expected = ""
            assertThat(current).isEqualTo(expected)
        } finally {
            tmp.delete()
        }
    }

    @Test
    fun `transform files single pattern`() {
        // Write test file
        val dir = Files.createTempDirectory("test").toFile()
        val tmp1 = File.createTempFile("test", "foo", dir)
        val tmp2 = File.createTempFile("test", "bar", dir)
        val tmp3 = File.createTempFile("test", "baz", dir)
        try {
            tmp1.deleteOnExit()
            tmp2.deleteOnExit()
            tmp3.deleteOnExit()
            dir.deleteOnExit()
            tmp1.writeText(testContents)
            tmp2.writeText(testContents)
            tmp3.writeText(testContents)

            // Nil "transformer" that drops everything
            val id: Transformer = { _ -> sequenceOf() }

            transformFiles(dir.toPath(), "**/*bar", id)

            val current1 = tmp1.readText()
            val expected1 = testContents
            assertThat(current1).isEqualTo(expected1)

            val current2 = tmp2.readText()
            val expected2 = ""
            assertThat(current2).isEqualTo(expected2)

            val current3 = tmp3.readText()
            val expected3 = testContents
            assertThat(current3).isEqualTo(expected3)
        } finally {
            dir.deleteRecursively()
        }
    }

    @Test
    fun `transform files multi pattern`() {
        // Write test file
        val dir = Files.createTempDirectory("test").toFile()
        val tmp1 = File.createTempFile("test", "foo", dir)
        val tmp2 = File.createTempFile("test", "bar", dir)
        val tmp3 = File.createTempFile("test", "baz", dir)
        try {
            tmp1.deleteOnExit()
            tmp2.deleteOnExit()
            tmp3.deleteOnExit()
            dir.deleteOnExit()
            tmp1.writeText(testContents)
            tmp2.writeText(testContents)
            tmp3.writeText(testContents)

            // Nil "transformer" that drops everything
            val id: Transformer = { _ -> sequenceOf() }

            transformFiles(dir.toPath(), listOf("**/*bar", "**/*baz"), id)

            val current1 = tmp1.readText()
            val expected1 = testContents
            assertThat(current1).isEqualTo(expected1)

            val current2 = tmp2.readText()
            val expected2 = ""
            assertThat(current2).isEqualTo(expected2)

            val current3 = tmp3.readText()
            val expected3 = ""
            assertThat(current3).isEqualTo(expected3)
        } finally {
            dir.deleteRecursively()
        }
    }

}
