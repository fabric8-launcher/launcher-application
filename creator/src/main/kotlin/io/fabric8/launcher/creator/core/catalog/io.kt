package io.fabric8.launcher.creator.core.catalog

import io.fabric8.launcher.creator.core.data.objectFromPath
import io.fabric8.launcher.creator.core.data.yamlIo
import io.fabric8.launcher.creator.core.deepClone
import java.nio.file.Path
import java.nio.file.Paths

fun readCapabilityInfoDef(capabilityName: String): ModuleInfoDef {
    try {
        return readInfoDef(capabilityName, Paths.get("META-INF/catalog/capabilities/${capabilityName}/info.yaml"))
    } catch (ex: Exception) {
        throw RuntimeException("No info found for capability '${capabilityName}'", ex)
    }
}

fun readGeneratorInfoDef(generatorName: String): ModuleInfoDef {
    try {
        return readInfoDef(generatorName, Paths.get("META-INF/catalog/generators/${generatorName}/info.yaml"))
    } catch (ex: Exception) {
        throw RuntimeException("No info found for generator '${generatorName}'", ex)
    }
}

private fun readInfoDef(name: String, infoFile: Path): ModuleInfoDef {
    val obj = yamlIo.objectFromPath(infoFile).deepClone()
    obj["module"] = name
    return ModuleInfoDef.build(obj)
}
