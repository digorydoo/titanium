package ch.digorydoo.titanium.engine.editor.action

import ch.digorydoo.kutils.utils.newFloatBuffer
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.editor.Selection
import ch.digorydoo.titanium.engine.file.HeightMapFileWriter
import ch.digorydoo.titanium.engine.heightmap.HeightMap
import ch.digorydoo.titanium.engine.heightmap.HeightMapSpawnPt
import java.io.File

internal class EditHeightMapActions(private val actions: EditorActions, private val selection: Selection) {
    fun addNewHeightMap(xsize: Float, ysize: Float, numSamplesX: Int, numSamplesY: Int) {
        val id = App.spawnMgr.generateUniqueId(HEIGHT_MAP_SPAWN_OBJ_TYPE)
        val pt = selection.getPosCentreInWorldCoords().apply { z -= 0.5f }

        val filename = "$id.hmp"
        val path = App.assets.pathToHeightMap(filename)

        if (File(path).exists()) {
            throw Exception("Height map already exists: $path")
        }

        val numZValues = numSamplesX * numSamplesY
        val zvalues = newFloatBuffer(numZValues)
        zvalues.position(0)

        (0 ..< numZValues).forEach { _ ->
            zvalues.put(0.0f)
        }

        HeightMapFileWriter.write(
            HeightMap(
                filename,
                numSamplesX = numSamplesX,
                numSamplesY = numSamplesY,
                xsize = xsize,
                ysize = ysize,
                zvalues,
            )
        )

        val raw = mutableMapOf<String, String>()
        raw["id"] = id
        raw["spawnObjType"] = HEIGHT_MAP_SPAWN_OBJ_TYPE
        raw["f"] = filename
        raw["x"] = pt.x.toString()
        raw["y"] = pt.y.toString()
        raw["z"] = pt.z.toString()
        raw["rotation"] = 0.0f.toString()
        val spawnPt = App.spawnMgr.add(raw)
        actions.didAddNewHeightMap(spawnPt as HeightMapSpawnPt)
    }

    companion object {
        private const val HEIGHT_MAP_SPAWN_OBJ_TYPE = "height-map"
    }
}
