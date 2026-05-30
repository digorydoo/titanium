package io.github.digorydoo.titanium.collect_intl

import io.github.digorydoo.kokuban.OptionsBuilder
import io.github.digorydoo.kokuban.OptionsParser
import io.github.digorydoo.kokuban.ShellCommandError
import java.io.File
import kotlin.system.exitProcess

class Options private constructor() {
    private var _srcOutputDir: File? = null
    val srcOutputDir: File get() = _srcOutputDir!!

    private var _resOutputDir: File? = null
    val resOutputDir: File get() = _resOutputDir!!

    private val _inputFiles = mutableListOf<File>()
    val inputFiles: List<File> get() = _inputFiles

    var clean = false; private set
    private var showHelp = false

    private val defs = OptionsBuilder.build {
        addBoolean("clean", 'c') { clean = it }
        addValueless("help", 'h') { showHelp = true }
        addString("src-output-dir", 's') { _srcOutputDir = File(it) }
        addString("res-output-dir", 'r') { _resOutputDir = File(it) }
    }

    private fun parse(args: Array<String>) {
        val result = OptionsParser(defs).parse(args, allowExtraArgs = true)

        if (showHelp) {
            printUsage()
            exitProcess(1)
        }

        _inputFiles.addAll(result.extraArgs.map { File(it) })

        if (_srcOutputDir == null) throw ShellCommandError("Missing option to specify source output directory.")
        if (_resOutputDir == null) throw ShellCommandError("Missing option to specify resource output directory.")
    }

    private fun printUsage() {
        println(
            """
            This tool is run from titanium-game's build.gradle.kts script as a pre-build step that
            collects and verifies *.intl files inside the source tree and generates the combined
            translation resources as well as the enum class GameTextId that contains all the keys.

            USAGE: ./collect-intl.sh <options> (<input-file>)*
            where <options> is one or more of:\n
            """.trimIndent()
        )

        defs.apply {
            get("clean").apply {
                helpBody = "Clean the directories provided by src-output-dir and res-output-dir first."
            }
            get("help").apply {
                helpBody = "Print this usage guide."
            }
            get("src-output-dir").apply {
                valueTypeHint = "<path>"
                helpBody = "Specify the directory where to create the Kotlin file containing the enum class."
            }
            get("res-output-dir").apply {
                valueTypeHint = "<path>"
                helpBody = "Specify the directory where to create the resource files."
            }
        }

        println(defs.makeHelpText())
    }

    companion object {
        fun fromCmdLine(args: Array<String>) =
            Options().apply { parse(args) }
    }
}
