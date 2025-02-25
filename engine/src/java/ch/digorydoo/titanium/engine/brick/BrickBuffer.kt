package ch.digorydoo.titanium.engine.brick

import ch.digorydoo.kutils.point.Point3i
import ch.digorydoo.kutils.utils.toIntBuffer

class BrickBuffer(private val xsize: Int, private val ysize: Int, private val zsize: Int) {
    private val buffer = IntArray(xsize * ysize * zsize * COMPONENTS_PER_BRICK).toIntBuffer()

    init {
        val empty = Brick().apply { setInvalid() }

        for (z in 0 ..< zsize) {
            for (y in 0 ..< ysize) {
                for (x in 0 ..< xsize) {
                    set(x, y, z, empty)
                }
            }
        }
    }

    private fun getIndex(x: Int, y: Int, z: Int): Int? =
        if (x in 0 ..< xsize && y in 0 ..< ysize && z in 0 ..< zsize) {
            (x + y * xsize + z * xsize * ysize) * COMPONENTS_PER_BRICK
        } else {
            null
        }

    fun get(pt: Point3i, brick: Brick) =
        get(pt.x, pt.y, pt.z, brick)

    fun get(x: Int, y: Int, z: Int, brick: Brick) {
        val index = getIndex(x, y, z)

        if (index == null) {
            brick.setInvalid()
        } else {
            brick.upFaceIdx = buffer[index]
            brick.downFaceIdx = buffer[index + 1]
            brick.northFaceIdx = buffer[index + 2]
            brick.eastFaceIdx = buffer[index + 3]
            brick.southFaceIdx = buffer[index + 4]
            brick.westFaceIdx = buffer[index + 5]
            brick.shape = BrickShape.fromInt(buffer[index + 6])
            brick.material = BrickMaterial.fromInt(buffer[index + 7])
        }
    }

    fun getShape(x: Int, y: Int, z: Int): BrickShape? {
        val index = getIndex(x, y, z) ?: return null
        return BrickShape.fromInt(buffer[index + 6])
    }

    fun getMaterial(x: Int, y: Int, z: Int): BrickMaterial? {
        val index = getIndex(x, y, z) ?: return null
        return BrickMaterial.fromInt(buffer[index + 7])
    }

    fun set(x: Int, y: Int, z: Int, brick: Brick) {
        getIndex(x, y, z)?.let { index ->
            buffer.put(index, brick.upFaceIdx)
            buffer.put(index + 1, brick.downFaceIdx)
            buffer.put(index + 2, brick.northFaceIdx)
            buffer.put(index + 3, brick.eastFaceIdx)
            buffer.put(index + 4, brick.southFaceIdx)
            buffer.put(index + 5, brick.westFaceIdx)
            buffer.put(index + 6, brick.shape.id)
            buffer.put(index + 7, brick.material.value)
        }
    }

    companion object {
        private const val COMPONENTS_PER_BRICK = 8
    }
}
