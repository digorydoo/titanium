import org.gradle.internal.os.OperatingSystem

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
            val generatedSrc = layout.buildDirectory.dir("generated/java") // see GenerateSourcesTask
            setSrcDirs(listOf("src/java", generatedSrc))
        }
    }
}

application {
    mainClass.set("ch.digorydoo.titanium.main.app.MainKt")

    if (OperatingSystem.current().isMacOsX) {
        applicationDefaultJvmArgs = listOf("-XstartOnFirstThread")
    }
}

dependencies {
    implementation(platform(libs.kotlin.bom))
    // testImplementation(libs.kotlin.test)

    implementation(project(":engine"))
    implementation(project(":game"))
    implementation(project(":kutils"))

    implementation(libs.kotlinx.coroutines)

    // To learn what libaries are available for LWJGL, see:
    // https://www.lwjgl.org/customize

    implementation(platform(libs.lwjgl.bom))
    implementation(libs.lwjgl)
    implementation(libs.lwjgl.glfw)
    implementation(libs.lwjgl.openal)
    implementation(libs.lwjgl.opengl)
    implementation(libs.lwjgl.stb)
    // implementation(libs.lwjgl.assimp)
    // implementation(libs.lwjgl.bgfx)
    // implementation(libs.lwjgl.nanovg)
    // implementation(libs.lwjgl.nuklear)
    // implementation(libs.lwjgl.par)
    // implementation(libs.lwjgl.vulkan)

    val lwjglNatives = when {
        OperatingSystem.current().isWindows -> "natives-windows"
        OperatingSystem.current().isMacOsX -> when (System.getProperty("os.arch")) {
            "aarch64" -> "natives-macos-arm64"
            else -> "natives-macos"
        }
        else -> throw Exception("build.gradle.kts: Don't know what lwjglNatives to use")
    }

    runtimeOnly("org.lwjgl:lwjgl::$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-glfw::$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-openal::$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-opengl::$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-stb::$lwjglNatives")
    // runtimeOnly("org.lwjgl:lwjgl-assimp::$lwjglNatives")
    // runtimeOnly("org.lwjgl:lwjgl-bgfx::$lwjglNatives")
    // runtimeOnly("org.lwjgl:lwjgl-nanovg::$lwjglNatives")
    // runtimeOnly("org.lwjgl:lwjgl-nuklear::$lwjglNatives")
    // runtimeOnly("org.lwjgl:lwjgl-par::$lwjglNatives")
    // runtimeOnly("org.lwjgl:lwjgl-vulkan::$lwjglNatives")
}

tasks.register<Exec>("customPostBuild") {
    group = "Build"
    description = "Run post-build.sh script"
    workingDir = layout.projectDirectory.dir("..").asFile

    commandLine = when (OperatingSystem.current().isWindows) {
        true -> listOf("C:\\cygwin64\\bin\\sh", "-c", "./post-build.sh")
        false -> listOf("sh", "-c", "./post-build.sh")
    }
}

tasks.named("compileKotlin") {
    finalizedBy("customPostBuild")
}

tasks.named("run") {
    dependsOn(":engine:test")
    dependsOn(":game:test")
    dependsOn(":kutils:test")
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
