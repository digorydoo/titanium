package ch.digorydoo.titanium.engine.file

import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.gel.SpawnPt
import java.io.BufferedReader
import java.io.File
import java.nio.charset.StandardCharsets

class GelListFileReader private constructor(private val reader: BufferedReader) {
    private fun read(result: MutableList<SpawnPt>) {
        reader.forEachLine { line ->
            try {
                if (!line.startsWith("#")) {
                    val colonAt = line.indexOf(":")
                    require(colonAt > 0) { "Missing colon: $line" }
                    val map = mutableMapOf<String, String>()
                    map["spawnObjType"] = line.substring(0 ..< colonAt)

                    line.substring(colonAt + 1).split(",").forEach { part ->
                        val eqAt = part.indexOf("=")
                        require(eqAt > 0) { "Missing eq: $part ($line)" }
                        val key = part.substring(0 ..< eqAt).trim()
                        val value = part.substring(eqAt + 1).trim()
                        map[key] = value
                    }

                    // EditorActions should assign ids to new spawn pts, so while createSpawnPt allows empty ids for
                    // ad-hoc spawn points, but this is an error inside a GelList.
                    if (!map.containsKey("id")) {
                        throw Exception("Spawn point is missing an id: $line")
                    }

                    result.add(App.factory.createSpawnPt(map))
                }
            } catch (e: Exception) {
                Log.error("Failed to create spawn point: $line")
                throw e
            }
        }
    }

    companion object {
        fun readFile(fileName: String, result: MutableList<SpawnPt>): List<SpawnPt> {
            if (fileName.isEmpty()) throw Exception("File name is empty!")

            val path = App.assets.pathToGelList(fileName)
            val file = File(path)

            file.bufferedReader(StandardCharsets.UTF_8).let { reader ->
                GelListFileReader(reader).read(result)
            }

            Log.info("GelListFileReader: $fileName: ${result.size} spawn points")
            return result
        }
    }
}
