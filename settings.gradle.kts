pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    // The following is necessary, because the implicit toolchain resolving mechanism has been deprecated in gradle 8.
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.7.0" // can't access libs.versions.toml from here
}

rootProject.name = "titanium"
include("engine", "game", "import_asset", "kutils", "main")

/**
 * Use -PlocalLibs=kokuban,kstruct to use locally modified copies of those libraries instead of the published version.
 */
providers.gradleProperty("localLibs").orElse("").get().takeIf { it.isNotEmpty() }?.split(",")?.forEach { lib ->
    val libPath = "../$lib"
    val libCoords = "io.github.digorydoo:$lib"
    val libProject = ":$lib-core"

    println(
        """
        Warning: Using local copy of library: $lib
           Path: $libPath
           Coordinate: $libCoords
           Project: $libProject
        """.trimIndent()
    )

    includeBuild(libPath) {
        dependencySubstitution {
            substitute(module(libCoords)).using(project(libProject))
        }
    }
}
