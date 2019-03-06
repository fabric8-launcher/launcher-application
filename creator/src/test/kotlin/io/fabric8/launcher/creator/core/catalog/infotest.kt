package io.fabric8.launcher.creator.core.catalog

import org.assertj.core.api.Assertions.*
import org.junit.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InfoTest {
    @Test
    fun `have capabilities`() {
        assertThat(CapabilityInfo.values()).isNotEmpty
    }

    @Test
    fun `capabilities have info`() {
        assertThat(CapabilityInfo
                .values()
                .map { it.info }
                .filter { it.type != null }).isNotEmpty
    }

    @Test
    fun `have generators`() {
        assertThat(GeneratorInfo.values()).isNotEmpty
    }

    @Test
    fun `generators have info`() {
        assertThat(GeneratorInfo
                .values()
                .map { it.info }
                .filter { it.type != null}).isNotEmpty
    }
}
