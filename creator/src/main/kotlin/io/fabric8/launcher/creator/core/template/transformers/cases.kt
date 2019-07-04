package io.fabric8.launcher.creator.core.template.transformers

import io.fabric8.launcher.creator.core.Properties
import io.fabric8.launcher.creator.core.pathGet
import io.fabric8.launcher.creator.core.template.Transformer

//
// Transformer that can filter special if-structures from a file
// determining whether to include or exclude those blocks of text
// depending on certain conditions.
// It does this by looking for lines that start with a line comment
// and a special token (by default "//$$" and then determines what
// to do with the following lines. Possible options are:
//
// {{if .keyName==value}}
//   If the property "keyName" has the value "value" in the given property
//   map then all following lines until the end of the block will be included
//   otherwise they will be dropped. All lines will have the first line
//   comments stripped. The block lasts until the next special token.
// {{else if .keyName==value}}
//   Just like in programming language the if will be tested if the previous
//   if-structure evaluated to false. The block will be included in the
//   output only when the if evaluates to true.
// {{else}}
//   Similarly an else-structure will be included in the output if all
//   previous if-structures evaluated to false.
// {{end}}
//   Signals the end of an if-block
// {{.keyName}}
//   Is replaced with the value of the property with the given name
//
// Example:
//
// function connect(host) {
// //{{if database==postgresql}}
//     return ConnectionManager.connect("jdbc:postgresql" + host);
// //{{else if database==mysql}}
// //    return ConnectionManager.connect("jdbc:mysql" + host);
// //{{end}}
// }
//
fun cases(props: Properties, lineComment: String = "//"): Transformer {
    var inIf = false
    var skipElse = false
    var skipBlock = false
    var foundElse = false

    val start = "$lineComment{{"
    val end = "}}"

    return { lines -> lines.flatMap { line ->
        val trimmedLine = line.trim()
        var skipLine = false

        // Check if this is a special "command line"
        if (trimmedLine.startsWith(start) && trimmedLine.endsWith(end)) {
            val inner = trimmedLine.substring(start.length, trimmedLine.length-end.length).trim()
            if (inner.startsWith("if ")) {
                if (inIf) {
                    throw IllegalArgumentException("if cannot be nested")
                }
                inIf = true
                skipBlock = !testCondition(inner.substring(3), props)
                skipElse = !skipBlock
                foundElse = false
            } else if (inner.startsWith("else if ")) {
                if (!inIf) {
                    throw IllegalArgumentException("else-if without if")
                }
                if (foundElse) {
                    throw IllegalArgumentException("else-if after else")
                }
                if (!skipElse) {
                    skipBlock = !testCondition(inner.substring(8), props)
                    skipElse = !skipBlock
                } else {
                    skipBlock = true
                }
            } else if (inner == "else") {
                if (!inIf) {
                    throw IllegalArgumentException("else without if")
                }
                skipBlock = skipElse
                foundElse = true
            } else if (inner == "end") {
                if (!inIf) {
                    throw IllegalArgumentException("end without if")
                }
                inIf = false
            }
            skipLine = true
        }

        // Perform any variable replacements
        val re = """\{\{\s*\.([a-zA-Z0-9-.]+)\s*}}""".toRegex()
        val line2 = re.replace(line) {
            props.pathGet<Any>(it.groupValues[1], "").toString()
        }

        if (skipLine || (inIf && skipBlock)) {
            sequenceOf()
        } else if (inIf) {
            var ln = line2
            // Remove any leading comment characters from the line
            if (ln.trim().startsWith(lineComment)) {
                val idx = ln.indexOf(lineComment)
                ln = ln.substring(0, idx) + ln.substring(idx + lineComment.length)
            }
            sequenceOf(ln)
        } else {
            sequenceOf(line2)
        }
    }}
}

fun testCondition(cond: String, props: Properties): Boolean {
    val parts = cond.split("==")
    val key = parts[0].trim().substring(1)
    val strval = props.pathGet<Any>(key)?.toString()
    return if (parts.size > 1) {
        strval == parts[1].trim()
    } else {
        strval != null
    }
}
