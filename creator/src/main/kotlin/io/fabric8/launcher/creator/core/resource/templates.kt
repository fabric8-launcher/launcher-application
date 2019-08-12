package io.fabric8.launcher.creator.core.resource

import io.fabric8.launcher.creator.core.*
import io.fabric8.launcher.creator.core.data.objectToString
import io.fabric8.launcher.creator.core.data.yamlIo
import io.fabric8.launcher.creator.core.oc.ocNewApp
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

private const val dummyName = "placeholder-app-name-730040e0c873453f877c10cd07912d1f"
private const val dummyLabel = "placeholder-app-label-d46881878f594a2dadfd963843452aab"
private val dummyNameRe = dummyName.toRegex(RegexOption.LITERAL)
private val dummyLabelRe = dummyLabel.toRegex(RegexOption.LITERAL)
private const val dummyGitUrl = "https://github.com/dummy_org/dummy_repo"

private val sourceRepoUrlParam = Parameter.build {
    name = "SOURCE_REPOSITORY_URL"
    description = "The source URL for the application"
    displayName = "Source URL"
    value = dummyGitUrl
    required = true
}

private val githubWebhookSecretParam = Parameter.build {
    name = "GITHUB_WEBHOOK_SECRET"
    description = "The secret used to configure a GitHub webhook"
    displayName = "GitHub Webhook Secret"
    required = true
    set("from", "[a-zA-Z0-9]{40}")
    set("generate", "expression")
}

private val buildTriggers = listOf(
    propsOf(
        "type" to "GitHub",
        "github" to propsOf(
            "secret" to "\${GITHUB_WEBHOOK_SECRET}"
        )
    ), propsOf(
        "type" to "ConfigChange"
    ), propsOf(
        "type" to "ImageChange",
        "imageChange" to propsOf()
    )
)

private fun normalizeImageName(name: String): String {
    return name.replace("""[/:]""".toRegex(), "_")
}

fun templatePath(name: String): Path {
    return Paths.get("META-INF/catalog/app-images/resources").resolve(normalizeImageName(name) + ".yaml")
}

fun generate() {
    var targetDir = Paths.get("src/main/resources")
    if (!Files.isDirectory(targetDir)) {
        targetDir = Paths.get("creator/src/main/resources")
        if (!Files.isDirectory(targetDir)) {
            throw IllegalThreadStateException("Missing target resources folder, please run in the root of the 'creator' project")
        }
    }
    images.forEach { img ->
        val image = img.id
        val metadata = img.metadata
        val isBuilder = metadata?.isBuilder ?: false
        try {
            val srcUri = if (isBuilder) dummyGitUrl else null
            val res = ocNewApp(dummyName, dummyLabel, image, srcUri)
            res.toTemplate()
            // Remove all openshift.io annotations
            res.items.forEach {
                filterAnnotations(it.metadata)
                filterAnnotations(it.pathGet<Properties>("spec.template.metadata"))
                it.pathGet<List<Properties>>("spec.tags")?.forEach { filterAnnotations(it) }
            }
            if (isBuilder) {
                // Turn the resources into a template and add parameters
                res
                    .addParam(sourceRepoUrlParam)
                    .addParam(githubWebhookSecretParam)
                // Make sure incremental builds are enabled
                res.buildConfig(dummyName)?.let {
                    it.pathPut("spec.strategy.sourceStrategy.incremental", true)
                    // Set the Git repo URL to use the template parameter
                    it.pathPut("spec.source.git.uri", "\${SOURCE_REPOSITORY_URL}")
                    // Set the GitHub webhook trigger to use the template parameter
                    it.pathPut("spec.triggers", buildTriggers)
                }
            }
            // Replace special symbols with patterns for later expansion
            val yamlStr = yamlIo.objectToString(res.json)
            val newYamlStr = yamlStr
                .replace(dummyNameRe, "{{.serviceName}}")
                .replace(dummyLabelRe, "{{.application}}")
            // Write the resources to a file
            val name = Paths.get("src/main/resources").resolve(templatePath(image))
            try {
                Files.createDirectories(name.parent)
                name.toFile().writeText(newYamlStr)
            } catch (ex: Exception) {
                System.err.println("Failed to write resources file ${name}: ${ex}")
                throw ex
            }
            System.out.println("Created image $image")
        } catch (ex: Exception) {
            System.err.println("Couldn't generate template for image $image $ex")
        }
    }
}

private fun filterAnnotations(container: Properties?) {
    container?.pathGet<Properties>("annotations")?.let {
        val filteredAnnotations = filterObject(it) { key, _ -> !key.startsWith("openshift.io/") }
        if (filteredAnnotations.isEmpty()) {
            container.remove("annotations")
        } else {
            container["annotations"] = filteredAnnotations
        }
    }
}
