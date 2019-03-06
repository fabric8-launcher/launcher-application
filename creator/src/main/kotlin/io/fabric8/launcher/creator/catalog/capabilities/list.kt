package io.fabric8.launcher.creator.catalog.capabilities

import io.fabric8.launcher.creator.core.catalog.CapabilityConstructor
import io.fabric8.launcher.creator.core.catalog.readCapabilityInfoDef
import org.immutables.value.internal.`$generator$`.`$Generator`.Import

enum class CapabilityInfo(val klazz: CapabilityConstructor) {
    database(::Database),
    health(::Health),
    import(::Import),
    rest(::Rest),
    `web-app`(::WebApp),
    welcome(::Welcome);

    val info by lazy { readCapabilityInfoDef(this.name) }
}
