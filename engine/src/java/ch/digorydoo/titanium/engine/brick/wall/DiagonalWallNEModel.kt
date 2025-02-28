package ch.digorydoo.titanium.engine.brick.wall

import ch.digorydoo.kutils.geometry.isPtInTriangle
import ch.digorydoo.kutils.math.lerp
import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.titanium.engine.brick.AbstrBrickModel
import ch.digorydoo.titanium.engine.brick.BrickSubvolume
import ch.digorydoo.titanium.engine.brick.BrickVolume.Companion.WORLD_BRICK_SIZE
import ch.digorydoo.titanium.engine.brick.Tesselator
import ch.digorydoo.titanium.engine.utils.Direction
import kotlin.math.sqrt

class DiagonalWallNEModel: AbstrBrickModel() {
    private val upperPt0 = MutablePoint3f()
    private val upperPt1 = MutablePoint3f()
    private val upperPt2 = MutablePoint3f()
    private val upperPt3 = MutablePoint3f()
    private val lowerPt0 = MutablePoint3f()
    private val lowerPt1 = MutablePoint3f()
    private val lowerPt2 = MutablePoint3f()
    private val lowerPt3 = MutablePoint3f()

    override fun prepare(ix: Int, iy: Int, iz: Int, subvolume: BrickSubvolume) {
        super.prepare(ix, iy, iz, subvolume)

        upperPt0.set(
            upside.pt1.x,
            lerp(upside.pt1.y, upside.pt0.y, THIN_RELSIZE),
            upside.pt1.z
        )

        upperPt1.set(
            upperPt0.x - diagonalWallSizeX,
            upperPt0.y + diagonalWallSizeX,
            upperPt0.z
        )

        upperPt2.set(
            lerp(upside.pt2.x, upside.pt0.x, THIN_RELSIZE),
            upside.pt2.y,
            upside.pt2.z
        )

        upperPt3.set(
            upperPt2.x - diagonalWallSizeX,
            upperPt2.y + diagonalWallSizeX,
            upperPt2.z
        )

        lowerPt0.set(upperPt0.x, upperPt0.y, downside.pt0.z)
        lowerPt1.set(upperPt1.x, upperPt1.y, downside.pt1.z)
        lowerPt2.set(upperPt2.x, upperPt2.y, downside.pt2.z)
        lowerPt3.set(upperPt3.x, upperPt3.y, downside.pt3.z)
    }

    override fun heightAt(x: Float, y: Float) =
        if (
            isPtInTriangle(x, y, upside.pt1.x, upside.pt1.y, upperPt1.x, upperPt1.y, upperPt0.x, upperPt0.y) ||
            isPtInTriangle(x, y, upperPt0.x, upperPt0.y, upperPt1.x, upperPt1.y, upperPt2.x, upperPt2.y) ||
            isPtInTriangle(x, y, upperPt1.x, upperPt1.y, upperPt3.x, upperPt3.y, upperPt2.x, upperPt2.y) ||
            isPtInTriangle(x, y, upside.pt2.x, upside.pt2.y, upperPt2.x, upperPt2.y, upperPt3.x, upperPt3.y)
        ) {
            upside.pt2.z
        } else {
            null
        }

    override fun tesselateUpFace(tess: Tesselator) {
        if (isBrickAboveFullyCoveringMyUpFace()) {
            return
        }

        tess.addTriangle(
            upside.pt2,
            upperPt2,
            upperPt3,
            upFaceIdx,
            Direction.upVector,
            0.0f,
            texRelWidth = THIN_RELSIZE,
            texRelHeight = shortFaceRelSize,
        )
        tess.addQuad(
            upperPt0,
            upperPt1,
            upperPt2,
            upperPt3,
            upFaceIdx,
            Direction.upVector,
            texRelWidth = THIN_RELSIZE,
            texRelY = shortFaceRelSize,
            texRelHeight = 1.0f - 2 * shortFaceRelSize,
        )
        tess.addTriangle(
            upside.pt1,
            upperPt1,
            upperPt0,
            upFaceIdx,
            Direction.upVector,
            1.0f,
            flipTexX = true,
            flipTexY = true,
            texRelY = 1.0f - shortFaceRelSize,
            texRelWidth = THIN_RELSIZE,
            texRelHeight = shortFaceRelSize,
        )
    }

    override fun tesselateDownFace(tess: Tesselator) {
        if (isBrickBelowFullyCoveringMyDownFace()) {
            return
        }

        tess.addTriangle(
            downside.pt2,
            lowerPt3,
            lowerPt2,
            downFaceIdx,
            Direction.downVector,
            0.0f,
            texRelWidth = THIN_RELSIZE,
            texRelHeight = shortFaceRelSize,
        )
        tess.addQuad(
            lowerPt1,
            lowerPt0,
            lowerPt3,
            lowerPt2,
            downFaceIdx,
            Direction.downVector,
            texRelWidth = THIN_RELSIZE,
            texRelY = shortFaceRelSize,
            texRelHeight = 1.0f - 2 * shortFaceRelSize,
        )
        tess.addTriangle(
            downside.pt1,
            lowerPt0,
            lowerPt1,
            downFaceIdx,
            Direction.downVector,
            1.0f,
            flipTexX = true,
            flipTexY = true,
            texRelY = 1.0f - shortFaceRelSize,
            texRelWidth = THIN_RELSIZE,
            texRelHeight = shortFaceRelSize,
        )
    }

    override fun tesselateNorthFace(tess: Tesselator) {
        tess.addQuad(
            downside.pt1,
            lowerPt1,
            upside.pt1,
            upperPt1,
            northFaceIdx,
            null, // direction is about ENE
            texRelWidth = shortFaceRelSize,
        )
        tess.addQuad(
            lowerPt1,
            lowerPt3,
            upperPt1,
            upperPt3,
            northFaceIdx,
            Direction.northEastVector,
            texRelX = shortFaceRelSize,
            texRelWidth = 1.0f - 2 * shortFaceRelSize,
        )
        tess.addQuad(
            lowerPt3,
            downside.pt2,
            upperPt3,
            upside.pt2,
            northFaceIdx,
            null, // direction is about NNE
            texRelX = 1.0f - shortFaceRelSize,
            texRelWidth = shortFaceRelSize,
        )
    }

    override fun tesselateSouthFace(tess: Tesselator) {
        tess.addQuad(
            lowerPt2,
            lowerPt0,
            upperPt2,
            upperPt0,
            southFaceIdx,
            Direction.southWestVector,
        )
        tess.addQuad(
            lowerPt0,
            downside.pt1,
            upperPt0,
            upside.pt1,
            southFaceIdx,
            Direction.southVector,
            texRelWidth = THIN_RELSIZE,
        )
    }

    override fun tesselateEastFace(tess: Tesselator) {
    }

    override fun tesselateWestFace(tess: Tesselator) {
        tess.addQuad(
            downside.pt2,
            lowerPt2,
            upside.pt2,
            upperPt2,
            westFaceIdx,
            Direction.westVector,
            texRelWidth = THIN_RELSIZE,
        )
    }

    companion object {
        private val diagonalWallSizeX = THIN_RELSIZE * WORLD_BRICK_SIZE / sqrt(2.0f)
        private val shortFaceRelSize = THIN_RELSIZE * sqrt(2.0f - sqrt(2.0f))
    }
}
