package it

import io.fabric8.launcher.creator.core.Runtime
import io.fabric8.launcher.creator.catalog.capabilities.CapabilityInfo
import io.fabric8.launcher.creator.core.catalog.ModuleInfoDef
import io.fabric8.launcher.creator.core.catalog.enumNN
import io.fabric8.launcher.creator.core.pathGet
import io.fabric8.launcher.creator.core.runCmd
import java.nio.file.Path

fun getRuntimes(tier: String): List<String> {
    val rtOverrides = getRuntimeOverrides()
    return enumNN("runtime.name")
        .filter { e -> e.pathGet<List<String>>("metadata.categories", listOf()).contains(tier) }
        .map { e -> e.id }
        .filter { rtid -> rtOverrides == null || rtOverrides.contains(rtid) }
}

fun getRuntimeVersions(tier: String): List<Runtime> {
    val rts = getRuntimes(tier)
    return rts.flatMap { rt ->
        enumNN("runtime.version.$rt").map { v ->
            Runtime.build {
                name = rt
                version = v.id
            }
        }
    }
}

fun getCapabilities(tier: String): List<ModuleInfoDef> {
    val cOverrides = getCapabilityOverrides()
    val cis = CapabilityInfo.values()
    return cis
        .map { ci -> ci.info }
        .filter { inf -> inf.pathGet("metadata.category", "") == tier }
        .filter { inf -> cOverrides == null || cOverrides.contains(inf.module) }
}

fun getServiceName(part: Part): String {
    return if (part.folder != null) "ittest-${part.folder}" else "ittest"
}

fun runTestCmd(cmd: String, vararg args: String?): String {
    return runTestCmd(null, cmd, *args)
}

fun runTestCmd(cwd: Path?, cmd: String, vararg args: String?): String {
    if (isVerbose()) {
        val cmdTxt = "$cmd ${args.joinToString(" ")}"
        System.out.println("      Run '$cmdTxt'")
    }
    if (!isDryRun()) {
        try {
            return runCmd(cwd, cmd, *args)
        } catch (ex: Exception) {
            if (isVerbose()) {
                System.err.println("ERROR: ${ex.message}")
            }
            throw ex
        }
    } else {
        return ""
    }
}
