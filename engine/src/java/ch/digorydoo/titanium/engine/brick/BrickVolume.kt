package ch.digorydoo.titanium.engine.brick

import ch.digorydoo.kutils.box.Boxi
import ch.digorydoo.kutils.box.MutableBoxi
import ch.digorydoo.kutils.math.clamp
import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.kutils.point.MutablePoint3i
import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.kutils.point.Point3i
import ch.digorydoo.titanium.engine.brick.Brick.Companion.WORLD_BRICK_SIZE
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.texture.Texture
import kotlin.math.ceil

/**
 * A BrickVolume holds the brick model data of a large area. BrickVolume is divided into multiple BrickSubvolume for
 * efficient rendering. Currently, there is only one instance of BrickVolume (App.bricks).
 */
class BrickVolume(
    val xsize: Int,
    val ysize: Int,
    val zsize: Int,
    val fileName: String,
    tex: Texture,
) {
    private val models = BrickModelHolder()
    private val subs: Array<BrickSubvolume>
    private val numSubsX: Int = (ceil(xsize.toFloat() / MAX_SUB_DIMENSION)).toInt()
    private val numSubsY: Int = (ceil(ysize.toFloat() / MAX_SUB_DIMENSION)).toInt()
    private val numSubsZ: Int = (ceil(zsize.toFloat() / MAX_SUB_DIMENSION)).toInt()

    init {
        val list = mutableListOf<BrickSubvolume>()

        for (z in 0 ..< numSubsZ) {
            val zs = if (z < numSubsZ - 1) MAX_SUB_DIMENSION else zsize - ((numSubsZ - 1) * MAX_SUB_DIMENSION)

            for (y in 0 ..< numSubsY) {
                val ys = if (y < numSubsY - 1) MAX_SUB_DIMENSION else ysize - ((numSubsY - 1) * MAX_SUB_DIMENSION)

                for (x in 0 ..< numSubsX) {
                    val xs = if (x < numSubsX - 1) MAX_SUB_DIMENSION else xsize - ((numSubsX - 1) * MAX_SUB_DIMENSION)

                    // box spans the bricks of the subvolume
                    val box = Boxi(
                        x * MAX_SUB_DIMENSION,
                        y * MAX_SUB_DIMENSION,
                        z * MAX_SUB_DIMENSION,
                        x * MAX_SUB_DIMENSION + xs,
                        y * MAX_SUB_DIMENSION + ys,
                        z * MAX_SUB_DIMENSION + zs,
                    )

                    val sub = BrickSubvolume(this, models, box, tex)
                    list.add(sub)
                }
            }
        }

        subs = list.toTypedArray()
    }

    val numSubVolumes = subs.size
    val totalNumPositions get() = subs.fold(0) { result, sub -> result + sub.numPositions }
    val totalNumNormals get() = subs.fold(0) { result, sub -> result + sub.numNormals }
    val totalNumTexCoords get() = subs.fold(0) { result, sub -> result + sub.numTexCoords }

    private val walker = BrickWalker(this)
    private val tempBrick = Brick()

    private fun subAt(brickCoords: Point3i) =
        subAt(brickCoords.x, brickCoords.y, brickCoords.z)

    private fun subAt(brickX: Int, brickY: Int, brickZ: Int): BrickSubvolume? {
        if (brickX !in 0 ..< xsize) return null
        if (brickY !in 0 ..< ysize) return null
        if (brickZ !in 0 ..< zsize) return null
        val bx: Int = brickX / MAX_SUB_DIMENSION
        val by: Int = brickY / MAX_SUB_DIMENSION
        val bz: Int = brickZ / MAX_SUB_DIMENSION
        val idx = bx + by * numSubsX + bz * numSubsX * numSubsY
        return subs.getOrNull(idx)
    }

    fun clampToSize(brickPos: MutablePoint3i) {
        brickPos.x = clamp(brickPos.x, 0, xsize - 1)
        brickPos.y = clamp(brickPos.y, 0, ysize - 1)
        brickPos.z = clamp(brickPos.z, 0, zsize - 1)
    }

    fun setAtBrickCoord(brickX: Int, brickY: Int, brickZ: Int, brick: Brick) {
        val bx = brickX % MAX_SUB_DIMENSION
        val by = brickY % MAX_SUB_DIMENSION
        val bz = brickZ % MAX_SUB_DIMENSION
        subAt(brickX, brickY, brickZ)?.setBrick(bx, by, bz, brick, acrossBounds = false)
    }

    fun getAtBrickCoord(brickCoord: Point3i, brick: Brick) =
        getAtBrickCoord(brickCoord.x, brickCoord.y, brickCoord.z, brick)

    fun getAtBrickCoord(brickX: Int, brickY: Int, brickZ: Int, brick: Brick): Boolean {
        brick.brickCoords.set(brickX, brickY, brickZ)

        brick.relBrickCoords.set(
            brickX % MAX_SUB_DIMENSION,
            brickY % MAX_SUB_DIMENSION,
            brickZ % MAX_SUB_DIMENSION,
        )

        val sub = subAt(brickX, brickY, brickZ)

        if (sub != null) {
            if (sub.getBrick(brick.relBrickCoords, brick, acrossBounds = false)) {
                brick.worldCoords.set(brickX * WORLD_BRICK_SIZE, brickY * WORLD_BRICK_SIZE, brickZ * WORLD_BRICK_SIZE)
                return true
            }
        }

        brick.setInvalid()
        return false
    }

    fun hasValidBrickAt(brickX: Int, brickY: Int, brickZ: Int): Boolean {
        getAtBrickCoord(brickX, brickY, brickZ, tempBrick)
        return tempBrick.isValid()
    }

    fun getAtWorldCoord(worldPt: Point3f, brick: Brick) =
        getAtWorldCoord(worldPt.x, worldPt.y, worldPt.z, brick)

    fun getAtWorldCoord(worldX: Float, worldY: Float, worldZ: Float, brick: Brick): Boolean {
        brick.brickCoords.set(
            (worldX / WORLD_BRICK_SIZE).toInt(),
            (worldY / WORLD_BRICK_SIZE).toInt(),
            (worldZ / WORLD_BRICK_SIZE).toInt(),
        )

        brick.relBrickCoords.set(
            brick.brickCoords.x % MAX_SUB_DIMENSION,
            brick.brickCoords.y % MAX_SUB_DIMENSION,
            brick.brickCoords.z % MAX_SUB_DIMENSION,
        )

        val sub = subAt(brick.brickCoords)

        if (sub != null) {
            if (sub.getBrick(brick.relBrickCoords, brick, acrossBounds = false)) {
                brick.worldCoords.set(worldX, worldY, worldZ)
                return true
            }
        }

        brick.setInvalid()
        return false
    }

    fun getFloor(worldX: Float, worldY: Float, worldZ: Float, brick: Brick) {
        val checkPt = MutablePoint3f(worldX, worldY, worldZ + WORLD_BRICK_SIZE)
        getAtWorldCoord(checkPt.x, checkPt.y, checkPt.z, brick)

        while (checkPt.z > 0) {
            if (brick.isValid()) {
                val h = subAt(brick.brickCoords)?.heightAt(
                    brick.worldCoords.x,
                    brick.worldCoords.y,
                    brick.relBrickCoords,
                )

                if (h != null && h <= worldZ) {
                    brick.worldCoords.z = h
                    return
                }
            }

            checkPt.z -= WORLD_BRICK_SIZE
            getAtWorldCoord(checkPt.x, checkPt.y, checkPt.z, brick)
        }

        brick.worldCoords.z = 0.0f
    }

    fun getFloorZ(worldPt: Point3f): Float {
        getFloor(worldPt.x, worldPt.y, worldPt.z, tempBrick)
        return tempBrick.worldCoords.z
    }

    private fun getTopFloorZ(worldX: Float, worldY: Float): Float {
        getFloor(worldX, worldY, zsize * WORLD_BRICK_SIZE, tempBrick)
        return tempBrick.worldCoords.z
    }

    fun getPtWithTopFloorZ(x: Float, y: Float) =
        Point3f(x, y, getTopFloorZ(x, y))

    fun forEachBrickOnWorldLine(
        worldStartPt: Point3f,
        worldEndPt: Point3f,
        lambda: (brick: Brick, worldPtOfBrickEntry: Point3f, rayVector: Point3f) -> Boolean,
    ) = walker.forEachBrickOnWorldLine(worldStartPt, worldEndPt, lambda)

    fun updateAll() {
        subs.forEach { it.update() }
    }

    fun updateBricks(box: Boxi, updateRunsAcross: Boolean) {
        subs.forEach { sub ->
            if (sub.overlaps(box)) {
                sub.update()
            }
        }

        if (updateRunsAcross) {
            val border = MutableBoxi(box)
            border.x0 = box.x0 - 1
            border.x1 = box.x0
            updateBricks(border, false)

            border.x0 = box.x1
            border.x1 = box.x1 + 1
            updateBricks(border, false)

            border.x0 = box.x0
            border.x1 = box.x1
            border.y0 = box.y0 - 1
            border.y1 = box.y0
            updateBricks(border, false)

            border.y0 = box.y1
            border.y1 = box.y1 + 1
            updateBricks(border, false)
        }
    }

    fun renderShadows() {
        if (App.gameMenu.isShown) return
        subs.forEach { it.renderShadows() }
    }

    fun renderSolid() {
        if (App.gameMenu.isShown) return
        subs.forEach { it.renderSolid() }
    }

    fun renderTransparent() {
        if (App.gameMenu.isShown) return
        subs.forEach { it.renderTransparent() }
    }

    fun free() {
        subs.forEach { it.free() }
    }

    companion object {
        private const val MAX_SUB_DIMENSION = 30
    }
}
