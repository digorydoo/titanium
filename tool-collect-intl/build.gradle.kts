plugins {
    alias(libs.plugins.kotlin.jvm)
    application
}

kotlin {
    jvmToolchain(libs.versions.targetJDK.get().toInt())
}

application {
    mainClass.set("io.github.digorydoo.titanium.collect_intl.MainKt")
}

dependencies {
    // NOTE: versions are now maintained in gradle/libs.versions.toml
    implementation(platform(libs.kotlin.bom))
    testImplementation(libs.kotlin.test)

    implementation(libs.kokuban)

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
