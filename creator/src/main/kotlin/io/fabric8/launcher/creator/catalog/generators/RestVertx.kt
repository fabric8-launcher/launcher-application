package io.fabric8.launcher.creator.catalog.generators

import io.fabric8.launcher.creator.catalog.generators.GeneratorInfo.*
import io.fabric8.launcher.creator.core.*
import io.fabric8.launcher.creator.core.catalog.BaseGenerator
import io.fabric8.launcher.creator.core.catalog.CatalogItemContext
import io.fabric8.launcher.creator.core.resource.*
import io.fabric8.launcher.creator.core.template.transformers.blocks
import io.fabric8.launcher.creator.core.template.transformers.insertAtEndOfList

class RestVertx(ctx: CatalogItemContext) : BaseGenerator(ctx) {
    override fun apply(resources: Resources, props: Properties, extra: Properties): Resources {
        val pprops = RuntimeVertxProps.build(props)
        // Check if the generator was already applied, so we don't do it twice
        if (!filesCopied()) {
            // First copy the files from the base vertx runtime module
            // and then copy our own over that
            generator(`runtime-vertx`).apply(resources, pprops, extra)
            copy()
            mergePoms()
            transform(
                "src/main/java/io/openshift/booster/MainApplication.java",
                blocks(
                    "return new RouterConsumer[]{", "}",
                    insertAtEndOfList(listOf("      new io.openshift.booster.http.HttpApplication(vertx)"))
                )
            )
        }
        extra["sourceMapping"] = propsOf("greetingEndpoint" to join(pprops.subFolderName, "src/main/java/io/openshift/booster/http/HttpApplication.java"))
        return resources
    }
}
