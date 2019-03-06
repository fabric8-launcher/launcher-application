package io.fabric8.launcher.creator.catalog.generators

import io.fabric8.launcher.creator.core.*
import io.fabric8.launcher.creator.core.catalog.BaseGenerator
import io.fabric8.launcher.creator.core.catalog.BaseGeneratorProps
import io.fabric8.launcher.creator.core.catalog.CatalogItemContext
import io.fabric8.launcher.creator.core.resource.*
import io.fabric8.launcher.creator.core.template.transformers.cases

interface LanguageCsharpProps : BaseGeneratorProps {
    val builderImage: String?
    val env: Environment?

    companion object {
        fun build(_map: Properties = propsOf(), block: Data.() -> kotlin.Unit = {}) =
            BaseProperties.build(::Data, _map, block)
    }

    open class Data(map: Properties = propsOf()) : BaseGeneratorProps.Data(map), LanguageCsharpProps {
        override var builderImage: String? by _map
        override var env: Environment? by _map
    }
}

class LanguageCsharp(ctx: CatalogItemContext) : BaseGenerator(ctx) {
    override fun apply(resources: Resources, props: Properties, extra: Properties): Resources {
        val lcprops = LanguageCsharpProps.build(props)
        // Check if the service already exists, so we don't create it twice
        if (resources.service(lcprops.serviceName) == null) {
            copy()
            transform(listOf("gap", "**/*.cs"), cases(lcprops));
            val res = newApp(
                    lcprops.serviceName,
                    lcprops.application,
                    lcprops.builderImage ?: BUILDER_DOTNET,
                    null,
                    lcprops.env);
            setBuildContextDir(res, lcprops.subFolderName);
            resources.add(res);
            return newRoute(resources, lcprops.routeName, lcprops.application, lcprops.serviceName);
        } else {
            setBuildEnv(resources, lcprops.env, lcprops.serviceName);
            setDeploymentEnv(resources, lcprops.env, lcprops.serviceName);
            return resources;
        }
    }
}
