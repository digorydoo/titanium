plugins {
    alias(libs.plugins.kotlin.jvm)
    application
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

application {
    mainClass.set("ch.digorydoo.titanium.import_asset.MainKt")
}

dependencies {
    // NOTE: versions are now maintained in gradle/libs.versions.toml
    implementation(platform(libs.kotlin.bom))
    testImplementation(libs.kotlin.test)

    implementation(libs.xmlparserv2)

    implementation(project(":engine"))
    implementation(project(":kutils"))
}

tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    manifest {
        attributes["Main-Class"] = application.mainClass
    }

    from(sourceSets.main.get().output)

    dependsOn(configurations.runtimeClasspath)

    from({
        configurations.runtimeClasspath.get()
            .filter { it.name.endsWith("jar") }
            .map { zipTree(it) }
    })
}
