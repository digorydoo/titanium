package io.github.digorydoo.titanium.import_asset.options

import io.github.digorydoo.kokuban.OptionsBuilder
import io.github.digorydoo.kokuban.OptionsParser
import kotlin.system.exitProcess

class ImportBrickTexturesOptions: Options() {
    var arrangeAcross = 1; private set
    var outFile = ""; private set
    var overwrite = false; private set
    var padding = 0; private set
    var verbosity = Verbosity.NORMAL; private set
    var extraArgs = listOf<String>(); private set
    private var showHelp = false

    val defs = OptionsBuilder.build {
        addValueless("help", 'h') { showHelp = true }
        addInt("arrange-across", 'a', 1, 100) { arrangeAcross = it }
        addString("out-file", 'f') { outFile = it }
        addInt("padding", 'p', 0, 32) { padding = it }
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
                USAGE: import-asset $BRICK_TEXTURES_ACTION <options> <files>
                <files> is one or more brick textures (PNG).
                <options> is one or more of:
            """.trimIndent() + "\n"
        )

        defs.apply {
            get("help").apply {
                helpBody = "Print this usage guide."
            }
            get("arrange-across").apply {
                helpBody = "When arranging textures, place n portions horizontally before starting a new row."
            }
            get("out-file").apply {
                valueTypeHint = "<path>"
                helpBody = "Set the filename of the generated texture."
            }
            get("padding").apply {
                helpBody = "When arranging textures, use a padding of the given number of pixels around each texture."
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
                "$ ./import-asset.sh brick-textures " +
                    "--out-file=assets/generated/textures/tiles-town.png " +
                    "--overwrite " +
                    "--padding=2 " +
                    "--arrange-across=9 " +
                    "assets/private/textures-tiles-town/*.png",
            ).joinToString("\n") + "\n"
        )
    }
}
