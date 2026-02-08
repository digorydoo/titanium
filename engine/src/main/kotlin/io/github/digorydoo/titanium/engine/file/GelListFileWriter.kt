package io.github.digorydoo.titanium.engine.file

import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.kutils.utils.Moment
import io.github.digorydoo.titanium.BuildConfig
import io.github.digorydoo.titanium.engine.core.App
import io.github.digorydoo.titanium.engine.gel.SpawnPt
import io.github.digorydoo.titanium.engine.utils.NotForProductionException
import io.github.digorydoo.kstruct.KstructBuilder
import io.github.digorydoo.kstruct.KstructSerialiser
import io.github.digorydoo.kstruct.KstructSerialiser.Style
import java.io.File
import java.nio.charset.StandardCharsets

object GelListFileWriter {
    private val TAG = Log.Tag("GelListFileWriter")

    fun writeFile(filename: String, spawnPts: List<SpawnPt>) {
        when {
            BuildConfig.isProduction -> throw NotForProductionException()
            filename.isEmpty() -> throw Exception("File name is empty!")
            else -> {
                arrayOf(
                    App.assets.pathToGelList(filename),
                    App.assets.pathToDeveloperGelList(filename),
                ).forEach { path ->
                    Log.info(TAG, "Writing $path")
                    File(path).bufferedWriter(StandardCharsets.UTF_8).use { writer ->
                        val content = serialise(spawnPts)
                        writer.apply {
                            write("// Written by titanium on ${Moment.now().formatAsZoneAgnosticDateTime()}\n")
                            write("$content\n")
                        }
                    }
                }
            }
        }
    }

    private fun serialise(spawnPts: List<SpawnPt>) =
        KstructSerialiser(indent = 0, Style.FLAT).serialise(
            KstructBuilder.build {
                setList("list") {
                    spawnPts.forEach {
                        addMap { it.serialise(this) }
                    }
                }
            }
        )
}
