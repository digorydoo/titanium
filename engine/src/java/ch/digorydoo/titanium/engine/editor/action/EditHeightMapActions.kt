package ch.digorydoo.titanium.engine.editor.action

import ch.digorydoo.kutils.math.clamp
import ch.digorydoo.kutils.utils.newFloatBuffer
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.editor.BrickSelection
import ch.digorydoo.titanium.engine.editor.EditorState
import ch.digorydoo.titanium.engine.editor.HeightMapSelection
import ch.digorydoo.titanium.engine.editor.cursor.CursorGelHolder
import ch.digorydoo.titanium.engine.file.HeightMapFileWriter
import ch.digorydoo.titanium.engine.heightmap.HeightMap
import ch.digorydoo.titanium.engine.heightmap.HeightMapGel
import ch.digorydoo.titanium.engine.heightmap.HeightMapSpawnPt
import ch.digorydoo.titanium.engine.mesh.MeshMaterial
import io.github.digorydoo.kstruct.KstructBuilder
import java.io.File
import kotlin.math.ceil
import kotlin.math.floor

internal class EditHeightMapActions(
    private val actions: EditorActions,
    private val brickSelection: BrickSelection,
    private val cursor: CursorGelHolder,
    private val heightMapSelection: HeightMapSelection,
    private val state: EditorState,
) {
    fun addNewHeightMap(xsize: Float, ysize: Float, numSamplesX: Int, numSamplesY: Int) {
        val id = App.spawnMgr.generateUniqueId(HEIGHT_MAP_SPAWN_OBJ_TYPE)
        val pt = brickSelection.getTipPosInWorldCoords().apply { z -= 0.5f }

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
                material = MeshMaterial.DEFAULT,
            )
        )

        val spawnPt = App.spawnMgr.add(
            KstructBuilder.build {
                set("id", id)
                set("type", HEIGHT_MAP_SPAWN_OBJ_TYPE)
                set("f", filename)
                set("x", pt.x)
                set("y", pt.y)
                set("z", pt.z)
                set("rotation", 0.0f)
            }
        )
        actions.didAddNewHeightMap(spawnPt as HeightMapSpawnPt)
    }

    fun setHeightMapEditMode(heightMap: HeightMap, spawnPt: HeightMapSpawnPt) {
        state.setHeightMapEditMode(heightMap, spawnPt)
        heightMapSelection.set(0, 0)
    }

    fun setBricksEditMode() {
        state.setBricksEditMode()
    }

    fun modifyHeightOfSelectedSamples(deltaZ: Float) {
        val heightMap = state.heightMap ?: return
        val spawnPt = state.heightMapSpawnPt ?: return
        val gel = App.content.find { it.spawnPt == spawnPt } as? HeightMapGel ?: return
        val sel = heightMapSelection.getUnreversed()

        (sel.top ..< sel.bottom).forEach { y ->
            (sel.left ..< sel.right).forEach { x ->
                val z = heightMap.getAt(x, y)
                heightMap.setAt(x, y, z + deltaZ)
            }
        }

        gel.heightMapChanged()
        cursor.heightMapSelectionChanged(heightMapSelection.get())
    }

    fun resampleHeightMap(heightMap: HeightMap, gel: HeightMapGel, numSamplesX: Int, numSamplesY: Int) {
        if (heightMap.numSamplesX == numSamplesX && heightMap.numSamplesY == numSamplesY) {
            return // nothing to do
        }

        val newHeightMap = HeightMap(
            heightMap.filename,
            numSamplesX = numSamplesX,
            numSamplesY = numSamplesY,
            xsize = heightMap.xsize,
            ysize = heightMap.ysize,
            zvalues = newFloatBuffer(numSamplesX * numSamplesY),
            material = heightMap.material,
        )

        (0 ..< numSamplesY).forEach { newY ->
            (0 ..< numSamplesX).forEach { newX ->
                val oldX = clamp(
                    newX.toFloat() / (numSamplesX - 1) * (heightMap.numSamplesX - 1),
                    0.0f,
                    (heightMap.numSamplesX - 1).toFloat()
                )
                val oldY = clamp(
                    newY.toFloat() / (numSamplesY - 1) * (heightMap.numSamplesY - 1),
                    0.0f,
                    (heightMap.numSamplesY - 1).toFloat()
                )

                val ox1 = floor(oldX).toInt()
                val ox2 = ceil(oldX).toInt()
                val px = oldX - ox1 // 0 ..< 1
                val qx = 1.0f - px

                val oy1 = floor(oldY).toInt()
                val oy2 = ceil(oldY).toInt()
                val py = oldY - oy1 // 0 ..< 1
                val qy = 1.0f - py

                val z11 = heightMap.getAt(ox1, oy1)
                val z12 = heightMap.getAt(ox1, oy2)
                val z21 = heightMap.getAt(ox2, oy1)
                val z22 = heightMap.getAt(ox2, oy2)

                val newZ = (z11 * qx + z21 * px) * qy + (z12 * qx + z22 * px) * py
                newHeightMap.setAt(newX, newY, newZ)
            }
        }

        gel.replaceHeightMap(newHeightMap)
    }

    companion object {
        const val HEIGHT_MAP_SPAWN_OBJ_TYPE = "height-map"
    }
}
