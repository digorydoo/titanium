package io.github.digorydoo.titanium.import_asset.options

import io.github.digorydoo.kokuban.OptionsBuilder
import io.github.digorydoo.kokuban.OptionsParser
import kotlin.system.exitProcess

class ImportColladaOptions: Options() {
    var onlyNewer = false; private set
    var outDir = ""; private set
    var overwrite = false; private set
    var verbosity = Verbosity.NORMAL; private set
    var extraArgs = listOf<String>(); private set
    private var showHelp = false

    val defs = OptionsBuilder.build {
        addValueless("help", 'h') { showHelp = true }
        addBoolean("only-newer", 'n') { onlyNewer = it }
        addString("out-dir", 'd') { outDir = it }
        addBoolean("overwrite", 'w') { overwrite = it }
        addValueless("quiet", 'q') { verbosity = Verbosity.QUIET }
        addValueless("verbose", 'v') { verbosity = Verbosity.VERBOSE }
    }

    override fun readCmdLine(args: Array<String>) {
        val parser = OptionsParser(defs)
        val result = parser.parse(args, allowExtraArgs = true)
        extraArgs = result.extraArgs

        if (showHelp) {
            printUsage()
            exitProcess(1)
        }
    }

    private fun printUsage() {
        println(
            """
                USAGE: import-asset $COLLADA_ACTION <options> <files>
                <files> is one or more Collada file paths.
                <options> is one or more of:
            """.trimIndent() + "\n"
        )

        defs.apply {
            get("help").apply {
                helpBody = "Print this usage guide."
            }
            get("only-newer").apply {
                helpBody = "Skip Collada files which are older than its existing output file."
            }
            get("out-dir").apply {
                valueTypeHint = "<path>"
                helpBody = "Set the output directory."
            }
            get("overwrite").apply {
                helpBody = "Overwrite existing files."
            }
            get("quiet").apply {
                helpBody = "Don't write anything to stdout except errors."
            }
            get("verbose").apply {
                helpBody = "Enable verbose mode."
            }
        }

        println(defs.makeHelpText())

        // NOTE: When updating this example, you probably also need to update make-proper.sh
        println(
            arrayOf(
                "Example:",
                "$ ./import-asset.sh collada " +
                    "--out-dir=assets/generated/mesh/ " +
                    "--overwrite " +
                    "--only-newer " +
                    "assets/private/collada/*.dae",
            ).joinToString("\n") + "\n"
        )
    }
}
