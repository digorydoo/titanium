package ch.digorydoo.titanium.engine.brick

import ch.digorydoo.kutils.box.Boxi
import ch.digorydoo.kutils.box.MutableBoxi
import ch.digorydoo.kutils.math.clamp
import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.kutils.point.MutablePoint3i
import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.kutils.point.Point3i
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
    enum class BrickFaceCovering { NOT_COVERED, FULLY_COVERED, PARTIALLY_COVERED }

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
    private val tempRelCoords = MutablePoint3i()

    private fun subAt(
        brickX: Int,
        brickY: Int,
        brickZ: Int,
        outSubRelativeCoords: MutablePoint3i? = null,
    ): BrickSubvolume? {
        if (brickX !in 0 ..< xsize) return null
        if (brickY !in 0 ..< ysize) return null
        if (brickZ !in 0 ..< zsize) return null

        val bx: Int = brickX / MAX_SUB_DIMENSION
        val by: Int = brickY / MAX_SUB_DIMENSION
        val bz: Int = brickZ / MAX_SUB_DIMENSION

        val idx = bx + by * numSubsX + bz * numSubsX * numSubsY
        val sub = subs.getOrNull(idx) ?: return null

        if (outSubRelativeCoords != null) {
            outSubRelativeCoords.x = brickX % MAX_SUB_DIMENSION
            outSubRelativeCoords.y = brickY % MAX_SUB_DIMENSION
            outSubRelativeCoords.z = brickZ % MAX_SUB_DIMENSION
        }

        return sub
    }

    private fun isBrickPtInBounds(brickX: Int, brickY: Int, brickZ: Int) =
        brickX in 0 ..< xsize && brickY in 0 ..< ysize && brickZ in 0 ..< zsize

    fun isWorldPtInBounds(worldPt: Point3f): Boolean {
        val brickX = (worldPt.x / WORLD_BRICK_SIZE).toInt()
        val brickY = (worldPt.y / WORLD_BRICK_SIZE).toInt()
        val brickZ = (worldPt.z / WORLD_BRICK_SIZE).toInt()
        return isBrickPtInBounds(brickX, brickY, brickZ)
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

    fun getAtBrickCoord(
        brickCoord: Point3i,
        brick: Brick,
        outWorldCoords: MutablePoint3f? = null,
        outSubRelativeCoords: MutablePoint3i? = null,
    ) {
        getAtBrickCoord(brickCoord.x, brickCoord.y, brickCoord.z, brick, outWorldCoords, outSubRelativeCoords)
    }

    fun getAtBrickCoord(
        brickX: Int,
        brickY: Int,
        brickZ: Int,
        brick: Brick,
        outWorldCoords: MutablePoint3f? = null,
        outSubRelativeCoords: MutablePoint3i? = null,
    ) {
        val subRelCoords = outSubRelativeCoords ?: tempRelCoords
        val sub = subAt(brickX, brickY, brickZ, subRelCoords)

        if (sub == null) {
            brick.setInvalid()
            return
        }

        sub.getBrick(subRelCoords, brick, acrossBounds = false)

        if (outWorldCoords != null && brick.isValid()) {
            outWorldCoords.set(brickX * WORLD_BRICK_SIZE, brickY * WORLD_BRICK_SIZE, brickZ * WORLD_BRICK_SIZE)
        }
    }

    fun getAtWorldCoord(worldPt: Point3f, brick: Brick, outBrickCoords: MutablePoint3i? = null) {
        getAtWorldCoord(worldPt.x, worldPt.y, worldPt.z, brick, outBrickCoords)
    }

    fun getAtWorldCoord(
        worldX: Float,
        worldY: Float,
        worldZ: Float,
        brick: Brick,
        outBrickCoords: MutablePoint3i? = null,
    ) {
        val brickX = (worldX / WORLD_BRICK_SIZE).toInt()
        val brickY = (worldY / WORLD_BRICK_SIZE).toInt()
        val brickZ = (worldZ / WORLD_BRICK_SIZE).toInt()

        val sub = subAt(brickX, brickY, brickZ, tempRelCoords)

        if (sub == null) {
            brick.setInvalid()
            return
        }

        sub.getBrick(tempRelCoords, brick, acrossBounds = false)

        if (outBrickCoords != null && brick.isValid()) {
            outBrickCoords.set(brickX, brickY, brickZ)
        }
    }

    // Called during collision detection to see if a collision with a certain brick face is unlikely.
    fun getBrickFaceCovering(
        brickCoords: Point3i,
        faceNormalX: Float,
        faceNormalY: Float,
        faceNormalZ: Float,
    ): BrickFaceCovering {
        val brickX = brickCoords.x
        val brickY = brickCoords.y
        val brickZ = brickCoords.z

        val neighbourX = (brickX + faceNormalX).toInt()
        val neighbourY = (brickY + faceNormalY).toInt()
        val neighbourZ = (brickZ + faceNormalZ).toInt()

        if (neighbourX == brickX && neighbourY == brickY && neighbourZ == brickZ) {
            throw IllegalArgumentException("Invalid faceNormal")
        }

        getAtBrickCoord(brickX, brickY, brickZ, tempBrick)
        val shapeOfNeighbour = tempBrick.shape
        return when {
            !tempBrick.isValid() -> BrickFaceCovering.NOT_COVERED
            shapeOfNeighbour.relVolume <= 0.1f -> BrickFaceCovering.NOT_COVERED // e.g. UPRIGHT_BAR_NW
            neighbourX > brickX && shapeOfNeighbour.coversNorth -> BrickFaceCovering.FULLY_COVERED
            neighbourX < brickX && shapeOfNeighbour.coversSouth -> BrickFaceCovering.FULLY_COVERED
            neighbourY > brickY && shapeOfNeighbour.coversWest -> BrickFaceCovering.FULLY_COVERED
            neighbourY < brickY && shapeOfNeighbour.coversEast -> BrickFaceCovering.FULLY_COVERED
            neighbourZ > brickZ && shapeOfNeighbour.coversBrickBelow -> BrickFaceCovering.FULLY_COVERED
            neighbourZ < brickZ && shapeOfNeighbour.coversBrickAbove -> BrickFaceCovering.FULLY_COVERED
            else -> BrickFaceCovering.PARTIALLY_COVERED
        }
    }

    fun getBrickFaceCovering(brickCoords: Point3i, faceNormal: Point3f) =
        getBrickFaceCovering(brickCoords, faceNormal.x, faceNormal.y, faceNormal.z)

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
        const val WORLD_BRICK_SIZE = 1.0f // bricks are cubes of 1 metre side length
        private const val MAX_SUB_DIMENSION = 30

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
