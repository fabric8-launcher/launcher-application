package io.fabric8.launcher.creator.catalog.generators

import io.fabric8.launcher.creator.core.*
import io.fabric8.launcher.creator.core.catalog.BaseGenerator
import io.fabric8.launcher.creator.core.catalog.CatalogItemContext
import io.fabric8.launcher.creator.core.resource.*

class RestThorntail(ctx: CatalogItemContext) : BaseGenerator(ctx) {
    override fun apply(resources: Resources, props: Properties, extra: Properties): Resources {
        val pprops = PlatformThorntailProps.build(props)
        // Check if the generator was already applied, so we don't do it twice
        if (!filesCopied()) {
            // First copy the files from the base springboot platform module
            // and then copy our own over that
            generator(::PlatformThorntail).apply(resources, pprops, extra)
            copy()
            mergePoms()
        }
        extra["sourceMapping"] = propsOf("greetingEndpoint" to join(pprops.subFolderName, "src/main/java/io/openshift/booster/http/GreetingEndpoint.java"))
        return resources
    }
}
