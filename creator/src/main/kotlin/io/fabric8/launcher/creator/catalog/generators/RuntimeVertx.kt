package io.fabric8.launcher.creator.catalog.generators

import io.fabric8.launcher.creator.catalog.generators.GeneratorInfo.*
import io.fabric8.launcher.creator.core.*
import io.fabric8.launcher.creator.core.catalog.BaseGenerator
import io.fabric8.launcher.creator.core.catalog.CatalogItemContext
import io.fabric8.launcher.creator.core.catalog.enumItemNN
import io.fabric8.launcher.creator.core.resource.*

interface RuntimeVertxProps : LanguageJavaProps, MavenSetupProps {
    val runtime: Runtime

    companion object {
        @JvmOverloads fun build(_map: Properties = propsOf(), block: Data.() -> Unit = {}) =
            BaseProperties.build(::Data, _map, block)
    }

    open class Data(map: Properties = propsOf()) : LanguageJavaProps.Data(map), RuntimeVertxProps {
        override val runtime: Runtime by _map
        override var maven: MavenCoords by _map
    }
}

class RuntimeVertx(ctx: CatalogItemContext) : BaseGenerator(ctx) {
    override fun apply(resources: Resources, props: Properties, extra: Properties): Resources {
        val pvprops = RuntimeVertxProps.build(props)
        val jarName = pvprops.maven.artifactId + '-' + pvprops.maven.version + ".jar"
        val lprops = propsOf(
                pvprops,
                "jarName" to jarName,
                "builderImage" to BUILDER_JAVA
        )

        // Check if the service already exists, so we don't create it twice
        if (resources.service(pvprops.serviceName) == null) {
            generator(`runtime-base-support`).apply(resources, pvprops, extra)
            copy()
            mergeVersionPoms(pvprops.runtime)
        }
        generator(`language-java`).apply(resources, lprops, extra)
        setMemoryLimit(resources, "1G", pvprops.serviceName)
        setDefaultHealthChecks(resources, pvprops.serviceName)
        generator(`maven-setup`).apply(resources, lprops, extra)

        val exProps = propsOf(
                "image" to BUILDER_JAVA,
                "enumInfo" to enumItemNN("runtime.name", "vertx"),
                "service" to pvprops.serviceName,
                "route" to pvprops.routeName
        )
        extra.pathPut("shared.runtimeInfo", exProps)

        return resources
    }
}
