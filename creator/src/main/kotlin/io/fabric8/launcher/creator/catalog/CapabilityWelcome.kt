package io.fabric8.launcher.creator.catalog

import io.fabric8.launcher.creator.catalog.GeneratorInfo.*
import io.fabric8.launcher.creator.core.*
import io.fabric8.launcher.creator.core.catalog.BaseGenerator
import io.fabric8.launcher.creator.core.catalog.GeneratorContext
import io.fabric8.launcher.creator.core.deploy.DeploymentDescriptor
import io.fabric8.launcher.creator.core.resource.Resources

class CapabilityWelcome(info: GeneratorInfo, ctx: GeneratorContext) : BaseGenerator(info, ctx) {
    override fun apply(resources: Resources, props: Properties, extra: Properties): Resources {
        return resources
    }

    override fun postApply(resources: Resources, props: Properties, deployment: DeploymentDescriptor): Resources {
        val appName = name(props["application"] as String, props["subFolderName"] as String?)
        val rtServiceName = appName
        val waprops = propsOf(
            "application" to props["application"],
            "subFolderName" to props["subFolderName"],
            "serviceName" to rtServiceName,
            "routeName" to "welcome",
            "deployment" to deployment
        )
        return generator(`welcome-app`).apply(resources, waprops, propsOf())
    }

}
