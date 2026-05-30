plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-library`
}

kotlin {
    jvmToolchain(libs.versions.targetJDK.get().toInt())
}

dependencies {
    // NOTE: versions are now maintained in gradle/libs.versions.toml
    implementation(platform(libs.kotlin.bom))
    testImplementation(libs.kotlin.test)

    implementation(libs.kotlinx.coroutines)
    implementation(libs.kstruct)

    implementation(project(":titanium-engine"))
    implementation(project(":kutils"))
}

sourceSets {
    main {
        kotlin {
            // BuildConfig is generated for multiple subprojects and is inside the rootProject's build directory.
            srcDir(rootProject.layout.buildDirectory.dir("generated/build_config"))

            // GameTextId.kt is generated for the game subproject only and is inside the subproject's build directory.
            srcDir(layout.buildDirectory.dir("generated/collected_intl_src"))
        }
        resources {
            srcDir(layout.buildDirectory.dir("generated/collected_intl_res"))
        }
    }
}
