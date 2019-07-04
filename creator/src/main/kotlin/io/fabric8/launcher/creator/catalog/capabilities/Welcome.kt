package io.fabric8.launcher.creator.catalog.capabilities

import io.fabric8.launcher.creator.catalog.generators.WelcomeApp
import io.fabric8.launcher.creator.core.*
import io.fabric8.launcher.creator.core.catalog.BaseCapability
import io.fabric8.launcher.creator.core.catalog.CatalogItemContext
import io.fabric8.launcher.creator.core.deploy.DeploymentDescriptor
import io.fabric8.launcher.creator.core.resource.Resources

class Welcome(ctx: CatalogItemContext) : BaseCapability(ctx) {
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
        return generator(::WelcomeApp).apply(resources, waprops, propsOf())
    }

}
