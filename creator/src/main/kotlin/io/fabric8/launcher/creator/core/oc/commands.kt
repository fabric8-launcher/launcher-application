package io.fabric8.launcher.creator.core.oc

import io.fabric8.launcher.creator.core.Environment
import io.fabric8.launcher.creator.core.Properties
import io.fabric8.launcher.creator.core.data.yamlIo
import io.fabric8.launcher.creator.core.envOf
import io.fabric8.launcher.creator.core.resource.Resources

// Returns a list of resources that when applied will create
// an instance of the given image or template.
fun ocNewApp(name: String, appName: String, imageName: String, sourceUri: String?, env: Environment = envOf()): Resources {
    val img = if (sourceUri != null) "$imageName~$sourceUri" else imageName
    val envArgs = env.entries.map { entry -> "-e${entry.key}=${entry.value}" }

    val cmd = mutableListOf("oc", "new-app",
        "--name=${name}",
        "--labels=app=${appName}",
        "--dry-run",
        "-o", "yaml",
        *envArgs.toTypedArray(),
        img
    )

    val pb = ProcessBuilder(cmd)
    val p = pb.start()
    p.waitFor()

    if (p.exitValue() != 0) {
        val err = p.errorStream.bufferedReader().use { it.readText() }
        val out = p.inputStream.bufferedReader().use { it.readText() }
        val str = if (err.isBlank()) out else err
        throw Exception("Failed to run '${cmd.joinToString(" ")}': $str")
    }

    //val out = p.inputStream.bufferedReader().use { it.readText() }
    val obj = p.inputStream.use { yamlIo.objectFromStream(it) }
    return Resources(obj as Properties)
}
