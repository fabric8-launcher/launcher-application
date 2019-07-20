package io.fabric8.launcher.creator.core.catalog

import io.fabric8.launcher.creator.catalog.capabilities.CapabilityInfo
import io.fabric8.launcher.creator.core.Properties
import io.fabric8.launcher.creator.core.deploy.DeploymentDescriptor
import io.fabric8.launcher.creator.core.resource.Resources
import java.nio.file.Path
import java.nio.file.Paths

interface Capability : CatalogItem {
    fun postApply(resources: Resources, props: Properties, deployment: DeploymentDescriptor): Resources {
        return resources
    }
}

typealias CapabilityConstructor = (CapabilityInfo, CatalogItemContext) -> Capability

abstract class BaseCapability(private val info: CapabilityInfo, ctx: CatalogItemContext) : BaseCatalogItem(ctx), Capability {
    override val sourceDir: Path
        get() = Paths.get("META-INF/catalog/capabilities/" + info.info.module)
}
