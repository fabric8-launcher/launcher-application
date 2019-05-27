package io.fabric8.launcher.creator.catalog.generators

import io.fabric8.launcher.creator.core.*
import io.fabric8.launcher.creator.core.catalog.BaseGenerator
import io.fabric8.launcher.creator.core.catalog.CatalogItemContext
import io.fabric8.launcher.creator.core.resource.*
import io.fabric8.launcher.creator.core.template.transformers.cases
import java.nio.file.Paths

interface DatabaseCrudSpringbootProps : RuntimeSpringbootProps, DatabaseSecretRef {
    val databaseType: String

    companion object {
        fun build(_map: Properties = propsOf(), block: Data.() -> Unit = {}) =
            BaseProperties.build(::Data, _map, block)
    }

    open class Data(map: Properties = propsOf()) : RuntimeSpringbootProps.Data(map), DatabaseCrudSpringbootProps {
        override var databaseType: String by _map
        override var secretName: String by _map
    }
}

class DatabaseCrudSpringboot(ctx: CatalogItemContext) : BaseGenerator(ctx) {
    override fun apply(resources: Resources, props: Properties, extra: Properties): Resources {
        val dcsprops = DatabaseCrudSpringbootProps.build(props)
        // Check if the generator was already applied, so we don"t do it twice
        if (!filesCopied()) {
            val pprops = propsOf(
                props,
                "env" to envOf(
                    "DB_HOST" to envOf(
                        "secret" to dcsprops.secretName,
                        "key" to "uri"
                    ),
                    "DB_USERNAME" to envOf(
                        "secret" to dcsprops.secretName,
                        "key" to "user"
                    ),
                    "DB_PASSWORD" to envOf(
                        "secret" to dcsprops.secretName,
                        "key" to "password"
                    )
                )
            )
            generator(::RuntimeSpringboot).apply(resources, pprops, extra)
            copy()
            mergePoms(
                Paths.get("merge/pom.${dcsprops.databaseType}.xml"))
            transform("src/**/*.properties", cases(props))
        }
        extra["sourceMapping"] = propsOf(
            "dbEndpoint" to join(
                dcsprops.subFolderName,
                "src/main/java/io/openshift/booster/database/service/FruitRepository.java"
            )
        )
        return resources
    }
}
