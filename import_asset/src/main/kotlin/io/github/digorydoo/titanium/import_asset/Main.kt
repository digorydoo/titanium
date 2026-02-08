package io.github.digorydoo.titanium.import_asset

import io.github.digorydoo.titanium.import_asset.brick_textures.BrickTextureImporter
import io.github.digorydoo.titanium.import_asset.collada.ColladaFileConverter
import io.github.digorydoo.titanium.import_asset.options.ImportBrickTexturesOptions
import io.github.digorydoo.titanium.import_asset.options.ImportColladaOptions
import io.github.digorydoo.titanium.import_asset.options.Options
import io.github.digorydoo.kokuban.ShellCommandError
import io.github.digorydoo.kokuban.toPrettyString
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    try {
        val options = Options.fromCmdLine(args)
        when (options) {
            is ImportColladaOptions -> ColladaFileConverter(options).convertFiles()
            is ImportBrickTexturesOptions -> BrickTextureImporter(options).importFiles()
        }
    } catch (e: ShellCommandError) {
        System.err.println(e.message)
        exitProcess(1)
    } catch (e: Exception) {
        System.err.println(e.toPrettyString())
        exitProcess(2)
    }
}
