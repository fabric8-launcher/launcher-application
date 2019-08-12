package io.fabric8.launcher.creator.catalog

import io.fabric8.launcher.creator.core.*
import io.fabric8.launcher.creator.core.catalog.BaseGenerator
import io.fabric8.launcher.creator.core.catalog.GeneratorContext
import io.fabric8.launcher.creator.core.resource.Resources

class CapabilityComponent(info: GeneratorInfo, ctx: GeneratorContext) : BaseGenerator(info, ctx) {
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
