package ch.digorydoo.titanium.import_asset

import ch.digorydoo.kutils.tty.Kokuban
import ch.digorydoo.kutils.tty.ShellCommandError
import ch.digorydoo.titanium.import_asset.Options.Verbosity
import ch.digorydoo.titanium.import_asset.brick_textures.BrickTextureImporter
import ch.digorydoo.titanium.import_asset.collada.ColladaFileConverter
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val kokuban = Kokuban()

    try {
        val options = Options.fromCmdLine(args)

        when (options.action) {
            Options.Action.COLLADA -> ColladaFileConverter(options).convertFiles()
            Options.Action.BRICK_TEXTURES -> BrickTextureImporter(options).importFiles()
        }

        if (options.verbosity != Verbosity.QUIET) {
            kokuban.text("Done.").println()
        }
    } catch (e: ShellCommandError) {
        kokuban.text(e.message).printlnToStdErr()
        exitProcess(1)
    } catch (e: Exception) {
        kokuban.red.bold.text("\nEXCEPTION: ").plain.text("$e").printlnToStdErr()

        val stack = e.stackTrace
            ?.joinToString("\n") { "$it" }
            ?.prependIndent("   ")

        kokuban.text(stack).printlnToStdErr()
        exitProcess(2)
    }
}
