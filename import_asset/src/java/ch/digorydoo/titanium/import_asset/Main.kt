package ch.digorydoo.titanium.import_asset

import ch.digorydoo.titanium.import_asset.Options.Verbosity
import ch.digorydoo.titanium.import_asset.brick_textures.BrickTextureImporter
import ch.digorydoo.titanium.import_asset.collada.ColladaFileConverter
import io.github.digorydoo.kokuban.ShellCommandError
import io.github.digorydoo.kokuban.toPrettyString
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    try {
        val options = Options.fromCmdLine(args)

        when (options.action) {
            Options.Action.COLLADA -> ColladaFileConverter(options).convertFiles()
            Options.Action.BRICK_TEXTURES -> BrickTextureImporter(options).importFiles()
        }

        if (options.verbosity != Verbosity.QUIET) {
            println("Done.")
        }
    } catch (e: ShellCommandError) {
        System.err.println(e.message)
        exitProcess(1)
    } catch (e: Exception) {
        System.err.println(e.toPrettyString())
        exitProcess(2)
    }
}
