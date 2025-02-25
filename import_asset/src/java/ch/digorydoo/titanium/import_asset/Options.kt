package ch.digorydoo.titanium.import_asset

import ch.digorydoo.kutils.tty.OptionsBuilder
import ch.digorydoo.kutils.tty.OptionsParser
import ch.digorydoo.kutils.tty.OptionsParserError
import ch.digorydoo.kutils.tty.ShellCommandError
import ch.digorydoo.titanium.import_asset.Options.Action.BRICK_TEXTURES
import ch.digorydoo.titanium.import_asset.Options.Action.COLLADA
import kotlin.system.exitProcess

class Options {
    enum class Action(val cmd: String) {
        COLLADA("collada"),
        BRICK_TEXTURES("brick-textures"),
    }

    enum class Verbosity { NORMAL, QUIET, VERBOSE }

    class OptionNotForThisActionError(action: Action):
        OptionsParserError("Option is not meant for action ${action.cmd}")

    var action = COLLADA; private set
    var arrangeAcross = 1; private set
    var onlyNewer = false; private set
    var outDir = ""; private set
    var outFile = ""; private set
    var overwrite = false; private set
    var padding = 0; private set
    var verbosity = Verbosity.NORMAL; private set
    var extraArgs = listOf<String>(); private set
    private var showHelp = false

    private fun readCmdLine(args: Array<String>) {
        if (args.isEmpty()) {
            throw ShellCommandError("Too few arguments. Use -h for help.")
        }

        val actionOrNull = args[0].lowercase().let { cmd ->
            Action.entries.firstOrNull { it.cmd == cmd }
        }

        if (actionOrNull == null) {
            if (args[0] == "-h" || args[0] == "--help") {
                printUsage()
                exitProcess(0)
            } else {
                throw ShellCommandError(
                    "Action not understood: ${args[0]}.\n" +
                        "Available actions are: ${Action.entries.joinToString(", ") { it.cmd }}"
                )
            }
        } else {
            action = actionOrNull
        }

        fun only(a: Action) {
            if (action != a) throw OptionNotForThisActionError(action)
        }

        val defs = OptionsBuilder.build {
            addBoolean("only-newer", "n") { only(COLLADA); onlyNewer = it }
            addString("out-dir", "d") { only(COLLADA); outDir = it }

            addInt("arrange-across", "a", 1, 100) { only(BRICK_TEXTURES); arrangeAcross = it }
            addString("out-file", "f") { only(BRICK_TEXTURES); outFile = it }
            addInt("padding", "p", 0, 32) { only(BRICK_TEXTURES); padding = it }

            addValueless("help", "h") { showHelp = true }
            addBoolean("overwrite", "w") { overwrite = it }
            addValueless("quiet", "q") { verbosity = Verbosity.QUIET }
            addValueless("verbose", "v") { verbosity = Verbosity.VERBOSE }
        }

        val parser = OptionsParser(defs)
        val result = parser.parse(args, startIdx = 1, allowExtraArgs = true)

        extraArgs = result.extraArgs

        if (showHelp) {
            // We come here only when both an action the the help option was given. We could give more specific
            // information here.
            printUsage()
            exitProcess(0)
        }
    }

    private fun printUsage() {
        println(
            """
            USAGE: import-asset (${Action.entries.joinToString("|") { it.cmd }}) <options> <files>
            
            When action is "${COLLADA.cmd}":
               <files> is one or more Collada file paths.
               <options> is one or more of:
               -n, --only-newer
                  Skip Collada files which are older than its existing output file.
               -d, --out-dir=/path/to/dir
                  Set the output directory.
               
            When action is "${BRICK_TEXTURES.cmd}":
               <files> is one or more brick textures (PNG).
               <options> is one or more of:
               -a, --arrange-across=n
                  When arranging textures, place n portions horizontally before starting a new row.
               -f, --out-file=/path/to/file
                  Set the filename of the generated texture.
               -p, --padding=1
                  When arranging textures, use a padding of the given number of pixels around each texture.
            
            The following options are available with all actions:
               -h, --help
                  Print this usage guide.
               -q, --quiet
                  Don't write anything to stdout except errors.
               -v, --verbose
                  Verbose mode.
               -w, --overwrite
                  Overwrite existing files.
           """.trimIndent()
        )
        println()

        // NOTE: When updating these examples, you probably also need to update make-proper.sh
        println(
            arrayOf(
                "Examples:",
                "$ ./import-asset.sh ${COLLADA.cmd} " +
                    "--out-dir=assets/generated/mesh/ " +
                    "--overwrite " +
                    "--only-newer " +
                    "assets/private/collada/*.dae",
                "$ ./import-asset.sh ${BRICK_TEXTURES.cmd} " +
                    "--out-file=assets/generated/textures/tiles-town.png " +
                    "--overwrite " +
                    "--padding=2 " +
                    "--arrange-across=9 " +
                    "assets/private/textures-tiles-town/*.png",
            ).joinToString("\n")
        )
    }

    companion object {
        fun fromCmdLine(args: Array<String>) =
            Options().apply { readCmdLine(args) }
    }
}
