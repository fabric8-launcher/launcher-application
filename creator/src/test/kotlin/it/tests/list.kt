package it.tests

import io.fabric8.launcher.creator.catalog.capabilities.CapabilityInfo
import it.Context
import it.IntegrationTests

typealias CapabilityTestConstructor = (context: Context) -> IntegrationTests

enum class CapabilityTest(val capInfo: CapabilityInfo, val testsProvider: CapabilityTestConstructor) {
    database(CapabilityInfo.database, ::DatabaseTests),
    health(CapabilityInfo.health, ::HealthTests),
    rest(CapabilityInfo.rest, ::RestTests),
    `web-app`(CapabilityInfo.`web-app`, ::WebAppTests);

    fun testOf(capInfo: CapabilityInfo): CapabilityTest {
        return valueOf(capInfo.name)
    }

    init {
        assert(name == capInfo.name) { "The Test must have the same name as the Capability. '$name' is not '${capInfo.name}'" }
    }
}
