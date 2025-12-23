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

/**
 * Used by our generateSources task to create the BuildConfig file. I had to move this into a class in order to make it
 * safe with configuration cache.
 */
abstract class GenerateSourcesTask: DefaultTask() {
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
            package ch.digorydoo.titanium

            object BuildConfig {
                val isWindows = ${OperatingSystem.current().isWindows}
                val isProduction = $isProduction
            }
            """.trimIndent()
        )
    }
}

tasks.register("run") {
    if (project == rootProject) {
        error(
            "Running `run` from the root project is not allowed. " +
                "Use `./gradlew <subproject>:run` instead."
        )
    }
}

val flavour = providers.gradleProperty("flavour").orElse("")

subprojects {
    val generateSources = tasks.register<GenerateSourcesTask>("generateSources") {
        outputDir.set(layout.buildDirectory.dir("generated/java"))
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

    tasks.matching { it.name == "compileKotlin" }.configureEach {
        dependsOn(generateSources)
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
