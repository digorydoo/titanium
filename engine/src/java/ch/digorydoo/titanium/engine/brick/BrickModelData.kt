package ch.digorydoo.titanium.engine.brick

import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.kutils.point.Point3i
import ch.digorydoo.kutils.utils.newFloatBuffer
import java.nio.FloatBuffer

class BrickModelData(private val subvolume: BrickSubvolume, private val models: BrickModelHolder) {
    class FloatLists {
        val positions = mutableListOf<Float>()
        val normals = mutableListOf<Float>()
        val texCoords = mutableListOf<Float>()

        fun clear() {
            positions.clear()
            normals.clear()
            texCoords.clear()
        }
    }

    class BufferIndices(val skip: Int, val count: Int)

    private class Part(translation: Point3f, texWidth: Int, texHeight: Int) {
        val trianglesList = FloatLists()
        var trianglesIndices: BufferIndices? = null
        // val strips: TODO
        // val fans: TODO

        private var _tesselator: Tesselator? = Tesselator(translation, texWidth, texHeight, trianglesList)
        val tesselator get() = _tesselator!!

        fun seal() {
            trianglesList.clear()
            _tesselator = null
        }
    }

    class RenderData(val positions: FloatBuffer, val normals: FloatBuffer, val texCoords: FloatBuffer)

    private val parts = mutableMapOf<BrickMaterial, MutableMap<BrickShape, Part>>()

    lateinit var solidRenderData: RenderData
    lateinit var transparentRenderData: RenderData

    val numPositions get() = solidRenderData.positions.limit() + transparentRenderData.positions.limit()
    val numNormals get() = solidRenderData.normals.limit() + transparentRenderData.normals.limit()
    val numTexCoords get() = solidRenderData.texCoords.limit() + transparentRenderData.texCoords.limit()

    fun distanceFromCamera() =
        subvolume.distanceFromCamera()

    fun forEachMaterial(
        lambda: (
            material: BrickMaterial,
            forEachShape: (innerLambda: (shape: BrickShape, triangles: BufferIndices) -> Unit) -> Unit,
        ) -> Unit,
    ) {
        parts.forEach { (material, shapes) ->
            lambda(material) { innerLambda ->
                shapes.forEach { (shape, part) ->
                    part.trianglesIndices?.let { innerLambda(shape, it) }
                }
            }
        }
    }

    fun rebuild(translation: Point3f, texWidth: Int, texHeight: Int) {
        parts.clear()

        subvolume.forEachBrick { brick, ix, iy, iz ->
            val partsOfMaterial = parts[brick.material]
                ?: mutableMapOf<BrickShape, Part>().also { parts[brick.material] = it }

            val part = partsOfMaterial[brick.shape]
                ?: Part(translation, texWidth, texHeight).also { partsOfMaterial[brick.shape] = it }

            val tess = part.tesselator

            models.get(brick.shape)?.apply {
                prepare(ix, iy, iz, subvolume)
                tesselateUpFace(tess)
                tesselateDownFace(tess)
                tesselateNorthFace(tess)
                tesselateSouthFace(tess)
                tesselateEastFace(tess)
                tesselateWestFace(tess)
            }
        }

        solidRenderData = gatherRenderData(solid = true)
        transparentRenderData = gatherRenderData(solid = false)
    }

    private fun gatherRenderData(solid: Boolean): RenderData {
        var totalNumPos = 0
        var totalNumNormals = 0
        var totalNumTexCoords = 0

        parts.forEach { (brickMaterial, partsOfMaterial) ->
            if (brickMaterial.solid == solid) {
                partsOfMaterial.forEach { (_, part) ->
                    totalNumPos += part.trianglesList.positions.size
                    totalNumNormals += part.trianglesList.normals.size
                    totalNumTexCoords += part.trianglesList.texCoords.size
                }
            }
        }

        val positions = newFloatBuffer(totalNumPos)
        val normals = newFloatBuffer(totalNumNormals)
        val texCoords = newFloatBuffer(totalNumTexCoords)

        parts.forEach { (brickMaterial, partsOfMaterial) ->
            if (brickMaterial.solid == solid) {
                partsOfMaterial.forEach { (_, part) ->
                    val skip = positions.position() / 3 // three components x, y, z per pos
                    val count = part.trianglesList.positions.size / 3
                    part.trianglesIndices = BufferIndices(skip, count)

                    part.trianglesList.positions.forEach { positions.put(it) }
                    part.trianglesList.normals.forEach { normals.put(it) }
                    part.trianglesList.texCoords.forEach { texCoords.put(it) }

                    part.seal()
                }
            }
        }

        return RenderData(positions, normals, texCoords)
    }

    fun heightAt(relWorldX: Float, relWorldY: Float, relBrickCoords: Point3i): Float? {
        val shape = subvolume.getShape(relBrickCoords) ?: return null
        val model = models.get(shape) ?: return null // returns null when shape is NONE
        model.prepare(relBrickCoords.x, relBrickCoords.y, relBrickCoords.z, subvolume)
        return model.heightAt(relWorldX, relWorldY) // returns null when brick is empty at this xy
    }
}
