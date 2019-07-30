package io.fabric8.launcher.creator.catalog.capabilities

import io.fabric8.launcher.creator.core.catalog.CapabilityConstructor
import io.fabric8.launcher.creator.core.catalog.readCapabilityInfoDef

enum class CapabilityInfo(val klazz: CapabilityConstructor) {
    database(::Database),
    health(::Health),
    import(::Import),
    rest(::Rest),
    `web-app`(::WebApp),
    welcome(::Welcome);

    val infoDef by lazy { readCapabilityInfoDef(this.name) }

    companion object {
        val infos by lazy { values().map { it.infoDef } }
    }
}
