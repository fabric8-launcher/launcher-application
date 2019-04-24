package it

import io.fabric8.launcher.creator.core.*
import io.fabric8.launcher.creator.core.catalog.*
import io.fabric8.launcher.creator.core.deploy.*
import io.restassured.RestAssured
import it.tests.CapabilityTest
import org.junit.jupiter.api.DynamicContainer
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import java.nio.file.Files
import java.nio.file.Path
import kotlin.streams.toList

// Put any capabilities here that need special options for testing.
// Each set of options in the array will be used in a separate test run
val capabilityOptions: CapabilityOptions = mapOf(
    "database" to listOf(
        propsOf("databaseType" to "postgresql"),
        propsOf("databaseType" to "mysql")
    )
)

class AllIntegrationTestsIT: IntegrationTests {
    @TestFactory
    override fun integrationTests(): Iterable<DynamicNode> {
        return sequence<DynamicNode> {
            var currProject: String? = null
            var setupOk = false
            
            yield(DynamicTest.dynamicTest("setup") {
                try {
                    runTestCmd("oc", "whoami")
                } catch (ex: Exception) {
                    throw Exception("You must be logged in to an OpenShift server to run the tests")
                }
                try {
                    currProject = runTestCmd("oc", "project", "-q").trim()
                } catch (ex: Exception) {
                    // Ignore any errors
                }
                setupOk = true
            })
            if (setupOk) {
                yield(
                    DynamicContainer.dynamicContainer(
                        "Backends",
                        testRuntimesCaps(getRuntimeVersions("backend"), getCapabilities("backend"))
                    )
                )
                yield(
                    DynamicContainer.dynamicContainer(
                        "Frontends",
                        testRuntimesCaps(getRuntimeVersions("frontend"), getCapabilities("frontend"))
                    )
                )
                yield(DynamicTest.dynamicTest("cleanup") {
                    if (currProject != null) {
                        // Try to restore the original project
                        try {
                            runTestCmd("oc", "project", currProject)
                        } catch (ex: Exception) {
                            // Ignore any errors
                        }
                    }
                })
            }
        }.asIterable()
    }
}

fun testRuntimesCaps(runtimes: List<Runtime>, capInfos: List<ModuleInfoDef>): List<DynamicNode> {
    return runtimes.flatMap { runtime ->
        val parts = listParts(runtime, capInfos)
        if (parts.isNotEmpty()) {
            testParts(parts)
        } else {
            listOf()
        }
    }
}

fun listParts(runtime: Runtime, capInfos: List<ModuleInfoDef>): List<Part> {
    val parts = mutableListOf<Part>()
    val rtCaps = capInfos.filter { d -> findPropertyWithValue(d, "runtime.name", runtime.name, listEnums()) != null }
    val caps = rtCaps.map { c -> c.module }
    val cOverrides = getCapabilityOverrides()

    fun actualCaps(idx: Int): List<CapabilityOpts> {
        return caps.map { c ->
            val co = capabilityOptions[c]
            if (co != null) {
                CapabilityOpts(c, co[idx % co.size])
            } else {
                CapabilityOpts(c)
            }
        }
    }

    if (rtCaps.isNotEmpty()) {
        val maxAlt = capabilityOptions
            .entries
            .filter { e -> caps.contains(e.key) }
            .fold(1) { acc, e -> Math.max(acc, e.value.size) }
        for (i in 0 until maxAlt) {
            if (cOverrides == null || cOverrides.contains("welcome")) {
                parts.add(Part.build {
                    this.runtime = runtime
                    capabilities = actualCaps(i) + CapabilityOpts("welcome")
                })
            } else {
                parts.add(Part.build {
                    this.runtime = runtime
                    capabilities = actualCaps(i)
                })
            }
        }
        parts.add(Part.build {
            this.runtime = runtime
            folder = "test"
            capabilities = actualCaps(0)
        })
    }
    return parts
}

fun testParts(parts: List<Part>): List<DynamicNode> {
    val rts = parts.groupBy { it.runtime?.name }
    return rts.keys.flatMap { rt ->
        val vs = rts.getValue(rt).groupBy { it.runtime?.version }
        vs.keys.map { v ->
            val tests = vs.getValue(v).flatMap { testPart(it) }
            DynamicContainer.dynamicContainer("Runtime $rt / $v", tests)
        }
    }
}

