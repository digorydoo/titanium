package ch.digorydoo.titanium.engine.brick

import ch.digorydoo.kutils.math.clamp
import ch.digorydoo.kutils.point.MutablePoint2f
import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.kutils.point.MutablePoint3i
import ch.digorydoo.kutils.point.Point2f
import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.titanium.engine.utils.Side
import ch.digorydoo.titanium.engine.utils.Side.*
import kotlin.math.abs

class Brick private constructor(
    var shape: BrickShape,
    var material: BrickMaterial,
    var upFaceIdx: Int,
    var downFaceIdx: Int,
    var northFaceIdx: Int,
    var southFaceIdx: Int,
    var eastFaceIdx: Int,
    var westFaceIdx: Int,
    val worldCoords: MutablePoint3f, // world coordinates; may be any point inside brick, e.g. collision check pt
    val brickCoords: MutablePoint3i, // absolute integer brick coordinates
    val relBrickCoords: MutablePoint3i, // relative to BrickSubvolume
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
        worldCoords = MutablePoint3f(),
        brickCoords = MutablePoint3i(),
        relBrickCoords = MutablePoint3i(),
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
        worldCoords = MutablePoint3f(other.worldCoords),
        brickCoords = MutablePoint3i(other.brickCoords),
        relBrickCoords = MutablePoint3i(other.relBrickCoords),
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
        worldCoords.set(0, 0, 0)
        brickCoords.set(0, 0, 0)
        relBrickCoords.set(0, 0, 0)
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
        worldCoords.set(other.worldCoords)
        brickCoords.set(other.brickCoords)
        relBrickCoords.set(other.relBrickCoords)
    }

    fun setFacesFromMaterialAndBrickCoords() {
        val bfa = BrickFaceAssigner(this)
        bfa.setFacesFromMaterialAndBrickCoords()
    }

    fun isOnSameBrick(other: Brick) =
        brickCoords.hasSameValues(other.brickCoords)

    fun getClosestSide(pt: Point2f): Side {
        val x1 = brickCoords.x * WORLD_BRICK_SIZE
        val y1 = brickCoords.y * WORLD_BRICK_SIZE
        val x2 = x1 + WORLD_BRICK_SIZE
        val y2 = y1 + WORLD_BRICK_SIZE

        val dx1 = abs(pt.x - x1)
        val dy1 = abs(pt.y - y1)
        val dx2 = abs(pt.x - x2)
        val dy2 = abs(pt.y - y2)

        return when {
            dx1 < dy1 && dx1 < dx2 && dx1 < dy2 -> WEST
            dy1 < dx1 && dy1 < dx2 && dy1 < dy2 -> NORTH
            dx2 < dx1 && dx2 < dy1 && dx2 < dy2 -> EAST
            else -> SOUTH
        }
    }

    override fun toString() =
        "Brick($shape, $material, world=$worldCoords, brick=$brickCoords, relBrick=$relBrickCoords)"

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

fun MutablePoint2f.clampToWorldCoordsOf(brick: Brick) {
    // -0.1f: This isn't correct, but we need to ensure the resulting pt is within brick bounds.
    val x1 = brick.brickCoords.x * Brick.WORLD_BRICK_SIZE
    val y1 = brick.brickCoords.y * Brick.WORLD_BRICK_SIZE
    val x2 = x1 + Brick.WORLD_BRICK_SIZE - 0.1f
    val y2 = y1 + Brick.WORLD_BRICK_SIZE - 0.1f
    x = clamp(x, x1, x2)
    y = clamp(y, y1, y2)
}
