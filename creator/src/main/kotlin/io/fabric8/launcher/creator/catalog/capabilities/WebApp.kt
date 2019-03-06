package io.fabric8.launcher.creator.catalog.capabilities

import io.fabric8.launcher.creator.core.*
import io.fabric8.launcher.creator.core.catalog.BaseCapability
import io.fabric8.launcher.creator.core.catalog.CatalogItemContext
import io.fabric8.launcher.creator.core.catalog.GeneratorConstructor
import io.fabric8.launcher.creator.catalog.generators.GeneratorInfo
import io.fabric8.launcher.creator.core.resource.Resources

// Returns the corresponding runtime generator depending on the given runtime type
private fun runtimeByType(rt: Runtime): GeneratorConstructor {
    return GeneratorInfo.valueOf("platform-${rt.name}").klazz
}

class WebApp(ctx: CatalogItemContext) : BaseCapability(ctx) {
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
            "nodejs" to NodejsCoords.build {
                name = props["application"] as String
                version = "1.0.0"
            }
        )
        return generator(runtimeByType(rt)).apply(resources, rtprops, extra)
    }
}
