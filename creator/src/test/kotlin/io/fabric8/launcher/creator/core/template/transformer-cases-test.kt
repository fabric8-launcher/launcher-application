package io.fabric8.launcher.creator.core.template

import io.fabric8.launcher.creator.core.Properties
import io.fabric8.launcher.creator.core.propsOf
import io.fabric8.launcher.creator.core.template.transformers.cases
import org.assertj.core.api.Assertions
import org.junit.Test
import java.io.File

class TransformerCasesTest {

    val testContents = """
    function connect(host) {
    //{{if .database == postgresql}}
        return ConnectionManager.connect("jdbc:postgresql" + host);
    //{{else if .database==mysql }}
    //    return ConnectionManager.connect("jdbc:mysql" + host);
    //{{else if .booleanOpt }}
    //    throw new Exception("Dummy option {{.booleanOpt}}");
    //{{else}}
    //    throw new Exception("Not implemented {{.nested.opt}}-{{.doesntexist}}-{{.empty}}-{{.nullish}}");
    //{{end}}
    }
"""

    val resultPostgresql = """
    function connect(host) {
        return ConnectionManager.connect("jdbc:postgresql" + host);
    }
"""

    val resultMysql = """
    function connect(host) {
        return ConnectionManager.connect("jdbc:mysql" + host);
    }
"""

    val resultBool = """
    function connect(host) {
        throw new Exception("Dummy option true");
    }
"""

    val resultElse = """
    function connect(host) {
        throw new Exception("Not implemented yet---");
    }
"""

    val testContentsDoubleSlashes = """
    function connect(host) {
    //{{if .database == postgresql}}
        return ConnectionManager.connect("jdbc:postgresql://" + host);
    //{{end}}
    }
"""

    val resultDoubleSlashes = """
    function connect(host) {
        return ConnectionManager.connect("jdbc:postgresql://" + host);
    }
"""

    @Test
    fun `transform cases compare 1`() {
        val props = propsOf(
            "database" to "postgresql"
        )
        test(props, resultPostgresql)
    }

    @Test
    fun `transform cases compare 2`() {
        val props = propsOf(
            "database" to "mysql"
        )
        test(props, resultMysql)
    }

    @Test
    fun `transform cases exist`() {
        val props = propsOf(
            "booleanOpt" to true
        )
        test(props, resultBool)
    }

    @Test
    fun `transform cases else`() {
        val props = propsOf(
            "nested" to propsOf(
                "opt" to "yet"
            ),
            "empty" to "",
            "nullish" to null
        )
        test(props, resultElse)
    }

    @Test
    fun `transform line with double slashes`() {
        val props = propsOf(
            "database" to "postgresql"
        )
        test(props, resultDoubleSlashes, testContentsDoubleSlashes)
    }

    private fun test(props: Properties, expected: String, contents: String = testContents) {
        // Write test file
        val tmp = File.createTempFile("test", "tmp")
        try {
            tmp.deleteOnExit()
            tmp.writeText(contents)

            transform(tmp.toPath(), tmp.toPath(), cases(props))

            val current = tmp.readText()
            Assertions.assertThat(current).isEqualTo(expected)
        } finally {
            tmp.delete()
        }
    }
}
