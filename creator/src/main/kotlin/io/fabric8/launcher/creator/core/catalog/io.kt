package io.fabric8.launcher.creator.core.catalog

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import io.fabric8.launcher.creator.core.streamFromPath
import java.nio.file.Path
import java.nio.file.Paths


fun readCapabilityInfoDef(capabilityName: String): ModuleInfoDef {
    try {
        val folderName = capabilityName.replace("-", "")
        return readInfoDef(capabilityName, Paths.get("io/fabric8/launcher/creator/catalog/capabilities/${folderName}/info.json"))
    } catch (ex: Exception) {
        throw RuntimeException("No info found for capability '${capabilityName}'", ex)
    }
}

fun readGeneratorInfoDef(generatorName: String): ModuleInfoDef {
    try {
        val folderName = generatorName.replace("-", "")
        return readInfoDef(generatorName, Paths.get("io/fabric8/launcher/creator/catalog/generators/${folderName}/info.json"))
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
