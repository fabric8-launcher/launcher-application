package io.fabric8.launcher.creator.core.catalog

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import io.fabric8.launcher.creator.core.streamFromPath
import java.nio.file.Path
import java.nio.file.Paths


fun readCapabilityInfoDef(capabilityName: String): InfoDef {
    try {
        val folderName = capabilityName.replace("-", "")
        return readInfoDef(Paths.get("io/fabric8/launcher/creator/catalog/capabilities/${folderName}/info.json"))
    } catch (ex: Exception) {
        throw RuntimeException("No info found for capability '${capabilityName}'", ex)
    }
}

fun readGeneratorInfoDef(generatorName: String): InfoDef {
    try {
        val folderName = generatorName.replace("-", "")
        return readInfoDef(Paths.get("io/fabric8/launcher/creator/catalog/generators/${folderName}/info.json"))
    } catch (ex: Exception) {
        throw RuntimeException("No info found for generator '${generatorName}'", ex)
    }
}

fun readInfoDef(infoFile: Path): InfoDef {
    streamFromPath(infoFile).use {
        return InfoDef.build(Parser.default().parse(it) as JsonObject)
    }
}
