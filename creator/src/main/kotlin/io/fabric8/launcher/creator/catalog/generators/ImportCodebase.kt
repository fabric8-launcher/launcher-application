package io.fabric8.launcher.creator.catalog.generators

import io.fabric8.launcher.creator.core.*
import io.fabric8.launcher.creator.core.analysis.cloneGitRepo
import io.fabric8.launcher.creator.core.analysis.determineBuilderImage
import io.fabric8.launcher.creator.core.analysis.removeGitFolder
import io.fabric8.launcher.creator.core.analysis.withGitRepo
import io.fabric8.launcher.creator.core.catalog.BaseGenerator
import io.fabric8.launcher.creator.core.catalog.BaseGeneratorProps
import io.fabric8.launcher.creator.core.catalog.CatalogItemContext
import io.fabric8.launcher.creator.core.resource.*

// Returns the corresponding language generator depending on the given builder image
private fun languageByBuilder(builder: BuilderImage): (CatalogItemContext) -> BaseGenerator {
    return when {
        builder.metadata?.language == "java" -> ::LanguageJava
        builder.metadata?.language == "nodejs" -> ::LanguageNodejs
        builder.metadata?.language == "csharp" -> ::LanguageCsharp
        else -> throw IllegalArgumentException("Unsupported builder: $builder")
    }
}

interface ImportCodebaseProps : BaseGeneratorProps {
    val gitImportUrl: String?
    val gitImportBranch: String?
    val builderImage: String?
    val builderLanguage: String?
    val env: Environment?
    val overlayOnly: Boolean?
    val keepGitFolder: Boolean?
    val dotnet: DotnetCoords?
    val maven: MavenCoords?
    val nodejs: NodejsCoords?

    companion object {
        @JvmOverloads fun build(_map: Properties = propsOf(), block: Data.() -> Unit = {}) =
            BaseProperties.build(::Data, _map, block)
    }

    open class Data(map: Properties = propsOf()) : BaseGeneratorProps.Data(map), ImportCodebaseProps {
        override var gitImportUrl: String? by _map
        override var gitImportBranch: String? by _map
        override var builderImage: String? by _map
        override var builderLanguage: String? by _map
        override var env: Environment? by _map
        override var overlayOnly: Boolean? by _map
        override var keepGitFolder: Boolean? by _map
        override var dotnet: DotnetCoords? by _map
        override var maven: MavenCoords? by _map
        override var nodejs: NodejsCoords? by _map
    }
}

class ImportCodebase(ctx: CatalogItemContext) : BaseGenerator(ctx) {
    override fun apply(resources: Resources, props: Properties, extra: Properties): Resources {
        val icprops = ImportCodebaseProps.build(props)
        val importUrl = icprops.gitImportUrl
        var image = builderById(icprops.builderImage) ?: builderByLanguage(icprops.builderLanguage)
        if (importUrl != null) {
            if (icprops.overlayOnly == true) {
                if (image == null) {
                    image = withGitRepo(importUrl, icprops.gitImportBranch) {
                        determineBuilderImage(this)
                    }
                }
            } else {
                cloneGitRepo(targetDir, importUrl, icprops.gitImportBranch)
                if (image == null) {
                    image = determineBuilderImage(targetDir)
                }
                if (icprops.keepGitFolder != true) {
                    removeGitFolder(targetDir)
                }
            }
        }
        if (image == null) {
            throw IllegalStateException("Unable to determine builder image")
        }
        val res: Resources
        if (image.id == MARKER_BOOSTER_IMPORT) {
            res = readResources(targetDir.resolve(".openshiftio/application.yaml"))
            setBuildEnv(res, icprops.env)
            setDeploymentEnv(res, icprops.env)
        } else {
            val lprops = propsOf(
                icprops,
                "builderImage" to image.id,
                "binaryExt" to image.metadata?.binaryExt
            )
            res = generator(languageByBuilder(image)).apply(resources, lprops, extra)
        }
        if (importUrl != null) {
            val param = res.parameter("SOURCE_REPOSITORY_URL")
            if (param != null) {
                param.value = importUrl
            }
        }
        return res
    }
}
