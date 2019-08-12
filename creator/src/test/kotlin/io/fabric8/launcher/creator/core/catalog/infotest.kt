package io.fabric8.launcher.creator.core.catalog

import io.fabric8.launcher.creator.catalog.GeneratorInfo
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test

class InfoTest {
    @Test
    fun `have capabilities`() {
        assertThat(GeneratorInfo.capabilities()).isNotEmpty
    }

    @Test
    fun `capabilities have info`() {
        assertThat(
            GeneratorInfo
                .capabilities()
                .map { it.infoDef }
                .filter { it.type != null }).isNotEmpty
    }

    @Test
    fun `have generators`() {
        assertThat(GeneratorInfo.values()).isNotEmpty
    }

    @Test
    fun `generators have info`() {
        assertThat(
            GeneratorInfo
                .values()
                .map { it.infoDef }
                .filter { it.type != null}).isNotEmpty
    }
}
