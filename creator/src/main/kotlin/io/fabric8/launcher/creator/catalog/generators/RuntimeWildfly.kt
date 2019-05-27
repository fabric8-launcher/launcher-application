package io.fabric8.launcher.creator.catalog.generators

import io.fabric8.launcher.creator.core.*
import io.fabric8.launcher.creator.core.catalog.BaseGenerator
import io.fabric8.launcher.creator.core.catalog.CatalogItemContext
import io.fabric8.launcher.creator.core.catalog.enumItemNN
import io.fabric8.launcher.creator.core.resource.*

interface RuntimeWildflyProps : LanguageJavaProps, MavenSetupProps {
    val runtime: Runtime

    companion object {
        fun build(_map: Properties = propsOf(), block: Data.() -> kotlin.Unit = {}) =
            BaseProperties.build(::Data, _map, block)
    }

    open class Data(map: Properties = propsOf()) : LanguageJavaProps.Data(map), RuntimeWildflyProps {
        override val runtime: Runtime by _map
        override var maven: MavenCoords by _map
    }
}

class RuntimeWildfly(ctx: CatalogItemContext) : BaseGenerator(ctx) {
    override fun apply(resources: Resources, props: Properties, extra: Properties): Resources {
        val pwprops = RuntimeWildflyProps.build(props)
        val jarName = "ROOT.war"
        val lprops = propsOf(
                pwprops,
                "jarName" to jarName,
                "builderImage" to BUILDER_JAVAEE
        )

        // Check if the service already exists, so we don't create it twice
        if (resources.service(pwprops.serviceName) == null) {
            generator(::RuntimeBaseSupport).apply(resources, pwprops, extra)
            copy()
        }
        generator(::LanguageJava).apply(resources, lprops, extra)
        setMemoryLimit(resources, "1G", pwprops.serviceName);
        setDefaultHealthChecks(resources, pwprops.serviceName);
        generator(::MavenSetup).apply(resources, lprops, extra)

        val exProps = propsOf(
                "image" to BUILDER_JAVAEE,
                "enumInfo" to enumItemNN("runtime.name", "wildfly"),
                "service" to pwprops.serviceName,
                "route" to pwprops.routeName
        )
        extra.pathPut("shared.runtimeInfo", exProps)

        return resources
    }
}
