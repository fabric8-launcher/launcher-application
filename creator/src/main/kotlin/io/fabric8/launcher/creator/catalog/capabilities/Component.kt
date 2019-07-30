package io.fabric8.launcher.creator.catalog.capabilities

import io.fabric8.launcher.creator.core.*
import io.fabric8.launcher.creator.core.catalog.BaseCapability
import io.fabric8.launcher.creator.core.catalog.CatalogItemContext
import io.fabric8.launcher.creator.catalog.generators.GeneratorInfo
import io.fabric8.launcher.creator.core.resource.Resources

class Component(info: CapabilityInfo, ctx: CatalogItemContext) : BaseCapability(info, ctx) {
    override fun apply(resources: Resources, props: Properties, extra: Properties): Resources {
        val gen = GeneratorInfo.valueOf(props["generator"] as String)
        val appName = name(props["application"] as String, props["subFolderName"] as String?)
        val rtServiceName = appName
        val rtRouteName = appName
        val gprops = propsOf(
            props,
            "serviceName" to rtServiceName,
            "routeName" to rtRouteName,
            "runtime" to props["runtime"]?.let { Runtime.build(it as Properties) }
        )
        return generator(gen).apply(resources, gprops, extra)
    }
}