private fun deployment(part: Part): DeploymentDescriptor {
    return DeploymentDescriptor.build {
        applications = mutableListOf(
            ApplicationDescriptor.build {
                application = "ittest"
                parts = mutableListOf(
                    PartDescriptor.build {
                        subFolderName = part.folder
                        shared = propsOf(
                            "runtime" to part.runtime
                        )
                        capabilities = part.capabilities.map {
                            CapabilityDescriptor.build {
                                module = it.name
                                props = propsOf(it.opts)
                            }
                        }.toMutableList()
                    }
                )
            }
        )
    }
}

fun testPart(part: Part): Iterable<DynamicNode> {
    return sequence<DynamicNode> {
        var targetDir: Path? = null
        var projectName: String? = null

        fun cleanup() {
            val td = targetDir
            if (td != null) {
                System.out.println("Removing temporary folder...")
                td.toFile().deleteRecursively()
            }
            val pn = projectName
            if (pn != null) {
                System.out.println("Deleting project...")
                runTestCmd("oc", "delete", "project", pn)
            }
        }

        val context = Context()

        val folderMsg = if (part.folder != null) " in folder " + part.folder else ""
        val capNames = part.capabilities.joinToString { it.name }
        val name = "Capabilities $capNames$folderMsg"
        yield(DynamicContainer.dynamicContainer(name, sequence {
            var setupOk = true
            yield(DynamicContainer.dynamicContainer("setup", sequence {
                yield(DynamicTest.dynamicTest("Creating project...") {
                    try {
                        val td = Files.createTempDirectory("test")
                        targetDir = td
                        applyDeployment(td, deployment(part))
                        if (!isDryRun()) {
                            projectName =
                                targetDir?.fileName.toString().toLowerCase().replace("""[^A-Za-z0-9]""".toRegex(), "")
                            runTestCmd("oc", "new-project", projectName)
                        }
                    } catch (ex: Throwable) {
                        setupOk = false
                        throw ex
                    }
                })
                if (setupOk) yield(DynamicTest.dynamicTest("Deploying project...") {
                    try {
                        runTestCmd(targetDir, "./gap", "deploy")
                    } catch (ex: Throwable) {
                        setupOk = false
                        throw ex
                    }
                })
                if (setupOk && !isNoBuild()) yield(DynamicTest.dynamicTest("Building project...") {
                    try {
                        runTestCmd(targetDir, "./gap", "build")
                    } catch (ex: Throwable) {
                        setupOk = false
                        throw ex
                    }
                })
                if (setupOk) yield(DynamicTest.dynamicTest("Pushing project...") {
                    try {
                        waitForFirstBuild(part)
                        runTestCmd(targetDir, "./gap", "push", "--wait")
                        waitForProject(part)
                        context.routeHost = getRouteHost(getServiceName(part))
                    } catch (ex: Throwable) {
                        setupOk = false
                        throw ex
                    }
                })
            }.asIterable()))

            if (setupOk) yield(DynamicContainer.dynamicContainer("Testing capabilities...", sequence {
                yieldAll(part.capabilities.flatMap { testRuntimeCap(it, context) })
            }.asIterable()))

            yield(DynamicTest.dynamicTest("cleanup") {
                cleanup()
            })
        }.asIterable()))
    }.asIterable()
}

fun testRuntimeCap(capability: CapabilityOpts, context: Context): Iterable<DynamicNode> {
    val capTest = CapabilityTest.values().find { it.name == capability.name }
    return if (capTest != null) {
        RestAssured.baseURI = "http://${context.routeHost}"
        val tests = capTest.testsProvider(context).integrationTests()
        if (!isDryRun()) {
            tests
        } else {
            dummyTests(tests)
        }
    } else {
        listOf()
    }
}

private fun dummyTests(tests: Iterable<DynamicNode>): Iterable<DynamicNode> {
    return tests.map {
        when (it) {
            is DynamicTest -> DynamicTest.dynamicTest(it.displayName, it.testSourceUri.orElse(null)) { true }
            is DynamicContainer -> DynamicContainer.dynamicContainer(it.displayName, dummyTests(it.children.toList()))
            else -> throw Exception("Unsupported DynamicNode sub class")
        }
    }
}

