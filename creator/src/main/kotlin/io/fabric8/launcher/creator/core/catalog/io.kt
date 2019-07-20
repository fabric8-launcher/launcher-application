package io.fabric8.launcher.creator.core.catalog

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import io.fabric8.launcher.creator.core.streamFromPath
import java.nio.file.Path
import java.nio.file.Paths


fun readCapabilityInfoDef(capabilityName: String): ModuleInfoDef {
    try {
        return readInfoDef(capabilityName, Paths.get("META-INF/catalog/capabilities/${capabilityName}/info.json"))
    } catch (ex: Exception) {
        throw RuntimeException("No info found for capability '${capabilityName}'", ex)
    }
}

fun readGeneratorInfoDef(generatorName: String): ModuleInfoDef {
    try {
        return readInfoDef(generatorName, Paths.get("META-INF/catalog/generators/${generatorName}/info.json"))
    } catch (ex: Exception) {
        throw RuntimeException("No info found for generator '${generatorName}'", ex)
    }
}

private fun readInfoDef(name: String, infoFile: Path): ModuleInfoDef {
    streamFromPath(infoFile).use {
        val obj = Parser.default().parse(it) as JsonObject
        obj["module"] = name
        return ModuleInfoDef.build(obj)
    }
}
