package io.fabric8.launcher.creator.core.catalog

import io.fabric8.launcher.creator.core.Properties
import io.fabric8.launcher.creator.core.deploy.DeploymentDescriptor
import io.fabric8.launcher.creator.core.resource.Resources

interface Capability : CatalogItem {
    fun postApply(resources: Resources, props: Properties, deployment: DeploymentDescriptor): Resources {
        return resources
    }
}

typealias CapabilityConstructor = (CatalogItemContext) -> Capability

abstract class BaseCapability(ctx: CatalogItemContext) : BaseCatalogItem(ctx), Capability {
}
