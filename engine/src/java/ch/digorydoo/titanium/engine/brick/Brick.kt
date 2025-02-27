package ch.digorydoo.titanium.engine.brick

import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.kutils.point.MutablePoint3i
import ch.digorydoo.kutils.point.Point3f

/**
 * This class holds all properties assigned to a given brick positition.
 */
class Brick private constructor(
    var shape: BrickShape,
    var material: BrickMaterial,
    var upFaceIdx: Int,
    var downFaceIdx: Int,
    var northFaceIdx: Int,
    var southFaceIdx: Int,
    var eastFaceIdx: Int,
    var westFaceIdx: Int,
) {
    constructor(): this(
        shape = BrickShape.NONE,
        material = BrickMaterial.GREY_CONCRETE,
        upFaceIdx = -1,
        downFaceIdx = -1,
        northFaceIdx = -1,
        southFaceIdx = -1,
        eastFaceIdx = -1,
        westFaceIdx = -1,
    )

    constructor(other: Brick): this(
        shape = other.shape,
        material = other.material,
        upFaceIdx = other.upFaceIdx,
        downFaceIdx = other.downFaceIdx,
        northFaceIdx = other.northFaceIdx,
        southFaceIdx = other.southFaceIdx,
        eastFaceIdx = other.eastFaceIdx,
        westFaceIdx = other.westFaceIdx,
    )

    fun isValid() =
        shape != BrickShape.NONE

    fun setInvalid() {
        shape = BrickShape.NONE
        material = BrickMaterial.GREY_CONCRETE
        upFaceIdx = -1
        downFaceIdx = -1
        northFaceIdx = -1
        southFaceIdx = -1
        eastFaceIdx = -1
        westFaceIdx = -1
    }

    fun set(other: Brick) {
        shape = other.shape
        material = other.material
        upFaceIdx = other.upFaceIdx
        downFaceIdx = other.downFaceIdx
        northFaceIdx = other.northFaceIdx
        southFaceIdx = other.southFaceIdx
        eastFaceIdx = other.eastFaceIdx
        westFaceIdx = other.westFaceIdx
    }

    override fun toString() =
        "Brick($shape, $material)"

    companion object {
        const val WORLD_BRICK_SIZE = 1.0f // bricks are cubes of 1 metre side length

        fun brickToWorld(brickX: Int, brickY: Int, brickZ: Int, result: MutablePoint3f) {
            result.set(
                brickX.toFloat() * WORLD_BRICK_SIZE,
                brickY.toFloat() * WORLD_BRICK_SIZE,
                brickZ.toFloat() * WORLD_BRICK_SIZE,
            )
        }

        fun brickToWorld(brickX: Float, brickY: Float, brickZ: Float, result: MutablePoint3f) {
            result.set(
                brickX * WORLD_BRICK_SIZE,
                brickY * WORLD_BRICK_SIZE,
                brickZ * WORLD_BRICK_SIZE,
            )
        }

        fun worldToBrick(worldPt: Point3f, brickPos: MutablePoint3i) {
            brickPos.set(
                (worldPt.x / WORLD_BRICK_SIZE).toInt(),
                (worldPt.y / WORLD_BRICK_SIZE).toInt(),
                (worldPt.z / WORLD_BRICK_SIZE).toInt(),
            )
        }
    }
}
