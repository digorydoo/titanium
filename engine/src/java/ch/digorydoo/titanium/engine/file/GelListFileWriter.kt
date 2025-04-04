package ch.digorydoo.titanium.engine.file

import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.kutils.utils.Moment
import ch.digorydoo.titanium.engine.gel.SpawnPt
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

        fun writeFile(fileName: String, spawnPts: List<SpawnPt>) {
            if (fileName.isEmpty()) throw Exception("File name is empty!")
            Log.info(TAG, "Writing $fileName")

            // val path = Assets.pathToGelList(fileName) -- NO, not into the build folder!
            val path = "/Users/pamberg/Develop/titanium/assets/gellists/${fileName}" // FIXME

            File(path).bufferedWriter(StandardCharsets.UTF_8).use { writer ->
                GelListFileWriter(writer).write(spawnPts)
            }
        }
    }
}
