package ch.digorydoo.titanium.engine.brick

import ch.digorydoo.titanium.engine.brick.Brick.Companion.WORLD_BRICK_SIZE

abstract class AbstrBrickModel {
    protected val upside = Floor()
    protected val downside = Floor()

    protected val upFaceIdx get() = upside.brick.upFaceIdx
    protected val downFaceIdx get() = upside.brick.downFaceIdx
    protected val northFaceIdx get() = upside.brick.northFaceIdx
    protected val southFaceIdx get() = upside.brick.southFaceIdx
    protected val eastFaceIdx get() = upside.brick.eastFaceIdx
    protected val westFaceIdx get() = upside.brick.westFaceIdx

    protected var shapeAbove = BrickShape.NONE; private set
    protected var shapeBelow = BrickShape.NONE; private set
    protected var shapeToNorth = BrickShape.NONE; private set
    protected var shapeToEast = BrickShape.NONE; private set
    protected var shapeToSouth = BrickShape.NONE; private set
    protected var shapeToWest = BrickShape.NONE; private set

    private val myMaterial get() = upside.brick.material
    private var matAbove = BrickMaterial.GREY_CONCRETE
    private var matBelow = BrickMaterial.GREY_CONCRETE
    private var matToNorth = BrickMaterial.GREY_CONCRETE
    private var matToEast = BrickMaterial.GREY_CONCRETE
    private var matToSouth = BrickMaterial.GREY_CONCRETE
    private var matToWest = BrickMaterial.GREY_CONCRETE

    open fun prepare(ix: Int, iy: Int, iz: Int, subvolume: BrickSubvolume) {
        upside.set(ix, iy, iz, subvolume)
        downside.set(ix, iy, iz, subvolume, true)

        // getShape and getMaterial are fast enough so we can always fetch all neighbours. If this is no longer true in
        // the future, we should remember ix, iy, iz and implement getShape lazily from shapeAbove, etc.

        shapeAbove = subvolume.getShape(ix, iy, iz + 1, acrossBounds = true) ?: BrickShape.NONE
        shapeBelow = subvolume.getShape(ix, iy, iz - 1, acrossBounds = true) ?: BrickShape.NONE
        shapeToNorth = subvolume.getShape(ix - 1, iy, iz, acrossBounds = true) ?: BrickShape.NONE
        shapeToEast = subvolume.getShape(ix, iy + 1, iz, acrossBounds = true) ?: BrickShape.NONE
        shapeToSouth = subvolume.getShape(ix + 1, iy, iz, acrossBounds = true) ?: BrickShape.NONE
        shapeToWest = subvolume.getShape(ix, iy - 1, iz, acrossBounds = true) ?: BrickShape.NONE

        matAbove = subvolume.getMaterial(ix, iy, iz + 1, acrossBounds = true) ?: BrickMaterial.GREY_CONCRETE
        matBelow = subvolume.getMaterial(ix, iy, iz - 1, acrossBounds = true) ?: BrickMaterial.GREY_CONCRETE
        matToNorth = subvolume.getMaterial(ix - 1, iy, iz, acrossBounds = true) ?: BrickMaterial.GREY_CONCRETE
        matToEast = subvolume.getMaterial(ix, iy + 1, iz, acrossBounds = true) ?: BrickMaterial.GREY_CONCRETE
        matToSouth = subvolume.getMaterial(ix + 1, iy, iz, acrossBounds = true) ?: BrickMaterial.GREY_CONCRETE
        matToWest = subvolume.getMaterial(ix, iy - 1, iz, acrossBounds = true) ?: BrickMaterial.GREY_CONCRETE
    }

    abstract fun tesselateUpFace(tess: Tesselator)
    abstract fun tesselateDownFace(tess: Tesselator)
    abstract fun tesselateNorthFace(tess: Tesselator)
    abstract fun tesselateSouthFace(tess: Tesselator)
    abstract fun tesselateEastFace(tess: Tesselator)
    abstract fun tesselateWestFace(tess: Tesselator)
    abstract fun heightAt(x: Float, y: Float): Float?

    protected fun isBrickAboveFullyCoveringMyUpFace() = shapeAbove.coversBrickBelow && isCoveredBy(matAbove)
    protected fun isBrickBelowFullyCoveringMyDownFace() = shapeBelow.coversBrickAbove && isCoveredBy(matBelow)
    protected fun isBrickToNorthFullyCoveringMyNorthFace() = shapeToNorth.coversSouth && isCoveredBy(matToNorth)
    protected fun isBrickToEastFullyCoveringMyEastFace() = shapeToEast.coversWest && isCoveredBy(matToEast)
    protected fun isBrickToSouthFullyCoveringMySouthFace() = shapeToSouth.coversNorth && isCoveredBy(matToSouth)
    protected fun isBrickToWestFullyCoveringMyWestFace() = shapeToWest.coversEast && isCoveredBy(matToWest)

    private fun isCoveredBy(otherMaterial: BrickMaterial) =
        when {
            otherMaterial.solid -> true
            myMaterial == otherMaterial -> true // two GLASS bricks standing next to each other should merge
            else -> false
        }

    companion object {
        const val BEVEL_CUT_SIZE = 0.2f
        const val BEVEL_Z_DELTA = 0.2f // metres
        const val BEVEL_Z_RELDELTA = BEVEL_Z_DELTA / WORLD_BRICK_SIZE
        const val THIN_INSET = 0.9f
        const val THIN_RELSIZE = 1.0f - THIN_INSET
    }
}
