import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.internal.os.OperatingSystem

plugins {
    // The following lets me check for outdated dependencies using:
    // $ ./gradlew dependencyUpdates
    // alias(libs.plugins.ben.manes.versions) // doesn't work, gives weird error, appears to be a known bug
    id("com.github.ben-manes.versions") version "0.53.0" // using a hard-coded version for now
}

buildscript {
    repositories {
        mavenCentral()
    }
}

allprojects {
    repositories {
        mavenCentral()
    }
}

tasks.register("run") {
    if (project == rootProject) {
        error(
            "Running `run` from the root project is not allowed to avoid ambiguities.\n" +
                "Use instead: ./gradlew main:run -Pflavour=[development|production]"
        )
    }
}

abstract class GenerateBuildConfigTask: DefaultTask() {
    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @get:Input
    var isProduction = false

    @TaskAction
    fun generate() {
        val dir = outputDir.get().asFile.resolve("ch/digorydoo/titanium")
        dir.mkdirs()
        dir.resolve("BuildConfig.kt").writeText(
            """
            package io.github.digorydoo.titanium

            object BuildConfig {
                val isWindows = ${OperatingSystem.current().isWindows}
                val isProduction = $isProduction
            }
            """.trimIndent()
        )
    }
}

val generateBuildConfigTask = tasks.register<GenerateBuildConfigTask>("generateSources") {
    outputDir.set(layout.buildDirectory.dir("generated/build_config"))

    val flavour: Provider<String> = providers.gradleProperty("flavour").orElse("")

    isProduction = flavour.map {
        when (it) {
            "production" -> true
            "development" -> false
            else -> error(
                (if (it.isEmpty()) "Flavour missing!" else "Illegal value for flavour: $it") +
                    "\nPlease use ./gradlew <task> -Pflavour=[development|production]"
            )
        }
    }.get()
}

project(":titanium-game") {
    class CollectIntlCmdArgsProvider(
        private val srcOutputDir: File,
        private val resOutputDir: File,
        private val files: Set<File>,
    ): CommandLineArgumentProvider {
        override fun asArguments(): Iterable<String> =
            listOf(
                "-c",
                "./collect-intl.sh " +
                    "-s=${escape(srcOutputDir.absolutePath)} " +
                    "-r=${escape(resOutputDir.absolutePath)} " +
                    "--clean " +
                    files.joinToString(" ") { escape(it.absolutePath) }
            )

        private fun escape(s: String) = s.replace(" ", "\\ ").replace("'", "\\'").replace("\"", "\\\"")
    }

    val collectIntlTask by tasks.registering(Exec::class) {
        dependsOn(":tool-collect-intl:build")

        group = "build"
        description = "Run collect-intl.sh script and generate new sources"

        workingDir = layout.projectDirectory.dir("..").asFile

        val theInputs = fileTree("src/main/kotlin") { include("**/*.intl") }
        val srcOutputDir = layout.buildDirectory.dir("generated/collected_intl_src")!!
        val resOutputDir = layout.buildDirectory.dir("generated/collected_intl_res")!!

        inputs.files(theInputs)
        outputs.dirs(srcOutputDir, resOutputDir)

        executable = when (OperatingSystem.current().isWindows) {
            true -> "C:\\cygwin64\\bin\\sh"
            false -> "sh"
        }

        argumentProviders.add(
            CollectIntlCmdArgsProvider(srcOutputDir.get().asFile, resOutputDir.get().asFile, theInputs.files)
        )
    }

    plugins.withId("java-library") {
        tasks.named("compileKotlin") {
            dependsOn(collectIntlTask)
        }
        tasks.named("processResources") {
            dependsOn(collectIntlTask)
        }
    }
}

subprojects {
    // Only the subproject whose names start with "titanium" rely on our BuildConfig. The tool subprojects don't, and
    // thus do not need a build flavour. However, tool-import-asset still relies on titanium-engine, so a flavour will
    // still be required when building that tool.
    if (name.startsWith("titanium")) {
        tasks.matching { it.name == "compileKotlin" }.configureEach {
            dependsOn(generateBuildConfigTask)
        }
    }

    // The following modifies the configuration of the "test" task in all subprojects
    tasks.withType<Test>().configureEach {
        useJUnitPlatform()

        testLogging {
            events("skipped", "failed") // emit details for skipped and failed tests only, not "passed"
            showStandardStreams = true // otherwise we won't see anything in console
            exceptionFormat = TestExceptionFormat.FULL

            addTestListener(object: TestListener {
                override fun beforeTest(testDescriptor: TestDescriptor) = Unit
                override fun afterTest(testDescriptor: TestDescriptor, result: TestResult) = Unit
                override fun beforeSuite(suite: TestDescriptor) = Unit

                override fun afterSuite(suite: TestDescriptor, result: TestResult) {
                    if (suite.parent == null) {
                        println(
                            "${suite.name} ${result.resultType}: " +
                                "${result.successfulTestCount} passed, " +
                                "${result.failedTestCount} failed, " +
                                "${result.skippedTestCount} skipped"
                        )
                    }
                }
            })
        }
    }
}
