package ch.digorydoo.titanium.engine.brick.zz_various

import ch.digorydoo.kutils.math.lerp
import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.titanium.engine.brick.AbstrBrickModel
import ch.digorydoo.titanium.engine.brick.Brick.Companion.WORLD_BRICK_SIZE
import ch.digorydoo.titanium.engine.brick.BrickSubvolume
import ch.digorydoo.titanium.engine.brick.Tesselator
import ch.digorydoo.titanium.engine.utils.Direction
import kotlin.math.abs

class BlockModel(
    private val relInsetNorthFace: Float = 0.0f,
    private val relInsetEastFace: Float = 0.0f,
    private val relInsetSouthFace: Float = 0.0f,
    private val relInsetWestFace: Float = 0.0f,
    private val relInsetUpFace: Float = 0.0f,
    private val relInsetDownFace: Float = 0.0f,
    private val upFaceNSSlope: Float = 0.0f,
    private val upFaceWESlope: Float = 0.0f,
): AbstrBrickModel() {
    private val upperPt0 = MutablePoint3f()
    private val upperPt1 = MutablePoint3f()
    private val upperPt2 = MutablePoint3f()
    private val upperPt3 = MutablePoint3f()
    private val midPt0 = MutablePoint3f()
    private val midPt1 = MutablePoint3f()
    private val midPt2 = MutablePoint3f()
    private val midPt3 = MutablePoint3f()
    private val lowerPt0 = MutablePoint3f()
    private val lowerPt1 = MutablePoint3f()
    private val lowerPt2 = MutablePoint3f()
    private val lowerPt3 = MutablePoint3f()

    override fun prepare(ix: Int, iy: Int, iz: Int, subvolume: BrickSubvolume) {
        super.prepare(ix, iy, iz, subvolume)

        val northX = lerp(upside.pt2.x, upside.pt0.x, relInsetNorthFace)
        val southX = lerp(upside.pt0.x, upside.pt2.x, relInsetSouthFace)
        val eastY = lerp(upside.pt1.y, upside.pt0.y, relInsetEastFace)
        val westY = lerp(upside.pt0.y, upside.pt1.y, relInsetWestFace)
        val upZ = lerp(upside.pt0.z, downside.pt0.z, relInsetUpFace)
        val dnZ = lerp(downside.pt0.z, upside.pt0.z, relInsetDownFace)

        val nsSlope = upFaceNSSlope * WORLD_BRICK_SIZE
        val weSlope = upFaceWESlope * WORLD_BRICK_SIZE

        upperPt0.set(southX, westY, upZ - nsSlope + weSlope)
        upperPt1.set(southX, eastY, upZ - nsSlope - weSlope)
        upperPt2.set(northX, westY, upZ + nsSlope + weSlope)
        upperPt3.set(northX, eastY, upZ + nsSlope - weSlope)

        if (nsSlope != 0.0f || weSlope != 0.0f) {
            val ansSlope = abs(nsSlope)
            val aweSlope = abs(weSlope)

            midPt0.set(southX, westY, upZ - ansSlope - aweSlope)
            midPt1.set(southX, eastY, upZ - ansSlope - aweSlope)
            midPt2.set(northX, westY, upZ - ansSlope - aweSlope)
            midPt3.set(northX, eastY, upZ - ansSlope - aweSlope)
        }

        lowerPt0.set(southX, westY, dnZ)
        lowerPt1.set(southX, eastY, dnZ)
        lowerPt2.set(northX, westY, dnZ)
        lowerPt3.set(northX, eastY, dnZ)
    }

    override fun heightAt(x: Float, y: Float) = when {
        // FIXME incorrect when top is sloped
        relInsetNorthFace > 0 && x < upperPt2.x -> null
        relInsetSouthFace > 0 && x > upperPt0.x -> null
        relInsetWestFace > 0 && y < upperPt0.y -> null
        relInsetEastFace > 0 && y > upperPt1.y -> null
        else -> upperPt0.z
    }

    override fun tesselateUpFace(tess: Tesselator) {
        if (relInsetUpFace <= 0.0f && isBrickAboveFullyCoveringMyUpFace()) {
            return
        }

        tess.addQuad(
            upperPt0,
            upperPt1,
            upperPt2,
            upperPt3,
            upFaceIdx,
            Direction.upVector,
            texRelX = relInsetWestFace,
            texRelWidth = 1.0f - relInsetWestFace - relInsetEastFace,
            texRelY = relInsetNorthFace,
            texRelHeight = 1.0f - relInsetNorthFace - relInsetSouthFace,
        )
    }

    override fun tesselateDownFace(tess: Tesselator) {
        if (relInsetDownFace <= 0.0f && isBrickBelowFullyCoveringMyDownFace()) {
            return
        }

        tess.addQuad(
            lowerPt1,
            lowerPt0,
            lowerPt3,
            lowerPt2,
            downFaceIdx,
            Direction.downVector,
            texRelX = relInsetEastFace,
            texRelWidth = 1.0f - relInsetWestFace - relInsetEastFace,
            texRelY = relInsetNorthFace,
            texRelHeight = 1.0f - relInsetNorthFace - relInsetSouthFace,
        )
    }

    override fun tesselateNorthFace(tess: Tesselator) {
        if (relInsetNorthFace <= 0.0f && isBrickToNorthFullyCoveringMyNorthFace()) {
            return
        }

        val texRelWidth = 1.0f - relInsetWestFace - relInsetEastFace
        val texRelHeight = 1.0f - relInsetUpFace - relInsetDownFace

        if (upFaceWESlope > 0.0f) {
            tess.addTriangle(
                upperPt2,
                midPt3,
                lowerPt3,
                northFaceIdx,
                Direction.northVector,
                texPivotX = relInsetUpFace - upFaceWESlope,
                pivotIsBrickRelative = true,
                texRelX = relInsetUpFace + upFaceWESlope,
                texRelWidth = texRelHeight - upFaceWESlope,
                texRelY = relInsetWestFace,
                texRelHeight = texRelWidth,
                rotateTex = true,
            )
            tess.addTriangle(
                upperPt2,
                lowerPt3,
                lowerPt2,
                northFaceIdx,
                Direction.northVector,
                texPivotX = 1.0f,
                texRelX = relInsetWestFace,
                texRelWidth = texRelWidth,
                texRelY = relInsetUpFace - upFaceWESlope,
                texRelHeight = texRelHeight + upFaceWESlope,
            )
        } else if (upFaceWESlope < 0.0f) {
            tess.addTriangle(
                upperPt3,
                lowerPt2,
                midPt2,
                northFaceIdx,
                Direction.northVector,
                texPivotX = relInsetUpFace + upFaceWESlope,
                pivotIsBrickRelative = true,
                texRelX = relInsetUpFace - upFaceWESlope,
                texRelWidth = texRelHeight + upFaceWESlope,
                texRelY = relInsetWestFace,
                texRelHeight = texRelWidth,
                rotateTex = true,
                flipTexX = true,
                flipTexY = true,
            )
            tess.addTriangle(
                upperPt3,
                lowerPt3,
                lowerPt2,
                northFaceIdx,
                Direction.northVector,
                texPivotX = 0.0f,
                texRelX = relInsetWestFace,
                texRelWidth = texRelWidth,
                texRelY = relInsetUpFace + upFaceWESlope,
                texRelHeight = texRelHeight - upFaceWESlope,
            )
        } else {
            tess.addQuad(
                lowerPt3,
                lowerPt2,
                upperPt3,
                upperPt2,
                northFaceIdx,
                Direction.northVector,
                texRelX = relInsetEastFace,
                texRelWidth = texRelWidth,
                texRelY = relInsetUpFace,
                texRelHeight = texRelHeight,
            )
        }
    }

    override fun tesselateSouthFace(tess: Tesselator) {
        if (relInsetSouthFace <= 0.0f && isBrickToSouthFullyCoveringMySouthFace()) {
            return
        }

        val texRelWidth = 1.0f - relInsetWestFace - relInsetEastFace
        val texRelHeight = 1.0f - relInsetUpFace - relInsetDownFace

        if (upFaceWESlope < 0.0f) {
            tess.addTriangle(
                upperPt1,
                midPt0,
                lowerPt0,
                southFaceIdx,
                Direction.southVector,
                texPivotX = relInsetUpFace + upFaceWESlope,
                pivotIsBrickRelative = true,
                texRelX = relInsetUpFace - upFaceWESlope,
                texRelWidth = texRelHeight + upFaceWESlope,
                texRelY = relInsetWestFace,
                texRelHeight = texRelWidth,
                rotateTex = true,
            )
            tess.addTriangle(
                upperPt1,
                lowerPt0,
                lowerPt1,
                southFaceIdx,
                Direction.southVector,
                texPivotX = 1.0f,
                texRelX = relInsetWestFace,
                texRelWidth = texRelWidth,
                texRelY = relInsetUpFace + upFaceWESlope,
                texRelHeight = texRelHeight - upFaceWESlope,
            )
        } else if (upFaceWESlope > 0.0f) {
            tess.addTriangle(
                upperPt0,
                lowerPt1,
                midPt1,
                southFaceIdx,
                Direction.southVector,
                texPivotX = relInsetUpFace - upFaceWESlope,
                pivotIsBrickRelative = true,
                texRelX = relInsetUpFace + upFaceWESlope,
                texRelWidth = texRelHeight - upFaceWESlope,
                texRelY = relInsetWestFace,
                texRelHeight = texRelWidth,
                rotateTex = true,
                flipTexX = true,
                flipTexY = true,
            )
            tess.addTriangle(
                upperPt0,
                lowerPt0,
                lowerPt1,
                southFaceIdx,
                Direction.southVector,
                texPivotX = 0.0f,
                texRelX = relInsetWestFace,
                texRelWidth = texRelWidth,
                texRelY = relInsetUpFace - upFaceWESlope,
                texRelHeight = texRelHeight + upFaceWESlope,
            )
        } else {
            tess.addQuad(
                lowerPt0,
                lowerPt1,
                upperPt0,
                upperPt1,
                southFaceIdx,
                Direction.southVector,
                texRelX = relInsetWestFace,
                texRelWidth = texRelWidth,
                texRelY = relInsetUpFace,
                texRelHeight = texRelHeight,
            )
        }
    }

    override fun tesselateEastFace(tess: Tesselator) {
        if (relInsetEastFace <= 0.0f && isBrickToEastFullyCoveringMyEastFace()) {
            return
        }

        val texRelWidth = 1.0f - relInsetNorthFace - relInsetSouthFace
        val texRelHeight = 1.0f - relInsetUpFace - relInsetDownFace

        if (upFaceNSSlope > 0.0f) {
            tess.addTriangle(
                upperPt3,
                midPt1,
                lowerPt1,
                eastFaceIdx,
                Direction.eastVector,
                texPivotX = relInsetUpFace - upFaceNSSlope,
                pivotIsBrickRelative = true,
                texRelX = relInsetUpFace + upFaceNSSlope,
                texRelWidth = texRelHeight - upFaceNSSlope,
                texRelY = relInsetSouthFace,
                texRelHeight = texRelWidth,
                rotateTex = true,
            )
            tess.addTriangle(
                upperPt3,
                lowerPt1,
                lowerPt3,
                eastFaceIdx,
                Direction.eastVector,
                texPivotX = 1.0f,
                texRelX = relInsetSouthFace,
                texRelWidth = texRelWidth,
                texRelY = relInsetUpFace - upFaceNSSlope,
                texRelHeight = texRelHeight + upFaceNSSlope,
            )
        } else if (upFaceNSSlope < 0.0f) {
            tess.addTriangle(
                upperPt1,
                lowerPt3,
                midPt3,
                eastFaceIdx,
                Direction.eastVector,
                texPivotX = relInsetUpFace + upFaceNSSlope,
                pivotIsBrickRelative = true,
                texRelX = relInsetUpFace - upFaceNSSlope,
                texRelWidth = texRelHeight + upFaceNSSlope,
                texRelY = relInsetSouthFace,
                texRelHeight = texRelWidth,
                rotateTex = true,
                flipTexX = true,
                flipTexY = true,
            )
            tess.addTriangle(
                upperPt1,
                lowerPt1,
                lowerPt3,
                eastFaceIdx,
                Direction.eastVector,
                texPivotX = 0.0f,
                texRelX = relInsetSouthFace,
                texRelWidth = texRelWidth,
                texRelY = relInsetUpFace + upFaceNSSlope,
                texRelHeight = texRelHeight - upFaceNSSlope,
            )
        } else {
            tess.addQuad(
                lowerPt1,
                lowerPt3,
                upperPt1,
                upperPt3,
                eastFaceIdx,
                Direction.eastVector,
                texRelX = relInsetSouthFace,
                texRelWidth = texRelWidth,
                texRelY = relInsetUpFace,
                texRelHeight = texRelHeight,
            )
        }
    }

    override fun tesselateWestFace(tess: Tesselator) {
        if (relInsetWestFace <= 0.0f && isBrickToWestFullyCoveringMyWestFace()) {
            return
        }

        val texRelWidth = 1.0f - relInsetNorthFace - relInsetSouthFace
        val texRelHeight = 1.0f - relInsetUpFace - relInsetDownFace

        if (upFaceNSSlope < 0.0f) {
            tess.addTriangle(
                upperPt0,
                midPt2,
                lowerPt2,
                westFaceIdx,
                Direction.westVector,
                texPivotX = relInsetUpFace + upFaceNSSlope,
                pivotIsBrickRelative = true,
                texRelX = relInsetUpFace - upFaceNSSlope,
                texRelWidth = texRelHeight + upFaceNSSlope,
                texRelY = relInsetSouthFace,
                texRelHeight = texRelWidth,
                rotateTex = true,
            )
            tess.addTriangle(
                upperPt0,
                lowerPt2,
                lowerPt0,
                westFaceIdx,
                Direction.westVector,
                texPivotX = 1.0f,
                texRelX = relInsetSouthFace,
                texRelWidth = texRelWidth,
                texRelY = relInsetUpFace + upFaceNSSlope,
                texRelHeight = texRelHeight - upFaceNSSlope,
            )
        } else if (upFaceNSSlope > 0.0f) {
            tess.addTriangle(
                upperPt2,
                lowerPt0,
                midPt0,
                westFaceIdx,
                Direction.westVector,
                texPivotX = relInsetUpFace - upFaceNSSlope,
                pivotIsBrickRelative = true,
                texRelX = relInsetUpFace + upFaceNSSlope,
                texRelWidth = texRelHeight - upFaceNSSlope,
                texRelY = relInsetSouthFace,
                texRelHeight = texRelWidth,
                rotateTex = true,
                flipTexX = true,
                flipTexY = true,
            )
            tess.addTriangle(
                upperPt2,
                lowerPt2,
                lowerPt0,
                westFaceIdx,
                Direction.westVector,
                texPivotX = 0.0f,
                texRelX = relInsetSouthFace,
                texRelWidth = texRelWidth,
                texRelY = relInsetUpFace - upFaceNSSlope,
                texRelHeight = texRelHeight + upFaceNSSlope,
            )
        } else {
            tess.addQuad(
                lowerPt2,
                lowerPt0,
                upperPt2,
                upperPt0,
                westFaceIdx,
                Direction.westVector,
                texRelX = relInsetNorthFace,
                texRelWidth = texRelWidth,
                texRelY = relInsetUpFace,
                texRelHeight = texRelHeight,
            )
        }
    }
}
