package ch.digorydoo.titanium.engine.brick.wall

import ch.digorydoo.kutils.math.lerp
import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.titanium.engine.brick.AbstrBrickModel
import ch.digorydoo.titanium.engine.brick.BrickSubvolume
import ch.digorydoo.titanium.engine.brick.Tesselator
import ch.digorydoo.titanium.engine.utils.Direction

class WallCornerNWModel(private val relInset: Float, private val ceiling: Boolean): AbstrBrickModel() {
    private val upperNEPt = MutablePoint3f()
    private val upperSWPt = MutablePoint3f()
    private val ceilSouthMidPt = MutablePoint3f()
    private val ceilEastMidPt = MutablePoint3f()
    private val ceilWestOuterPt = MutablePoint3f()
    private val ceilCentrePt = MutablePoint3f()
    private val ceilSEOuterPt = MutablePoint3f()
    private val lowerSouthMidPt = MutablePoint3f()
    private val lowerEastMidPt = MutablePoint3f()
    private val lowerWestOuterPt = MutablePoint3f()
    private val lowerCentrePt = MutablePoint3f()

    override fun prepare(ix: Int, iy: Int, iz: Int, subvolume: BrickSubvolume) {
        super.prepare(ix, iy, iz, subvolume)

        val midX = lerp(upside.pt1.x, upside.pt3.x, relInset)
        val midY = lerp(upside.pt1.y, upside.pt0.y, relInset)
        val midZ = upside.pt0.z - (if (ceiling) 1.0f - relInset else 0.0f)

        upperNEPt.set(midX, upside.pt3.y, upside.pt3.z)
        upperSWPt.set(upside.pt0.x, midY, upside.pt0.z)

        ceilSouthMidPt.set(upside.pt0.x, midY, midZ)
        lowerSouthMidPt.set(downside.pt0.x, midY, downside.pt0.z)

        ceilEastMidPt.set(midX, upside.pt1.y, midZ)
        lowerEastMidPt.set(midX, downside.pt1.y, downside.pt1.z)

        ceilWestOuterPt.set(midX, upside.pt0.y, midZ)
        lowerWestOuterPt.set(midX, downside.pt0.y, downside.pt0.z)

        ceilCentrePt.set(midX, midY, midZ)
        lowerCentrePt.set(midX, midY, downside.pt0.z)

        ceilSEOuterPt.set(upside.pt1.x, upside.pt1.y, midZ)
    }

    override fun tesselateUpFace(tess: Tesselator) {
        if (isBrickAboveFullyCoveringMyUpFace()) {
            return
        }

        if (ceiling) {
            tess.addQuad(
                upside.pt0,
                upside.pt1,
                upside.pt2,
                upside.pt3,
                upFaceIdx,
                Direction.upVector,
            )
        } else {
            tess.addQuad(
                upside.pt0,
                ceilSouthMidPt,
                ceilWestOuterPt,
                ceilCentrePt,
                upFaceIdx,
                Direction.upVector,
                texRelX = 0.0f,
                texRelWidth = 1.0f - relInset,
                texRelY = 1.0f - relInset,
                texRelHeight = relInset,
            )
            tess.addQuad(
                ceilWestOuterPt,
                ceilEastMidPt,
                upside.pt2,
                upside.pt3,
                upFaceIdx,
                Direction.upVector,
                texRelHeight = 1.0f - relInset,
            )
        }
    }

    override fun tesselateDownFace(tess: Tesselator) {
        if (!isBrickBelowFullyCoveringMyDownFace()) {
            tess.addQuad(
                lowerSouthMidPt,
                downside.pt0,
                lowerCentrePt,
                lowerWestOuterPt,
                downFaceIdx,
                Direction.downVector,
                texRelX = relInset,
                texRelWidth = 1.0f - relInset,
                texRelY = 1.0f - relInset,
                texRelHeight = relInset,
            )
            tess.addQuad(
                lowerEastMidPt,
                lowerWestOuterPt,
                downside.pt3,
                downside.pt2,
                downFaceIdx,
                Direction.downVector,
                texRelHeight = 1.0f - relInset,
            )
        }

        if (ceiling) {
            tess.addQuad(
                ceilSEOuterPt,
                ceilSouthMidPt,
                ceilEastMidPt,
                ceilCentrePt,
                downFaceIdx,
                Direction.downVector,
                texRelX = 0.0f,
                texRelWidth = relInset,
                texRelY = 1.0f - relInset,
                texRelHeight = relInset,
            )
        }
    }

    override fun tesselateNorthFace(tess: Tesselator) {
        if (isBrickToNorthFullyCoveringMyNorthFace()) {
            return
        }

        tess.addQuad(
            downside.pt3,
            downside.pt2,
            upside.pt3,
            upside.pt2,
            northFaceIdx,
            Direction.northVector,
        )
    }

    override fun tesselateSouthFace(tess: Tesselator) {
        if (!isBrickToSouthFullyCoveringMySouthFace()) {
            tess.addQuad(
                downside.pt0,
                lowerSouthMidPt,
                upside.pt0,
                upperSWPt,
                southFaceIdx,
                Direction.southVector,
                texRelX = 0.0f,
                texRelWidth = 1.0f - relInset,
            )

            if (ceiling) {
                tess.addQuad(
                    ceilSouthMidPt,
                    ceilSEOuterPt,
                    upperSWPt,
                    upside.pt1,
                    southFaceIdx,
                    Direction.southVector,
                    texRelX = 1.0f - relInset,
                    texRelWidth = relInset,
                    texRelY = 0.0f,
                    texRelHeight = 1.0f - relInset,
                )
            }
        }

        tess.addQuad(
            lowerCentrePt,
            lowerEastMidPt,
            ceilCentrePt,
            ceilEastMidPt,
            southFaceIdx,
            Direction.southVector,
            texRelX = 1.0f - relInset,
            texRelWidth = relInset,
        )
    }

    override fun tesselateEastFace(tess: Tesselator) {
        if (!isBrickToEastFullyCoveringMyEastFace()) {
            tess.addQuad(
                lowerEastMidPt,
                downside.pt3,
                upperNEPt,
                upside.pt3,
                eastFaceIdx,
                Direction.eastVector,
                texRelX = relInset,
                texRelWidth = 1.0f - relInset,
            )

            if (ceiling) {
                tess.addQuad(
                    ceilSEOuterPt,
                    ceilEastMidPt,
                    upside.pt1,
                    upperNEPt,
                    eastFaceIdx,
                    Direction.eastVector,
                    texRelX = 0.0f,
                    texRelWidth = relInset,
                    texRelY = 0.0f,
                    texRelHeight = 1.0f - relInset,
                )
            }
        }

        tess.addQuad(
            lowerSouthMidPt,
            lowerCentrePt,
            ceilSouthMidPt,
            ceilCentrePt,
            eastFaceIdx,
            Direction.eastVector,
            texRelX = 0.0f,
            texRelWidth = relInset,
        )
    }

    override fun tesselateWestFace(tess: Tesselator) {
        if (isBrickToWestFullyCoveringMyWestFace()) {
            return
        }

        tess.addQuad(
            downside.pt2,
            downside.pt0,
            upside.pt2,
            upside.pt0,
            westFaceIdx,
            Direction.westVector,
        )
    }

    override fun heightAt(x: Float, y: Float) = upside.pt0.z // TODO
}
