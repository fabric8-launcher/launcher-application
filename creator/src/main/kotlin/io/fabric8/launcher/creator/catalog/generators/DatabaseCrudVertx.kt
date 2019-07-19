package io.fabric8.launcher.creator.catalog.generators

import io.fabric8.launcher.creator.catalog.generators.GeneratorInfo.*
import io.fabric8.launcher.creator.core.*
import io.fabric8.launcher.creator.core.catalog.BaseGenerator
import io.fabric8.launcher.creator.core.catalog.CatalogItemContext
import io.fabric8.launcher.creator.core.resource.*
import io.fabric8.launcher.creator.core.template.transformers.blocks
import io.fabric8.launcher.creator.core.template.transformers.cases
import io.fabric8.launcher.creator.core.template.transformers.insertAtEndOfList
import java.nio.file.Paths

interface DatabaseCrudVertxProps : RuntimeVertxProps, DatabaseSecretRef {
    val databaseType: String

    companion object {
        @JvmOverloads fun build(_map: Properties = propsOf(), block: Data.() -> Unit = {}) =
            BaseProperties.build(::Data, _map, block)
    }

    open class Data(map: Properties = propsOf()) : RuntimeVertxProps.Data(map), DatabaseCrudVertxProps {
        override var databaseType: String by _map
        override var secretName: String by _map
    }
}

class DatabaseCrudVertx(ctx: CatalogItemContext) : BaseGenerator(ctx) {
    override fun apply(resources: Resources, props: Properties, extra: Properties): Resources {
        val dcvprops = DatabaseCrudVertxProps.build(props)
        // Check if the generator was already applied, so we don"t do it twice
        if (!filesCopied()) {
            val pprops = propsOf(
                props,
                "env" to envOf(
                    "DB_HOST" to envOf(
                        "secret" to dcvprops.secretName,
                        "key" to "uri"
                    ),
                    "DB_USERNAME" to envOf(
                        "secret" to dcvprops.secretName,
                        "key" to "user"
                    ),
                    "DB_PASSWORD" to envOf(
                        "secret" to dcvprops.secretName,
                        "key" to "password"
                    )
                )
            )
            generator(`runtime-vertx`).apply(resources, pprops, extra)
            copy()
            mergePoms(
                Paths.get("merge/pom.${dcvprops.databaseType}.xml"))
            mergeVersionPoms(dcvprops.runtime)
            transform("src/**/*.java", cases(props))
            transform(
                "src/main/java/io/openshift/booster/MainApplication.java",
                blocks(
                    "return new RouterConsumer[]{", "}",
                    insertAtEndOfList(listOf("      new io.openshift.booster.database.CrudApplication(vertx),"))
                )
            )
        }
        extra["sourceMapping"] = propsOf(
            "dbEndpoint" to join(
                dcvprops.subFolderName,
                "src/main/java/io/openshift/booster/database/CrudApplication.java"
            )
        )
        return resources
    }
}
