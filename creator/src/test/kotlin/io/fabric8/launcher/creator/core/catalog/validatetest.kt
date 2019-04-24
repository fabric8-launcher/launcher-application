package io.fabric8.launcher.creator.core.catalog

import io.fabric8.launcher.creator.core.Enumeration
import io.fabric8.launcher.creator.core.Enums
import io.fabric8.launcher.creator.core.pathGet
import io.fabric8.launcher.creator.core.propsOf
import org.assertj.core.api.Assertions.*
import org.junit.Test

val def: List<PropertyDef>
    get() = listOf(
        EnumPropertyDef.build {
            id = "databaseType"
            name = "Database Type"
            description = "The type of database to use"
            required = true
            type = "enum"
            enumValues = listOf(
                    "postgresql",
                    "mysql"
            )
            default = "postgresql"
        },
        EnumPropertyDef.build {
            id = "runtime"
            name = "Runtime Type"
            description = "The type of runtime to use"
            required = true
            type = "enum"
            enumValues = listOf(
                    "nodejs",
                    "springboot",
                    "thorntail",
                    "vertx"
            )
        },
        EnumPropertyDef.build {
            id = "version"
            name = "Runtime Version"
            description = "The version of runtime to use"
            type = "enum"
            enumRef = "version.\${runtime}"
        },
        EnumPropertyDef.build {
            id = "type"
            name = "Some Type"
            description = "The type to use"
            required = true
            type = "enum"
            enumRef = "type"
            default = "type1"
        },
        ObjectPropertyDef.build {
            id = "maven"
            name = "Maven Project Setting"
            description = "The ids and version to use for the Maven project"
            required = true
            shared = true
            enabledWhen_ {
                propId = "runtime"
                equals = listOf(
                        "vertx"
                )
            }
            type = "object"
            props = mutableListOf(
                    PropertyDef.build {
                        id = "groupId"
                        name = "Group Id"
                        description = "The Maven Group Id for the project"
                        required = true
                        type = "string"
                        default = "org.openshift.appgen"
                    }
            )
        }
)

val enums: Enums = mapOf(
    "type" to listOf(Enumeration.build {
        id = "type1"
        name = "Type 1"
    }),
    "version.vertx" to listOf(Enumeration.build {
        id = "v1"
        name = "Vert.x Version 1"
    }),
    "version.nodejs" to listOf(Enumeration.build {
        id = "ver1"
        name = "Node Version 1"
    })
)

class ValidateTest {
    @Test
    fun `info validate all ok`() {
        val props = propsOf(
                "databaseType" to "mysql",
                "runtime" to "vertx",
                "version" to "v1",
                "type" to "type1",
                "maven" to propsOf(
                        "groupId" to "xxx"
                )
        )

        assertThatCode { validate(def, enums, props) }.doesNotThrowAnyException()
    }

    @Test
    fun `info validate using default maven`() {
        val props = propsOf(
            "runtime" to "vertx"
        )

        assertThatCode { validate(def, enums, props) }.doesNotThrowAnyException()
        assertThat(props["databaseType"]).isEqualTo("postgresql")
        assertThat(props.pathGet<String?>("maven.groupId")).isEqualTo("org.openshift.appgen")
    }

    @Test
    fun `info validate using default nodejs`() {
        val props = propsOf(
                "runtime" to "nodejs"
        )

        assertThatCode { validate(def, enums, props) }.doesNotThrowAnyException()
        assertThat(props["databaseType"]).isEqualTo("postgresql")
        assertThat(props.pathGet<Any?>("maven")).isNull()
    }

    @Test
    fun `info validate invalid enum value`() {
        val props = propsOf(
                "databaseType" to "WRONG",
                "runtime" to "vertx",
                "maven" to propsOf(
                        "groupId" to "xxx"
                )
        )

        assertThatCode { validate(def, enums, props) }.hasMessageContaining("Invalid enumeration value")
    }

    @Test
    fun `info validate missing required`() {
        val props = propsOf(
                "databaseType" to "mysql",
                "maven" to propsOf(
                        "groupId" to "xxx"
                )
        )

        assertThatCode { validate(def, enums, props) }.hasMessageContaining("Missing property")
    }

    @Test
    fun `info validate definition wrong type`() {
        val wrongTypeDef = listOf(
                PropertyDef.build {
                    id = "version"
                    name = "Version"
                    description = "The Maven Version for the project"
                    required = true
                    type = "WRONG"
                    default = "1.0"
                }
        )

        val props = propsOf(
                "version" to "2.0"
        )

        assertThatCode { validate(wrongTypeDef, enums, props) }.hasMessageContaining("Unknown type")
    }

    @Test
    fun `info validate definition missing enum values`() {
        val wrongEnumDef1 = listOf(
                EnumPropertyDef.build {
                    id = "databaseType"
                    name = "Database Type"
                    description = "The type of database to use"
                    required = true
                    type = "enum"
                    enumValues = listOf()
                    default = "postgresql"
                }
        )

        val wrongEnumDef2 = listOf(
                EnumPropertyDef.build(propsOf(
                        "id" to "databaseType",
                        "name" to "Database Type",
                        "description" to "The type of database to use",
                        "required" to true,
                        "type" to "enum",
                        "values" to "WRONG TYPE",
                        "default" to "postgresql"
                ))
        )

        val wrongEnumDef3 = listOf(
                EnumPropertyDef.build {
                    id = "databaseType"
                    name = "Database Type"
                    description = "The type of database to use"
                    required = true
                    type = "enum"
                    default = "postgresql"
                }
        )

        val props = propsOf(
                "databaseType" to "dummy"
        )

        assertThatCode { validate(wrongEnumDef1, enums, props) }.hasMessageContaining("Missing 'values' for")
        assertThatCode { validate(wrongEnumDef2, enums, props) }.hasMessageContaining("cannot be cast to")
        assertThatCode { validate(wrongEnumDef3, enums, props) }.hasMessageContaining("Missing 'values' or 'enumRef' for")
    }
}

/*
*/
