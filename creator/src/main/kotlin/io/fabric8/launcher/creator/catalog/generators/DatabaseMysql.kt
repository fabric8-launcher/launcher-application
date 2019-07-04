package io.fabric8.launcher.creator.catalog.generators

import io.fabric8.launcher.creator.core.*
import io.fabric8.launcher.creator.core.catalog.BaseGenerator
import io.fabric8.launcher.creator.core.catalog.BaseGeneratorProps
import io.fabric8.launcher.creator.core.catalog.CatalogItemContext
import io.fabric8.launcher.creator.core.resource.*

private val livenessProbe = propsOf(
    "initialDelaySeconds" to 30,
    "tcpSocket" to propsOf(
        "port" to 3306
    )
)

private val readinessProbe = propsOf(
    "initialDelaySeconds" to 5,
    "exec" to propsOf(
        "command" to listOf(
            "/bin/sh",
            "-i",
            "-c",
            "MYSQL_PWD=\"\$MYSQL_PASSWORD\" mysql -h 127.0.0.1 -u \$MYSQL_USER -D \$MYSQL_DATABASE -e \"SELECT 1\""
        )
    )
)

interface DatabaseMysqlProps : BaseGeneratorProps, DatabaseSecretRef {
    companion object {
        @JvmOverloads fun build(_map: Properties = propsOf(), block: Data.() -> Unit = {}) =
            BaseProperties.build(::Data, _map, block)
    }

    open class Data(map: Properties = propsOf()) : BaseGeneratorProps.Data(map), DatabaseMysqlProps {
        override var secretName: String by _map
    }
}

class DatabaseMysql(ctx: CatalogItemContext) : BaseGenerator(ctx) {
    override fun apply(resources: Resources, props: Properties, extra: Properties): Resources {
        val dmprops = DatabaseMysqlProps.build(props)
        // Check that the database doesn"t already exist
        if (resources.service(dmprops.serviceName) == null) {
            // Create the database resource definitions
            val res = newApp(dmprops.serviceName, dmprops.application, IMAGE_MYSQL, null, envOf(
                "MYSQL_ROOT_PASSWORD" to "verysecretrootpassword",
                "MYSQL_DATABASE" to propsOf("secret" to dmprops.secretName, "key" to "database"),
                "MYSQL_USER" to propsOf("secret" to dmprops.secretName, "key" to "user"),
                "MYSQL_PASSWORD" to propsOf("secret" to dmprops.secretName, "key" to "password")
            ))
            setMemoryLimit(res, "512Mi")
            setCpuLimit(res, "1")
            setHealthProbe(res, "livenessProbe", livenessProbe)
            setHealthProbe(res, "readinessProbe", readinessProbe)
            resources.add(res)
        }

        val exProps = propsOf(
            "image" to IMAGE_MYSQL,
            "service" to dmprops.secretName
        )
        extra["databaseInfo"] = exProps

        return resources
    }
}
