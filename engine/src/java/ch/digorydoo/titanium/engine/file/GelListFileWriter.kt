package ch.digorydoo.titanium.engine.file

import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.kutils.utils.Moment
import ch.digorydoo.titanium.BuildConfig
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.gel.SpawnPt
import ch.digorydoo.titanium.engine.utils.NotForProductionError
import java.io.BufferedWriter
import java.io.File
import java.nio.charset.StandardCharsets

class GelListFileWriter private constructor(private val writer: BufferedWriter) {
    fun write(spawnPts: List<SpawnPt>) {
        writer.write("# Written by titanium on ${Moment().formatRevDateTime()}\n")

        spawnPts.forEach { spawnPt ->
            val s = spawnPt.serialize()
                .map { (key, value) ->
                    require(Regex("[a-zA-Z0-9_\\-]*").matches(key)) { "Bad key: $key" }
                    require(Regex("[a-zA-Z0-9_\\-.]*").matches(value)) { "Bad value: $value" }
                    "$key=$value"
                }
                .joinToString(", ")
            writer.write("${spawnPt.spawnObjTypeAsString}: $s\n")
        }
    }

    companion object {
        private val TAG = Log.Tag("GelListFileWriter")

        fun writeFile(filename: String, spawnPts: List<SpawnPt>) {
            when {
                BuildConfig.isProduction -> throw NotForProductionError()
                filename.isEmpty() -> throw Exception("File name is empty!")
                else -> {
                    arrayOf(
                        App.assets.pathToGelList(filename),
                        App.assets.pathToDeveloperGelList(filename),
                    ).forEach { path ->
                        Log.info(TAG, "Writing $path")
                        File(path).bufferedWriter(StandardCharsets.UTF_8).use { writer ->
                            GelListFileWriter(writer).write(spawnPts)
                        }
                    }
                }
            }
        }
    }
}
