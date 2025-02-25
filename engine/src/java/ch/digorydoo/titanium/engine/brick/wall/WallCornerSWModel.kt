package ch.digorydoo.titanium.engine.brick.wall

import ch.digorydoo.kutils.math.lerp
import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.titanium.engine.brick.AbstrBrickModel
import ch.digorydoo.titanium.engine.brick.BrickSubvolume
import ch.digorydoo.titanium.engine.brick.Tesselator
import ch.digorydoo.titanium.engine.utils.Direction

class WallCornerSWModel(private val relInset: Float, private val ceiling: Boolean): AbstrBrickModel() {
    private val upperNWPt = MutablePoint3f()
    private val upperSEPt = MutablePoint3f()
    private val ceilNorthMidPt = MutablePoint3f()
    private val ceilEastMidPt = MutablePoint3f()
    private val ceilWestOuterPt = MutablePoint3f()
    private val ceilCentrePt = MutablePoint3f()
    private val ceilNEOuterPt = MutablePoint3f()
    private val lowerNorthMidPt = MutablePoint3f()
    private val lowerEastMidPt = MutablePoint3f()
    private val lowerWestOuterPt = MutablePoint3f()
    private val lowerCentrePt = MutablePoint3f()

    override fun prepare(ix: Int, iy: Int, iz: Int, subvolume: BrickSubvolume) {
        super.prepare(ix, iy, iz, subvolume)

        val midX = lerp(upside.pt3.x, upside.pt1.x, relInset)
        val midY = lerp(upside.pt1.y, upside.pt0.y, relInset)
        val midZ = upside.pt0.z - (if (ceiling) 1.0f - relInset else 0.0f)

        upperNWPt.set(upside.pt2.x, midY, upside.pt2.z)
        upperSEPt.set(midX, upside.pt1.y, upside.pt1.z)

        ceilNorthMidPt.set(upside.pt2.x, midY, midZ)
        lowerNorthMidPt.set(downside.pt2.x, midY, downside.pt2.z)

        ceilEastMidPt.set(midX, upside.pt1.y, midZ)
        lowerEastMidPt.set(midX, downside.pt1.y, downside.pt1.z)

        ceilWestOuterPt.set(midX, upside.pt0.y, midZ)
        lowerWestOuterPt.set(midX, downside.pt0.y, downside.pt0.z)

        ceilCentrePt.set(midX, midY, midZ)
        lowerCentrePt.set(midX, midY, downside.pt0.z)

        ceilNEOuterPt.set(upside.pt3.x, upside.pt3.y, midZ)
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
                ceilWestOuterPt,
                ceilCentrePt,
                upside.pt2,
                ceilNorthMidPt,
                upFaceIdx,
                Direction.upVector,
                texRelWidth = 1.0f - relInset,
                texRelHeight = relInset,
            )
            tess.addQuad(
                upside.pt0,
                upside.pt1,
                ceilWestOuterPt,
                ceilEastMidPt,
                upFaceIdx,
                Direction.upVector,
                texRelY = relInset,
                texRelHeight = 1.0f - relInset,
            )
        }
    }

    override fun tesselateDownFace(tess: Tesselator) {
        if (!isBrickBelowFullyCoveringMyDownFace()) {
            tess.addQuad(
                lowerCentrePt,
                lowerWestOuterPt,
                lowerNorthMidPt,
                downside.pt2,
                downFaceIdx,
                Direction.downVector,
                texRelX = relInset,
                texRelWidth = 1.0f - relInset,
                texRelHeight = relInset,
            )
            tess.addQuad(
                downside.pt1,
                downside.pt0,
                lowerEastMidPt,
                lowerWestOuterPt,
                downFaceIdx,
                Direction.downVector,
                texRelY = relInset,
                texRelHeight = 1.0f - relInset,
            )
        }

        if (ceiling) {
            tess.addQuad(
                ceilEastMidPt,
                ceilCentrePt,
                ceilNEOuterPt,
                ceilNorthMidPt,
                downFaceIdx,
                Direction.downVector,
                texRelX = 0.0f,
                texRelWidth = relInset,
                texRelY = 0.0f,
                texRelHeight = relInset,
            )
        }
    }

    override fun tesselateNorthFace(tess: Tesselator) {
        if (!isBrickToNorthFullyCoveringMyNorthFace()) {
            tess.addQuad(
                lowerNorthMidPt,
                downside.pt2,
                upperNWPt,
                upside.pt2,
                northFaceIdx,
                Direction.northVector,
                texRelX = relInset,
                texRelWidth = 1.0f - relInset,
            )

            if (ceiling) {
                tess.addQuad(
                    ceilNEOuterPt,
                    ceilNorthMidPt,
                    upside.pt3,
                    upperNWPt,
                    northFaceIdx,
                    Direction.northVector,
                    texRelX = 0.0f,
                    texRelWidth = relInset,
                    texRelY = 0.0f,
                    texRelHeight = 1.0f - relInset,
                )
            }
        }

        tess.addQuad(
            lowerEastMidPt,
            lowerCentrePt,
            ceilEastMidPt,
            ceilCentrePt,
            northFaceIdx,
            Direction.northVector,
            texRelX = 0.0f,
            texRelWidth = relInset,
        )
    }

    override fun tesselateSouthFace(tess: Tesselator) {
        if (isBrickToSouthFullyCoveringMySouthFace()) {
            return
        }

        tess.addQuad(
            downside.pt0,
            downside.pt1,
            upside.pt0,
            upside.pt1,
            southFaceIdx,
            Direction.southVector,
        )
    }

    override fun tesselateEastFace(tess: Tesselator) {
        if (!isBrickToEastFullyCoveringMyEastFace()) {
            tess.addQuad(
                downside.pt1,
                lowerEastMidPt,
                upside.pt1,
                upperSEPt,
                eastFaceIdx,
                Direction.eastVector,
                texRelX = 0.0f,
                texRelWidth = 1.0f - relInset,
            )

            if (ceiling) {
                tess.addQuad(
                    ceilEastMidPt,
                    ceilNEOuterPt,
                    upperSEPt,
                    upside.pt3,
                    eastFaceIdx,
                    Direction.eastVector,
                    texRelX = 1.0f - relInset,
                    texRelWidth = relInset,
                    texRelY = 0.0f,
                    texRelHeight = 1.0f - relInset,
                )
            }
        }

        tess.addQuad(
            lowerCentrePt,
            lowerNorthMidPt,
            ceilCentrePt,
            ceilNorthMidPt,
            eastFaceIdx,
            Direction.eastVector,
            texRelX = 1.0f - relInset,
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
