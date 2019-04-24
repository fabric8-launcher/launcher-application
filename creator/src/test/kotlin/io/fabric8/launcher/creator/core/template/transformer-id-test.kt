package io.fabric8.launcher.creator.core.template

import io.fabric8.launcher.creator.core.template.transformers.id
import org.assertj.core.api.Assertions
import org.junit.Test
import java.io.File

class TransformerIdTest {
    val testContents = """The first line.
The second line.
The third line.
""";

    @Test
    fun `transform id`() {
        // Write test file
        val tmp = File.createTempFile("test", "tmp")
        try {
            tmp.deleteOnExit()
            tmp.writeText(testContents)

            transform(tmp.toPath(), tmp.toPath(), id())

            val current = tmp.readText()
            val expected = testContents
            Assertions.assertThat(current).isEqualTo(expected)
        } finally {
            tmp.delete()
        }
    }
}

