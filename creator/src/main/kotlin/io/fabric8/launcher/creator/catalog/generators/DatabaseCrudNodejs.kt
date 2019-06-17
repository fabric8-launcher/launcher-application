package io.fabric8.launcher.creator.catalog.generators

import io.fabric8.launcher.creator.core.*
import io.fabric8.launcher.creator.core.catalog.BaseGenerator
import io.fabric8.launcher.creator.core.catalog.CatalogItemContext
import io.fabric8.launcher.creator.core.resource.*
import io.fabric8.launcher.creator.core.template.transformers.cases
import io.fabric8.launcher.creator.core.template.transformers.insertAfter

interface DatabaseCrudNodejsProps : RuntimeNodejsProps, DatabaseSecretRef {
    val databaseType: String

    companion object {
        @JvmOverloads fun build(_map: Properties = propsOf(), block: Data.() -> Unit = {}) =
            BaseProperties.build(::Data, _map, block)
    }

    open class Data(map: Properties = propsOf()) : RuntimeNodejsProps.Data(map), DatabaseCrudNodejsProps {
        override var databaseType: String by _map
        override var secretName: String by _map
    }
}

class DatabaseCrudNodejs(ctx: CatalogItemContext) : BaseGenerator(ctx) {
    override fun apply(resources: Resources, props: Properties, extra: Properties): Resources {
        val dcnprops = DatabaseCrudNodejsProps.build(props)
        // Check if the generator was already applied, so we don"t do it twice
        if (!filesCopied()) {
            val pprops = propsOf(
                props,
                "env" to envOf(
                    "DB_HOST" to envOf(
                        "secret" to dcnprops.secretName,
                        "key" to "uri"
                    ),
                    "DB_USERNAME" to envOf(
                        "secret" to dcnprops.secretName,
                        "key" to "user"
                    ),
                    "DB_PASSWORD" to envOf(
                        "secret" to dcnprops.secretName,
                        "key" to "password"
                    )
                )
            )
            generator(::RuntimeNodejs).apply(resources, pprops, extra)
            copy()
            mergePackageJson()
            transform("lib/**/*.js", cases(props))
            val mergeFile = sourceDir.resolve("merge/app.merge.js")
            val lines = streamFromPath(mergeFile).reader().readLines()
            transform("app.js", insertAfter("//TODO: Add routes", lines))
        }
        extra["sourceMapping"] = propsOf( "dbEndpoint" to join(dcnprops.subFolderName, "lib/routes/fruits.js"))
        return resources
    }
}
