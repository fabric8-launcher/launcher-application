package io.fabric8.launcher.creator.catalog.generators

import io.fabric8.launcher.creator.core.*
import io.fabric8.launcher.creator.core.catalog.BaseGenerator
import io.fabric8.launcher.creator.core.catalog.CatalogItemContext
import io.fabric8.launcher.creator.core.catalog.enumItemNN
import io.fabric8.launcher.creator.core.resource.*
import io.fabric8.launcher.creator.core.template.transformers.cases
import java.nio.file.Paths

interface PlatformDotnetProps : LanguageCsharpProps {
    val runtime: Runtime
    val dotnet: DotnetCoords

    companion object {
        fun build(_map: Properties = propsOf(), block: Data.() -> Unit = {}) =
            BaseProperties.build(::Data, _map, block)
    }

    open class Data(map: Properties = propsOf()) : LanguageCsharpProps.Data(map), PlatformDotnetProps {
        override val runtime: Runtime by _map
        override val dotnet: DotnetCoords by _map
    }
}

class PlatformDotnet(ctx: CatalogItemContext) : BaseGenerator(ctx) {
    override fun apply(resources: Resources, props: Properties, extra: Properties): Resources {
        val pdprops = PlatformDotnetProps.build(props)
        val lprops = LanguageCsharpProps.build(pdprops) {
            builderImage = BUILDER_DOTNET
        }

        // Check if the service already exists, so we don't create it twice
        if (resources.service(pdprops.serviceName) == null) {
            generator(::PlatformBaseSupport).apply(resources, pdprops, extra)
            copy()
            transform(listOf("**/*.cs", "files.csproj"), cases(pdprops))
            move(Paths.get("files.csproj"), Paths.get(pdprops.application + ".csproj"))
        }
        val res = generator(::LanguageCsharp).apply(resources, lprops, extra)
        setMemoryLimit(res, "512M")
        setDefaultHealthChecks(resources, pdprops.serviceName)

        val exProps = propsOf(
                "image" to BUILDER_DOTNET,
                "enumInfo" to enumItemNN("runtime.name", "dotnet"),
                "service" to pdprops.serviceName,
                "route" to pdprops.routeName
        )
        extra.pathPut("shared.runtimeInfo", exProps)

        return res
    }
}
