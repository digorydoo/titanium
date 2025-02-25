package ch.digorydoo.titanium.engine.brick.wall

import ch.digorydoo.kutils.math.lerp
import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.titanium.engine.brick.AbstrBrickModel
import ch.digorydoo.titanium.engine.brick.BrickSubvolume
import ch.digorydoo.titanium.engine.brick.Tesselator
import ch.digorydoo.titanium.engine.utils.Direction

class WallCornerNEModel(private val relInset: Float, private val ceiling: Boolean): AbstrBrickModel() {
    private val upperNWPt = MutablePoint3f()
    private val upperSEPt = MutablePoint3f()
    private val ceilSouthMidPt = MutablePoint3f()
    private val ceilWestMidPt = MutablePoint3f()
    private val ceilEastOuterPt = MutablePoint3f()
    private val ceilCentrePt = MutablePoint3f()
    private val ceilSWOuterPt = MutablePoint3f()
    private val lowerSouthMidPt = MutablePoint3f()
    private val lowerWestMidPt = MutablePoint3f()
    private val lowerEastOuterPt = MutablePoint3f()
    private val lowerCentrePt = MutablePoint3f()

    override fun prepare(ix: Int, iy: Int, iz: Int, subvolume: BrickSubvolume) {
        super.prepare(ix, iy, iz, subvolume)

        val midX = lerp(upside.pt0.x, upside.pt2.x, relInset)
        val midY = lerp(upside.pt0.y, upside.pt1.y, relInset)
        val midZ = upside.pt0.z - (if (ceiling) 1.0f - relInset else 0.0f)

        upperNWPt.set(midX, upside.pt2.y, upside.pt2.z)
        upperSEPt.set(upside.pt1.x, midY, upside.pt1.z)

        ceilSouthMidPt.set(upside.pt0.x, midY, midZ)
        lowerSouthMidPt.set(downside.pt0.x, midY, downside.pt0.z)

        ceilEastOuterPt.set(midX, upside.pt1.y, midZ)
        lowerEastOuterPt.set(midX, downside.pt1.y, downside.pt1.z)

        ceilWestMidPt.set(midX, upside.pt0.y, midZ)
        lowerWestMidPt.set(midX, downside.pt0.y, downside.pt0.z)

        ceilCentrePt.set(midX, midY, midZ)
        lowerCentrePt.set(midX, midY, downside.pt0.z)

        ceilSWOuterPt.set(upside.pt0.x, upside.pt0.y, midZ)
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
                ceilWestMidPt,
                ceilEastOuterPt,
                upside.pt2,
                upside.pt3,
                upFaceIdx,
                Direction.upVector,
                texRelHeight = 1.0f - relInset,
            )
            tess.addQuad(
                ceilSouthMidPt,
                upside.pt1,
                ceilCentrePt,
                ceilEastOuterPt,
                upFaceIdx,
                Direction.upVector,
                texRelX = relInset,
                texRelWidth = 1.0f - relInset,
                texRelY = 1.0f - relInset,
                texRelHeight = relInset,
            )
        }
    }

    override fun tesselateDownFace(tess: Tesselator) {
        if (!isBrickBelowFullyCoveringMyDownFace()) {
            tess.addQuad(
                lowerEastOuterPt,
                lowerWestMidPt,
                downside.pt3,
                downside.pt2,
                downFaceIdx,
                Direction.downVector,
                texRelHeight = 1.0f - relInset,
            )
            tess.addQuad(
                downside.pt1,
                lowerSouthMidPt,
                lowerEastOuterPt,
                lowerCentrePt,
                downFaceIdx,
                Direction.downVector,
                texRelX = 0.0f,
                texRelWidth = 1.0f - relInset,
                texRelY = 1.0f - relInset,
                texRelHeight = relInset,
            )
        }

        if (ceiling) {
            tess.addQuad(
                ceilSouthMidPt,
                ceilSWOuterPt,
                ceilCentrePt,
                ceilWestMidPt,
                downFaceIdx,
                Direction.downVector,
                texRelX = 1.0f - relInset,
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
                lowerSouthMidPt,
                downside.pt1,
                upperSEPt,
                upside.pt1,
                southFaceIdx,
                Direction.southVector,
                texRelX = relInset,
                texRelWidth = 1.0f - relInset,
            )

            if (ceiling) {
                tess.addQuad(
                    ceilSWOuterPt,
                    ceilSouthMidPt,
                    upside.pt0,
                    upperSEPt,
                    southFaceIdx,
                    Direction.southVector,
                    texRelX = 0.0f,
                    texRelWidth = relInset,
                    texRelY = 0.0f,
                    texRelHeight = 1.0f - relInset,
                )
            }
        }

        tess.addQuad(
            lowerWestMidPt,
            lowerCentrePt,
            ceilWestMidPt,
            ceilCentrePt,
            southFaceIdx,
            Direction.southVector,
            texRelX = 0.0f,
            texRelWidth = relInset,
        )
    }

    override fun tesselateEastFace(tess: Tesselator) {
        if (isBrickToEastFullyCoveringMyEastFace()) {
            return
        }

        tess.addQuad(
            downside.pt1,
            downside.pt3,
            upside.pt1,
            upside.pt3,
            eastFaceIdx,
            Direction.eastVector,
        )
    }

    override fun tesselateWestFace(tess: Tesselator) {
        if (!isBrickToWestFullyCoveringMyWestFace()) {
            tess.addQuad(
                downside.pt2,
                lowerWestMidPt,
                upside.pt2,
                upperNWPt,
                westFaceIdx,
                Direction.westVector,
                texRelX = 0.0f,
                texRelWidth = 1.0f - relInset,
            )

            if (ceiling) {
                tess.addQuad(
                    ceilWestMidPt,
                    ceilSWOuterPt,
                    upperNWPt,
                    upside.pt0,
                    westFaceIdx,
                    Direction.westVector,
                    texRelX = 1.0f - relInset,
                    texRelWidth = relInset,
                    texRelY = 0.0f,
                    texRelHeight = 1.0f - relInset,
                )
            }
        }

        tess.addQuad(
            lowerCentrePt,
            lowerSouthMidPt,
            ceilCentrePt,
            ceilSouthMidPt,
            westFaceIdx,
            Direction.westVector,
            texRelX = 1.0f - relInset,
            texRelWidth = relInset,
        )
    }

    override fun heightAt(x: Float, y: Float) = upside.pt0.z // TODO
}
