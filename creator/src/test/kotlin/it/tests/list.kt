package it.tests

import io.fabric8.launcher.creator.catalog.GeneratorInfo
import it.Context
import it.IntegrationTests

typealias CapabilityTestConstructor = (context: Context) -> IntegrationTests

enum class CapabilityTest(val capInfo: GeneratorInfo, val testsProvider: CapabilityTestConstructor) {
    `capability-database`(GeneratorInfo.`capability-database`, ::DatabaseTests),
    `capability-health`(GeneratorInfo.`capability-health`, ::HealthTests),
    `capability-rest`(GeneratorInfo.`capability-rest`, ::RestTests),
    `capability-web-app`(GeneratorInfo.`capability-web-app`, ::WebAppTests);

    fun testOf(capInfo: GeneratorInfo): CapabilityTest {
        return values().find { it.capInfo == capInfo } ?: throw IllegalArgumentException("No test was found for $capInfo")
    }

    init {
        assert(name == capInfo.name) { "The Test must have the same name as the Capability. '$name' is not '${capInfo.name}'" }
    }
}
