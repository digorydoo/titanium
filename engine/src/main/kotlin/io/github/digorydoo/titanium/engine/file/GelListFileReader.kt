package io.github.digorydoo.titanium.engine.file

import ch.digorydoo.kutils.utils.Log
import io.github.digorydoo.titanium.engine.core.App
import io.github.digorydoo.titanium.engine.gel.SpawnPt
import io.github.digorydoo.kstruct.KstructList
import io.github.digorydoo.kstruct.KstructMap
import io.github.digorydoo.kstruct.parser.KstructParser
import java.io.File
import java.nio.charset.StandardCharsets

object GelListFileReader {
    private val TAG = Log.Tag("GelListFileReader")

    fun readFile(fileName: String, result: MutableList<SpawnPt>): List<SpawnPt> {
        if (fileName.isEmpty()) throw Exception("File name is empty!")

        val path = App.assets.pathToGelList(fileName)
        val file = File(path)

        file.bufferedReader(StandardCharsets.UTF_8).let { reader ->
            val root = KstructParser.parse(reader.readText())
            val listNode = root["list"] ?: throw Exception("Missing member 'list'")
            val list = listNode as? KstructList ?: throw Exception("Member 'list' is not a list")

            list.forEachChild { i, child ->
                val map = (child as? KstructMap) ?: throw Exception("Member [$i] not a map!")
                result.add(App.factory.createSpawnPt(map))
            }
        }

        Log.info(TAG, "$fileName: ${result.size} spawn points")
        return result
    }
}
