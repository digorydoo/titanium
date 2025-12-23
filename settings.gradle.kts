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
