package io.fabric8.launcher.creator.core.template

import io.fabric8.launcher.creator.core.template.transformers.*
import org.assertj.core.api.Assertions
import org.junit.Test
import java.io.File

class TransformerBlocksTest {
    val testContentsEmpty = """
Consumer[] listConsumers() {
    return new Consumer[] {
    }
}
"""

    val testContentsWithEntries = """
Consumer[] listConsumers() {
    return new Consumer[] {
        new FooConsumer(),
        new FooConsumer(),
        new BarConsumer()
    }
}
"""

    val startPattern = "return new Consumer[]"
    val endPattern = "}"

    val insertedEntry = listOf("        new SingleConsumer()")

    val insertedEntryWithComma = listOf("        new SingleConsumer(), ")

    val insertedEntries = listOf(
        "        new OneConsumer()",
        "        new TwoConsumer()"
    )

    val insertedEntriesWithCommas = listOf(
        "        new OneConsumer()",
        "        new TwoConsumer()"
    )

    val resultInsertAtEndEmptySingle = """
Consumer[] listConsumers() {
    return new Consumer[] {
        new SingleConsumer()
    }
}
"""

    val resultInsertAtEndEmptyMulti = """
Consumer[] listConsumers() {
    return new Consumer[] {
        new OneConsumer(),
        new TwoConsumer()
    }
}
"""

    val resultDedupWithEntries = """
Consumer[] listConsumers() {
    return new Consumer[] {
        new FooConsumer(),
        new BarConsumer()
    }
}
"""

    val resultInsertAtEndWithEntriesSingle = """
Consumer[] listConsumers() {
    return new Consumer[] {
        new FooConsumer(),
        new FooConsumer(),
        new BarConsumer(),
        new SingleConsumer()
    }
}
"""

    val resultInsertAtEndWithEntriesMulti = """
Consumer[] listConsumers() {
    return new Consumer[] {
        new FooConsumer(),
        new FooConsumer(),
        new BarConsumer(),
        new OneConsumer(),
        new TwoConsumer()
    }
}
"""

    val resultInsertAtStartEmptySingle = """
Consumer[] listConsumers() {
    return new Consumer[] {
        new SingleConsumer()
    }
}
"""

    val resultInsertAtStartEmptyMulti = """
Consumer[] listConsumers() {
    return new Consumer[] {
        new OneConsumer(),
        new TwoConsumer()
    }
}
"""

    val resultInsertAtStartWithEntriesSingle = """
Consumer[] listConsumers() {
    return new Consumer[] {
        new SingleConsumer(),
        new FooConsumer(),
        new FooConsumer(),
        new BarConsumer()
    }
}
"""

    val resultInsertAtStartWithEntriesMulti = """
Consumer[] listConsumers() {
    return new Consumer[] {
        new OneConsumer(),
        new TwoConsumer(),
        new FooConsumer(),
        new FooConsumer(),
        new BarConsumer()
    }
}
"""

    @Test
    fun `transform blocks id empty`() {
        test(blocks(startPattern, endPattern, id()), testContentsEmpty, testContentsEmpty)
    }

    @Test
    fun `transform blocks id entries`() {
        test(blocks(startPattern, endPattern, id()), testContentsWithEntries, testContentsWithEntries)
    }

    @Test
    fun `transform blocks dedup empty`() {
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
        test(blocks(startPattern, endPattern, dedup), resultDedupWithEntries, testContentsWithEntries)
    }

    @Test
    fun `transform blocks insertAtEnd empty single`() {
        test(blocks(startPattern, endPattern, insertAtEndOfList(insertedEntry)),
            resultInsertAtEndEmptySingle, testContentsEmpty)
    }

    @Test
    fun `transform blocks insertAtEnd empty single 2`() {
        test(
            blocks(startPattern, endPattern, insertAtEndOfList(insertedEntryWithComma)),
            resultInsertAtEndEmptySingle, testContentsEmpty
        )
    }

    @Test
    fun `transform blocks insertAtEnd empty multi`() {
        test(
            blocks(startPattern, endPattern, insertAtEndOfList(insertedEntries)),
            resultInsertAtEndEmptyMulti, testContentsEmpty
        )
    }

    @Test
    fun `transform blocks insertAtEnd empty multi 2`() {
        test(
            blocks(startPattern, endPattern, insertAtEndOfList(insertedEntriesWithCommas)),
            resultInsertAtEndEmptyMulti, testContentsEmpty
        )
    }

    @Test
    fun `transform blocks insertAtEnd entries single`() {
        test(
            blocks(startPattern, endPattern, insertAtEndOfList(insertedEntry)),
            resultInsertAtEndWithEntriesSingle, testContentsWithEntries
        )
    }

    @Test
    fun `transform blocks insertAtEnd entries single 2`() {
        test(
            blocks(startPattern, endPattern, insertAtEndOfList(insertedEntryWithComma)),
            resultInsertAtEndWithEntriesSingle, testContentsWithEntries
        )
    }

    @Test
    fun `transform blocks insertAtEnd entries multi`() {
        test(
            blocks(startPattern, endPattern, insertAtEndOfList(insertedEntries)),
            resultInsertAtEndWithEntriesMulti, testContentsWithEntries
        )
    }

    @Test
    fun `transform blocks insertAtEnd entries multi 2`() {
        test(
            blocks(startPattern, endPattern, insertAtEndOfList(insertedEntriesWithCommas)),
            resultInsertAtEndWithEntriesMulti, testContentsWithEntries
        )
    }

    @Test
    fun `transform blocks insertAtStart empty single`() {
        test(
            blocks(startPattern, endPattern, insertAtStartOfList(insertedEntry)),
            resultInsertAtStartEmptySingle, testContentsEmpty
        )
    }

    @Test
    fun `transform blocks insertAtStart empty single 2`() {
        test(
            blocks(startPattern, endPattern, insertAtStartOfList(insertedEntryWithComma)),
            resultInsertAtStartEmptySingle, testContentsEmpty
        )
    }

    @Test
    fun `transform blocks insertAtStart empty multi`() {
        test(
            blocks(startPattern, endPattern, insertAtStartOfList(insertedEntries)),
            resultInsertAtStartEmptyMulti, testContentsEmpty
        )
    }

    @Test
    fun `transform blocks insertAtStart empty multi 2`() {
        test(
            blocks(startPattern, endPattern, insertAtStartOfList(insertedEntriesWithCommas)),
            resultInsertAtStartEmptyMulti, testContentsEmpty
        )
    }

    @Test
    fun `transform blocks insertAtStart entries single`() {
        test(
            blocks(startPattern, endPattern, insertAtStartOfList(insertedEntry)),
            resultInsertAtStartWithEntriesSingle, testContentsWithEntries
        )
    }

    @Test
    fun `transform blocks insertAtStart entries single 2`() {
        test(
            blocks(startPattern, endPattern, insertAtStartOfList(insertedEntryWithComma)),
            resultInsertAtStartWithEntriesSingle, testContentsWithEntries
        )
    }

    @Test
    fun `transform blocks insertAtStart entries multi`() {
        test(
            blocks(startPattern, endPattern, insertAtStartOfList(insertedEntries)),
            resultInsertAtStartWithEntriesMulti, testContentsWithEntries
        )
    }

    @Test
    fun `transform blocks insertAtStart entries multi 2`() {
        test(
            blocks(startPattern, endPattern, insertAtStartOfList(insertedEntriesWithCommas)),
            resultInsertAtStartWithEntriesMulti, testContentsWithEntries
        )
    }

    private fun test(tr: Transformer, expected: String, contents: String) {
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
