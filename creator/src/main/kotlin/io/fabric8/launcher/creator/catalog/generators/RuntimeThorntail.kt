package io.fabric8.launcher.creator.catalog.generators

import io.fabric8.launcher.creator.catalog.generators.GeneratorInfo.*
import io.fabric8.launcher.creator.core.*
import io.fabric8.launcher.creator.core.catalog.BaseGenerator
import io.fabric8.launcher.creator.core.catalog.CatalogItemContext
import io.fabric8.launcher.creator.core.catalog.enumItemNN
import io.fabric8.launcher.creator.core.resource.*

interface RuntimeThorntailProps : LanguageJavaProps, MavenSetupProps {
    val runtime: Runtime

    companion object {
        @JvmOverloads fun build(_map: Properties = propsOf(), block: Data.() -> Unit = {}) =
            BaseProperties.build(::Data, _map, block)
    }

    open class Data(map: Properties = propsOf()) : LanguageJavaProps.Data(map), RuntimeThorntailProps {
        override val runtime: Runtime by _map
        override var maven: MavenCoords by _map
    }
}

class RuntimeThorntail(ctx: CatalogItemContext) : BaseGenerator(ctx) {
    override fun apply(resources: Resources, props: Properties, extra: Properties): Resources {
        val ptprops = RuntimeThorntailProps.build(props)
        val jarName = ptprops.maven.artifactId + '-' + ptprops.maven.version + "-thorntail.jar"
        val lprops = propsOf(
                ptprops,
                "jarName" to jarName,
                "builderImage" to BUILDER_JAVA
        )

        // Check if the service already exists, so we don't create it twice
        if (resources.service(ptprops.serviceName) == null) {
            generator(`runtime-base-support`).apply(resources, ptprops, extra)
            copy()
        }
        generator(`language-java`).apply(resources, lprops, extra)
        setMemoryLimit(resources, "1G", ptprops.serviceName)
        setDefaultHealthChecks(resources, ptprops.serviceName)
        generator(`maven-setup`).apply(resources, lprops, extra)

        val exProps = propsOf(
                "image" to BUILDER_JAVA,
                "enumInfo" to enumItemNN("runtime.name", "thorntail"),
                "service" to ptprops.serviceName,
                "route" to ptprops.routeName
        )
        extra.pathPut("shared.runtimeInfo", exProps)

        return resources
    }
}
