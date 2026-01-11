plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-library`
}

kotlin {
    jvmToolchain(libs.versions.targetJDK.get().toInt())
}

sourceSets {
    main {
        java {
            setSrcDirs(listOf("src/java"))
        }
    }
}

dependencies {
    // NOTE: versions are now maintained in gradle/libs.versions.toml
    implementation(platform(libs.kotlin.bom))
    testImplementation(libs.kotlin.test)
}
