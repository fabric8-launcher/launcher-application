package io.fabric8.launcher.creator.catalog.generators

import io.fabric8.launcher.creator.core.catalog.GeneratorConstructor
import io.fabric8.launcher.creator.core.catalog.SimpleConfigGenerator
import io.fabric8.launcher.creator.core.catalog.readGeneratorInfoDef

enum class GeneratorInfo(val klazz: GeneratorConstructor = ::SimpleConfigGenerator) {
    `app-images`(::AppImages),
    `database-crud-dotnet`,
    `database-crud-nodejs`,
    `database-crud-quarkus`,
    `database-crud-springboot`,
    `database-crud-thorntail`,
    `database-crud-vertx`,
    `database-crud-wildfly`(::DatabaseCrudWildfly),
    `database-mysql`,
    `database-postgresql`,
    `database-secret`,
    `import-codebase`(::ImportCodebase),
    `language-csharp`,
    `language-java`,
    `language-nodejs`,
    `maven-setup`(::MavenSetup),
    `runtime-angular`,
    `runtime-base-support`(::RuntimeBaseSupport),
    `runtime-dotnet`,
    `runtime-nodejs`,
    `runtime-quarkus`,
    `runtime-react`,
    `runtime-springboot`,
    `runtime-thorntail`,
    `runtime-vertx`,
    `runtime-vuejs`,
    `runtime-wildfly`,
    `rest-dotnet`,
    `rest-nodejs`,
    `rest-quarkus`,
    `rest-springboot`,
    `rest-thorntail`,
    `rest-vertx`,
    `rest-wildfly`,
    `welcome-app`(::WelcomeApp);

    val infoDef by lazy { readGeneratorInfoDef(this.name) }

    companion object {
        val infoDefs by lazy { values().map { it.infoDef } }
    }
}
