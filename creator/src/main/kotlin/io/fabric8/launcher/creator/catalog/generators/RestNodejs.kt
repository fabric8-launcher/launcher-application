package io.fabric8.launcher.creator.catalog.generators

import io.fabric8.launcher.creator.catalog.generators.GeneratorInfo.*
import io.fabric8.launcher.creator.core.*
import io.fabric8.launcher.creator.core.catalog.BaseGenerator
import io.fabric8.launcher.creator.core.catalog.CatalogItemContext
import io.fabric8.launcher.creator.core.resource.*
import io.fabric8.launcher.creator.core.template.transformers.insertAfter

class RestNodejs(ctx: CatalogItemContext) : BaseGenerator(ctx) {
    override fun apply(resources: Resources, props: Properties, extra: Properties): Resources {
        val pprops = RuntimeNodejsProps.build(props)
        // Check if the generator was already applied, so we don't do it twice
        if (!filesCopied()) {
            // First copy the files from the base nodejs runtime module
            // and then copy our own over that
            generator(`runtime-nodejs`).apply(resources, pprops, extra);
            val mergeFile = sourceDir.resolve("merge/app.merge.js")
            val lines = streamFromPath(mergeFile).reader().readLines()
            transform("app.js", insertAfter("//TODO: Add routes", lines))
            copy()
        }
        extra["sourceMapping"] = propsOf("greetingEndpoint" to join(pprops.subFolderName, "/greeting.js"))
        return resources
    }
}
