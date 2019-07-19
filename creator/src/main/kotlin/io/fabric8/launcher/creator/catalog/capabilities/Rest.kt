package io.fabric8.launcher.creator.catalog.capabilities

import io.fabric8.launcher.creator.core.*
import io.fabric8.launcher.creator.core.catalog.BaseCapability
import io.fabric8.launcher.creator.core.catalog.CatalogItemContext
import io.fabric8.launcher.creator.catalog.generators.GeneratorInfo
import io.fabric8.launcher.creator.core.resource.Resources

// Returns the corresponding runtime generator depending on the given runtime type
private fun runtimeByType(rt: Runtime): GeneratorInfo {
    return GeneratorInfo.valueOf("rest-${rt.name}")
}

class Rest(ctx: CatalogItemContext) : BaseCapability(ctx) {
    override fun apply(resources: Resources, props: Properties, extra: Properties): Resources {
        val appName = name(props["application"] as String, props["subFolderName"] as String?)
        val rtServiceName = appName
        val rtRouteName = appName
        val rt = props["runtime"].let { Runtime.build(it as Properties) }
        val rtprops = propsOf(
            "application" to props["application"],
            "subFolderName" to props["subFolderName"],
            "serviceName" to rtServiceName,
            "routeName" to rtRouteName,
            "runtime" to rt,
            "maven" to props["maven"]?.let { MavenCoords.build(it as Properties) },
            "nodejs" to props["nodejs"]?.let { NodejsCoords.build(it as Properties) },
            "dotnet" to props["dotnet"]?.let { DotnetCoords.build(it as Properties) }
        )
        return generator(runtimeByType(rt)).apply(resources, rtprops, extra)
    }
}
