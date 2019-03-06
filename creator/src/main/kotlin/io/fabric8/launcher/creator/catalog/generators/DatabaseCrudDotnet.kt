package io.fabric8.launcher.creator.catalog.generators

import io.fabric8.launcher.creator.core.*
import io.fabric8.launcher.creator.core.catalog.BaseGenerator
import io.fabric8.launcher.creator.core.catalog.CatalogItemContext
import io.fabric8.launcher.creator.core.resource.*
import io.fabric8.launcher.creator.core.template.transformers.cases
import io.fabric8.launcher.creator.core.template.transformers.insertAfter
import io.fabric8.launcher.creator.core.template.transformers.insertBefore
import io.fabric8.launcher.creator.core.template.transformers.replace

interface DatabaseCrudDotnetProps : PlatformDotnetProps, DatabaseSecretRef {
    val databaseType: String

    companion object {
        fun build(_map: Properties = propsOf(), block: Data.() -> Unit = {}) =
            BaseProperties.build(::Data, _map, block)
    }

    open class Data(map: Properties = propsOf()) : PlatformDotnetProps.Data(map), DatabaseCrudDotnetProps {
        override var databaseType: String by _map
        override var secretName: String by _map
    }
}

class DatabaseCrudDotnet(ctx: CatalogItemContext) : BaseGenerator(ctx) {
    override fun apply(resources: Resources, props: Properties, extra: Properties): Resources {
        val dcdprops = DatabaseCrudDotnetProps.build(props)
        // Check if the generator was already applied, so we don"t do it twice
        if (!filesCopied()) {
            val pprops = propsOf(
                props,
                "env" to envOf(
                    "DB_HOST" to envOf(
                        "secret" to dcdprops.secretName,
                        "key" to "uri"
                    ),
                    "DB_USERNAME" to envOf(
                        "secret" to dcdprops.secretName,
                        "key" to "user"
                    ),
                    "DB_PASSWORD" to envOf(
                        "secret" to dcdprops.secretName,
                        "key" to "password"
                    )
                )
            )
            generator(::PlatformDotnet).apply(resources, pprops, extra)
            copy()

            // Update csproj file
            val csprojFile = dcdprops.application + ".csproj"
            val mergeFile = sourceDir.resolve("merge/csproj-${dcdprops.databaseType}")
            val lines = streamFromPath(mergeFile).reader().readLines()
            transform(csprojFile, insertAfter("<!-- Add additional package references here -->", lines))

            // Update Startup.cs
            val efCoreFile = sourceDir.resolve("merge/efcore")
            val efCoreLines = streamFromPath(efCoreFile).reader().readLines()
            transform("Startup.cs", insertBefore("using " + dcdprops.dotnet.namespace + ".Models;", efCoreLines))

            val dbContextFile = sourceDir.resolve("merge/dbcontext")
            val dbContextLines = streamFromPath(dbContextFile).reader().readLines()
            transform("Startup.cs", insertAfter("// Add any DbContext here", dbContextLines))

            val dbInitFile = sourceDir.resolve("merge/dbinitialize")
            val dbInitLines = streamFromPath(dbInitFile).reader().readLines()
            transform("Startup.cs", insertAfter("// Optionally, initialize Db with data here", dbInitLines))

            val mergeHealthcheckFile = sourceDir.resolve("merge/healthcheck-${dcdprops.databaseType}")
            val healthcheckLines = streamFromPath(mergeHealthcheckFile).reader().readLines()
            transform("Startup.cs", replace("services.AddHealthChecks()", healthcheckLines))

            transform("**/*.cs", cases(props))
        }
        extra["sourceMapping"] = propsOf(
            "dbEndpoint" to join(
                dcdprops.subFolderName,
                "Controllers/FruitsController.cs"
            )
        )
        return resources
    }
}
