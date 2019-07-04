package io.fabric8.launcher.creator.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import io.fabric8.launcher.creator.core.Properties
import io.fabric8.launcher.creator.core.deploy.*
import io.fabric8.launcher.creator.core.propsOf
import io.fabric8.launcher.creator.core.toRuntime
import java.nio.file.Paths

class Apply : CliktCommand(help = "Adds capabilities to projects") {
    val project: String by option(help = "Project folder").default(".")
    val name: String by option(help = "The name of the application").required()
    val folder: String? by option(help = "Optional subfolder to use for the capability")
    val runtime: String? by option(help = "The runtime or runtime/version to use for the application")
    val capabilities: List<String> by argument(help = "The names of the capabilities to apply. Each name can optionally be followed by a JSON object or flags").multiple(true)

    override fun run() {
        var currcap: String = ""
        val caps = capabilities.groupBy {
            if (it.startsWith("--")) {
                currcap
            } else {
                currcap = it
                it
            }
        }

        val deployment = DeploymentDescriptor.build {
            applications = mutableListOf(ApplicationDescriptor.build {
                application = name
                parts = mutableListOf(PartDescriptor.build {
                    subFolderName = folder
                    shared = propsOf(
                            "runtime" to toRuntime(runtime)
                    )
                    capabilities = caps.map {
                        CapabilityDescriptor.build {
                            module = it.key
                            if (it.value.size > 1) {
                                props = flagsToProps(it.value.drop(1))
                            }
                        }
                    }.toMutableList()
                })
            })
        }

        applyDeployment(Paths.get(project), deployment)
        echo("Applied capability to '${project}'")
        echo("Go into that folder and type './gap deploy' while logged into OpenShift to create the application")
        echo("in the currently active project. Afterwards type './gap push' at any time to push the current")
        echo("application code to the project.")
    }

    private fun flagsToProps(flags: List<String>): Properties {
        return propsOf(*flags
                .filter { it.startsWith("--") }
                .map { it.substring(2) }
                .map {
                    val parts = it.split('=', limit = 2)
                    val key = parts[0]
                    if (parts.size == 2) {
                        val value: Any = if (parts[1] == "true") {
                            true
                        } else if (parts[1] == "false") {
                            false
                        } else if (parts[1].toIntOrNull() != null) {
                            parts[1].toInt()
                        } else if (parts[1].startsWith('"') && parts[1].endsWith('"')) {
                            parts[1].substring(1, parts[1].length - 1)
                        } else {
                            parts[1]
                        }
                        Pair(key, value)
                    } else {
                        Pair(key, true)
                    }
                }.toTypedArray())
    }
}
