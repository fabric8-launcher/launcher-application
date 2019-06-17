package io.fabric8.launcher.creator.catalog.generators

import io.fabric8.launcher.creator.core.*
import io.fabric8.launcher.creator.core.catalog.BaseGenerator
import io.fabric8.launcher.creator.core.catalog.CatalogItemContext
import io.fabric8.launcher.creator.core.catalog.enumItemNN
import io.fabric8.launcher.creator.core.resource.*

interface RuntimeSpringbootProps : LanguageJavaProps, MavenSetupProps {
    val runtime: Runtime

    companion object {
        @JvmOverloads fun build(_map: Properties = propsOf(), block: Data.() -> kotlin.Unit = {}) =
            BaseProperties.build(::Data, _map, block)
    }

    open class Data(map: Properties = propsOf()) : LanguageJavaProps.Data(map), RuntimeSpringbootProps {
        override val runtime: Runtime by _map
        override var maven: MavenCoords by _map
    }
}

class RuntimeSpringboot(ctx: CatalogItemContext) : BaseGenerator(ctx) {
    override fun apply(resources: Resources, props: Properties, extra: Properties): Resources {
        val psprops = RuntimeSpringbootProps.build(props)
        val jarName = psprops.maven.artifactId + '-' + psprops.maven.version + ".jar"
        val lprops = propsOf(
                psprops,
                "jarName" to jarName,
                "builderImage" to BUILDER_JAVA
        )

        // Check if the service already exists, so we don't create it twice
        if (resources.service(psprops.serviceName) == null) {
            generator(::RuntimeBaseSupport).apply(resources, psprops, extra)
            copy()
        }
        generator(::LanguageJava).apply(resources, lprops, extra)
        setMemoryLimit(resources, "1G", psprops.serviceName);
        setDefaultHealthChecks(resources, psprops.serviceName);
        generator(::MavenSetup).apply(resources, lprops, extra)

        val exProps = propsOf(
                "image" to BUILDER_JAVA,
                "enumInfo" to enumItemNN("runtime.name", "springboot"),
                "service" to psprops.serviceName,
                "route" to psprops.routeName
        )
        extra.pathPut("shared.runtimeInfo", exProps)

        return resources
    }
}
