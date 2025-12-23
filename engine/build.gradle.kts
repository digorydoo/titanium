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
            val generatedSrc = layout.buildDirectory.dir("generated/java") // see GenerateSourcesTask
            setSrcDirs(listOf("src/java", generatedSrc))
            resources.srcDirs("src/res")
        }
    }
}

dependencies {
    // NOTE: versions are now maintained in gradle/libs.versions.toml
    implementation(platform(libs.kotlin.bom))
    testImplementation(libs.kotlin.test)

    implementation(libs.kotlinx.coroutines)

    implementation(project(":kutils"))
}
