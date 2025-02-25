package ch.digorydoo.titanium.engine.brick.wall

import ch.digorydoo.kutils.math.lerp
import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.titanium.engine.brick.AbstrBrickModel
import ch.digorydoo.titanium.engine.brick.BrickSubvolume
import ch.digorydoo.titanium.engine.brick.Tesselator
import ch.digorydoo.titanium.engine.utils.Direction

class WallCornerSEModel(private val relInset: Float, private val ceiling: Boolean): AbstrBrickModel() {
    private val upperNEPt = MutablePoint3f()
    private val upperSWPt = MutablePoint3f()
    private val ceilNorthMidPt = MutablePoint3f()
    private val ceilWestMidPt = MutablePoint3f()
    private val ceilEastOuterPt = MutablePoint3f()
    private val ceilCentrePt = MutablePoint3f()
    private val ceilNWOuterPt = MutablePoint3f()
    private val lowerNorthMidPt = MutablePoint3f()
    private val lowerWestMidPt = MutablePoint3f()
    private val lowerEastOuterPt = MutablePoint3f()
    private val lowerCentrePt = MutablePoint3f()

    override fun prepare(ix: Int, iy: Int, iz: Int, subvolume: BrickSubvolume) {
        super.prepare(ix, iy, iz, subvolume)

        val midX = lerp(upside.pt2.x, upside.pt0.x, relInset)
        val midY = lerp(upside.pt2.y, upside.pt3.y, relInset)
        val midZ = upside.pt0.z - (if (ceiling) 1.0f - relInset else 0.0f)

        upperNEPt.set(upside.pt3.x, midY, upside.pt3.z)
        upperSWPt.set(midX, upside.pt0.y, upside.pt0.z)

        ceilNorthMidPt.set(upside.pt3.x, midY, midZ)
        lowerNorthMidPt.set(downside.pt3.x, midY, downside.pt3.z)

        ceilEastOuterPt.set(midX, upside.pt1.y, midZ)
        lowerEastOuterPt.set(midX, downside.pt1.y, downside.pt1.z)

        ceilWestMidPt.set(midX, upside.pt0.y, midZ)
        lowerWestMidPt.set(midX, downside.pt0.y, downside.pt0.z)

        ceilCentrePt.set(midX, midY, midZ)
        lowerCentrePt.set(midX, midY, downside.pt0.z)

        ceilNWOuterPt.set(upside.pt2.x, upside.pt2.y, midZ)
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
                ceilCentrePt,
                ceilEastOuterPt,
                ceilNorthMidPt,
                upside.pt3,
                upFaceIdx,
                Direction.upVector,
                texRelX = relInset,
                texRelWidth = 1.0f - relInset,
                texRelHeight = relInset,
            )
            tess.addQuad(
                upside.pt0,
                upside.pt1,
                ceilWestMidPt,
                ceilEastOuterPt,
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
                lowerEastOuterPt,
                lowerCentrePt,
                downside.pt3,
                lowerNorthMidPt,
                downFaceIdx,
                Direction.downVector,
                texRelX = 0.0f,
                texRelWidth = 1.0f - relInset,
                texRelHeight = relInset,
            )
            tess.addQuad(
                downside.pt1,
                downside.pt0,
                lowerEastOuterPt,
                lowerWestMidPt,
                downFaceIdx,
                Direction.downVector,
                texRelY = relInset,
                texRelHeight = 1.0f - relInset,
            )
        }

        if (ceiling) {
            tess.addQuad(
                ceilCentrePt,
                ceilWestMidPt,
                ceilNorthMidPt,
                ceilNWOuterPt,
                downFaceIdx,
                Direction.downVector,
                texRelX = 1.0f - relInset,
                texRelWidth = relInset,
                texRelY = 0.0f,
                texRelHeight = relInset,
            )
        }
    }

    override fun tesselateNorthFace(tess: Tesselator) {
        if (!isBrickToNorthFullyCoveringMyNorthFace()) {
            tess.addQuad(
                downside.pt3,
                lowerNorthMidPt,
                upside.pt3,
                upperNEPt,
                northFaceIdx,
                Direction.northVector,
                texRelX = 0.0f,
                texRelWidth = 1.0f - relInset,
            )

            if (ceiling) {
                tess.addQuad(
                    ceilNorthMidPt,
                    ceilNWOuterPt,
                    upperNEPt,
                    upside.pt2,
                    northFaceIdx,
                    Direction.northVector,
                    texRelX = 1.0f - relInset,
                    texRelWidth = relInset,
                    texRelY = 0.0f,
                    texRelHeight = 1.0f - relInset,
                )
            }
        }

        tess.addQuad(
            lowerCentrePt,
            lowerWestMidPt,
            ceilCentrePt,
            ceilWestMidPt,
            northFaceIdx,
            Direction.northVector,
            texRelX = 1.0f - relInset,
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
                lowerWestMidPt,
                downside.pt0,
                upperSWPt,
                upside.pt0,
                westFaceIdx,
                Direction.westVector,
                texRelX = relInset,
                texRelWidth = 1.0f - relInset,
            )

            if (ceiling) {
                tess.addQuad(
                    ceilNWOuterPt,
                    ceilWestMidPt,
                    upside.pt2,
                    upperSWPt,
                    westFaceIdx,
                    Direction.westVector,
                    texRelX = 0.0f,
                    texRelWidth = relInset,
                    texRelY = 0.0f,
                    texRelHeight = 1.0f - relInset,
                )
            }
        }

        tess.addQuad(
            lowerNorthMidPt,
            lowerCentrePt,
            ceilNorthMidPt,
            ceilCentrePt,
            westFaceIdx,
            Direction.westVector,
            texRelX = 0.0f,
            texRelWidth = relInset,
        )
    }

    override fun heightAt(x: Float, y: Float) = upside.pt0.z // TODO
}
