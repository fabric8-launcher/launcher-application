package io.fabric8.launcher.creator.core.resource

import io.fabric8.launcher.creator.core.propsOf
import org.assertj.core.api.Assertions.*
import org.junit.Test
import org.junit.jupiter.api.TestInstance
import java.io.File
import java.nio.file.Paths

private fun getResources(): Resources {
    val resPath = Paths.get("io/fabric8/launcher/creator/core/resource/resources.yaml")
    return readResources(resPath)
}

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ResourceTest {
    @Test
    fun `read resources`() {
        val res = getResources()
        assertThat(res.json.kind).isEqualTo("Template")
        assertThat(res.items).hasSize(10)
        assertThat(res.parameters).hasSize(2)
    }

    @Test
    fun `toList`() {
        val res = getResources()
        res.toList()
        assertThat(res.json.kind).isEqualTo("List")
        assertThat(res.isEmpty).isFalse()
        assertThat(res.items).hasSize(10)
        assertThat(res.parameters).isEmpty()
    }

    @Test
    fun `have buildConfig`() {
        val res = getResources()
        val bcs = res.buildConfigs
        assertThat(bcs).hasSize(1)
        assertThat(bcs[0].metadata?.name).isEqualTo("fubar-backend")
    }

    @Test
    fun `have specific buildConfig`() {
        val res = getResources()
        val bc = res.buildConfig("fubar-backend")
        assertThat(bc).isNotNull()
    }

    @Test
    fun `have no builds`() {
        val res = getResources()
        val bs = res.builds
        assertThat(bs).isEmpty()
    }

    @Test
    fun `have deploymentConfigs`() {
        val res = getResources()
        val dcs = res.deploymentConfigs
        assertThat(dcs).hasSize(2)
        assertThat(dcs.map { it.metadata?.name }).contains("fubar-backend", "fubar-backend-database")
    }

    @Test
    fun `have specific deploymentConfig`() {
        val res = getResources()
        val dc = res.deploymentConfig("fubar-backend")
        assertThat(dc).isNotNull()
    }

    @Test
    fun `add stuff`() {
        val res = getResources()
        val secret = propsOf(
                "kind" to "ConfigMap",
                "apiVersion" to "v1",
                "metadata" to propsOf(
                        "name" to "some name",
                        "labels" to propsOf(
                                "app" to "app-label"
                        )
                ),
                "stringData" to propsOf(
                        "uri" to "uri:to/somewhere",
                        "database" to "my-db",
                        "user" to "dbuser",
                        "password" to "secret"
                )
        )
        assertThat(res.configMaps).isEmpty()
        res.add(secret)
        assertThat(res.items).hasSize(11)
        assertThat(res.configMaps).hasSize(1)
    }

    @Test
    fun `write resources`() {
        val res = getResources()
        val temp = File.createTempFile("test-resources", ".tmp")
        temp.deleteOnExit()
        writeResources(temp.toPath(), res)
        val res2 = readResources(temp.toPath())
        assertThat(res2.json.kind).isEqualTo("Template")
        assertThat(res2.items).hasSize(10)
        assertThat(res2.parameters).hasSize(2)
    }
}
