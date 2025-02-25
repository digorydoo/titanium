package ch.digorydoo.titanium.engine.brick.zz_various

import ch.digorydoo.kutils.math.lerp
import ch.digorydoo.kutils.point.MutablePoint2f
import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.titanium.engine.brick.AbstrBrickModel
import ch.digorydoo.titanium.engine.brick.BrickSubvolume
import ch.digorydoo.titanium.engine.brick.Tesselator
import ch.digorydoo.titanium.engine.utils.Direction

class CutModel(
    private val cutSize: Float = 1.0f,
    private val cutNE: Boolean = false,
    private val cutNW: Boolean = false,
    private val cutSE: Boolean = false,
    private val cutSW: Boolean = false,
    private val relInsetUpFace: Float = 0.0f,
    private val relInsetDownFace: Float = 0.0f,
): AbstrBrickModel() {
    private val upperPt0 = MutablePoint3f()
    private val upperPt1 = MutablePoint3f()
    private val upperPt2 = MutablePoint3f()
    private val upperPt3 = MutablePoint3f()
    private val upperPt4 = MutablePoint3f()
    private val upperPt5 = MutablePoint3f()
    private val upperPt6 = MutablePoint3f()
    private val upperPt7 = MutablePoint3f()
    private val upperPt8 = MutablePoint3f()
    private val upperPt9 = MutablePoint3f()
    private val upperPt10 = MutablePoint3f()
    private val upperPt11 = MutablePoint3f()
    private val upperPtM = MutablePoint3f()

    private val lowerPt0 = MutablePoint3f()
    private val lowerPt1 = MutablePoint3f()
    private val lowerPt2 = MutablePoint3f()
    private val lowerPt3 = MutablePoint3f()
    private val lowerPt4 = MutablePoint3f()
    private val lowerPt5 = MutablePoint3f()
    private val lowerPt6 = MutablePoint3f()
    private val lowerPt7 = MutablePoint3f()
    private val lowerPt8 = MutablePoint3f()
    private val lowerPt9 = MutablePoint3f()
    private val lowerPt10 = MutablePoint3f()
    private val lowerPt11 = MutablePoint3f()
    private val lowerPtM = MutablePoint3f()

    private val texPt4 = MutablePoint2f()
    private val texPt5 = MutablePoint2f()
    private val texPt6 = MutablePoint2f()
    private val texPt7 = MutablePoint2f()
    private val texPt8 = MutablePoint2f()
    private val texPt9 = MutablePoint2f()
    private val texPt10 = MutablePoint2f()
    private val texPt11 = MutablePoint2f()
    private val texPtM = MutablePoint2f()

    override fun prepare(ix: Int, iy: Int, iz: Int, subvolume: BrickSubvolume) {
        super.prepare(ix, iy, iz, subvolume)

        val upZ = lerp(upside.pt0.z, downside.pt0.z, relInsetUpFace)
        val dnZ = lerp(downside.pt0.z, upside.pt0.z, relInsetDownFace)

        upperPt0.set(upside.pt0.x, upside.pt0.y, upZ)
        upperPt1.set(upside.pt1.x, upside.pt1.y, upZ)
        upperPt2.set(upside.pt2.x, upside.pt2.y, upZ)
        upperPt3.set(upside.pt3.x, upside.pt3.y, upZ)

        lowerPt0.set(downside.pt0.x, downside.pt0.y, dnZ)
        lowerPt1.set(downside.pt1.x, downside.pt1.y, dnZ)
        lowerPt2.set(downside.pt2.x, downside.pt2.y, dnZ)
        lowerPt3.set(downside.pt3.x, downside.pt3.y, dnZ)

        val relSW = if (cutSW) cutSize else 0.0f
        val relSE = if (cutSE) cutSize else 0.0f
        val relNW = if (cutNW) cutSize else 0.0f
        val relNE = if (cutNE) cutSize else 0.0f

        upperPt4.x = lerp(upside.pt0.x, upside.pt1.x, relSW)
        upperPt4.y = lerp(upside.pt0.y, upside.pt1.y, relSW)
        upperPt4.z = upZ

        upperPt5.x = lerp(upside.pt1.x, upside.pt0.x, relSE)
        upperPt5.y = lerp(upside.pt1.y, upside.pt0.y, relSE)
        upperPt5.z = upZ

        upperPt6.x = lerp(upside.pt1.x, upside.pt3.x, relSE)
        upperPt6.y = lerp(upside.pt1.y, upside.pt3.y, relSE)
        upperPt6.z = upZ

        upperPt7.x = lerp(upside.pt3.x, upside.pt1.x, relNE)
        upperPt7.y = lerp(upside.pt3.y, upside.pt1.y, relNE)
        upperPt7.z = upZ

        upperPt8.x = lerp(upside.pt3.x, upside.pt2.x, relNE)
        upperPt8.y = lerp(upside.pt3.y, upside.pt2.y, relNE)
        upperPt8.z = upZ

        upperPt9.x = lerp(upside.pt2.x, upside.pt3.x, relNW)
        upperPt9.y = lerp(upside.pt2.y, upside.pt3.y, relNW)
        upperPt9.z = upZ

        upperPt10.x = lerp(upside.pt2.x, upside.pt0.x, relNW)
        upperPt10.y = lerp(upside.pt2.y, upside.pt0.y, relNW)
        upperPt10.z = upZ

        upperPt11.x = lerp(upside.pt0.x, upside.pt2.x, relSW)
        upperPt11.y = lerp(upside.pt0.y, upside.pt2.y, relSW)
        upperPt11.z = upZ

        upperPtM.x = lerp(upside.pt0.x, upside.pt3.x, 0.5f)
        upperPtM.y = lerp(upside.pt0.y, upside.pt3.y, 0.5f)
        upperPtM.z = upZ

        lowerPt4.set(upperPt4.x, upperPt4.y, dnZ)
        lowerPt5.set(upperPt5.x, upperPt5.y, dnZ)
        lowerPt6.set(upperPt6.x, upperPt6.y, dnZ)
        lowerPt7.set(upperPt7.x, upperPt7.y, dnZ)
        lowerPt8.set(upperPt8.x, upperPt8.y, dnZ)
        lowerPt9.set(upperPt9.x, upperPt9.y, dnZ)
        lowerPt10.set(upperPt10.x, upperPt10.y, dnZ)
        lowerPt11.set(upperPt11.x, upperPt11.y, dnZ)
        lowerPtM.set(upperPtM.x, upperPtM.y, dnZ)

        texPt4.set(relSW, 1.0f)
        texPt5.set(1.0f - relSE, 1.0f)
        texPt6.set(1.0f, 1.0f - relSE)
        texPt7.set(1.0f, relNE)
        texPt8.set(1.0f - relNE, 0.0f)
        texPt9.set(relNW, 0.0f)
        texPt10.set(0.0f, relNW)
        texPt11.set(0.0f, 1.0f - relSW)
        texPtM.set(0.5f, 0.5f)
    }

    override fun tesselateUpFace(tess: Tesselator) {
        if (isBrickAboveFullyCoveringMyUpFace()) {
            return
        }

        if (cutSize >= 1.0f) {
            // Optimisations for special cases

            when {
                cutNE && !cutNW && !cutSE && !cutSW -> {
                    tess.addTriangle(
                        upperPt2,
                        upperPt0,
                        upperPt1,
                        upFaceIdx,
                        Direction.upVector,
                        0.0f,
                    )
                    return
                }
                cutNW && !cutNE && !cutSE && !cutSW -> {
                    tess.addTriangle(
                        upperPt3,
                        upperPt0,
                        upperPt1,
                        upFaceIdx,
                        Direction.upVector,
                        1.0f,
                    )
                    return
                }
                cutSE && !cutSW && !cutNE && !cutNW -> {
                    tess.addTriangle(
                        upperPt0,
                        upperPt3,
                        upperPt2,
                        upFaceIdx,
                        Direction.upVector,
                        1.0f,
                        flipTexX = true,
                        flipTexY = true,
                    )
                    return
                }
                cutSW && !cutSE && !cutNE && !cutNW -> {
                    tess.addTriangle(
                        upperPt1,
                        upperPt3,
                        upperPt2,
                        upFaceIdx,
                        Direction.upVector,
                        0.0f,
                        flipTexX = true,
                        flipTexY = true,
                    )
                    return
                }
            }
        }

        if (cutSW) {
            tess.addTriangle(
                upperPt11,
                upperPt4,
                upperPtM,
                upFaceIdx,
                Direction.upVector,
                texPt11,
                texPt4,
                texPtM,
            )
        }

        tess.addTriangle(
            upperPt4,
            upperPt5,
            upperPtM,
            upFaceIdx,
            Direction.upVector,
            texPt4,
            texPt5,
            texPtM,
        )

        if (cutSE) {
            tess.addTriangle(
                upperPt5,
                upperPt6,
                upperPtM,
                upFaceIdx,
                Direction.upVector,
                texPt5,
                texPt6,
                texPtM,
            )
        }

        tess.addTriangle(
            upperPt6,
            upperPt7,
            upperPtM,
            upFaceIdx,
            Direction.upVector,
            texPt6,
            texPt7,
            texPtM,
        )

        if (cutNE) {
            tess.addTriangle(
                upperPt7,
                upperPt8,
                upperPtM,
                upFaceIdx,
                Direction.upVector,
                texPt7,
                texPt8,
                texPtM,
            )
        }

        tess.addTriangle(
            upperPt8,
            upperPt9,
            upperPtM,
            upFaceIdx,
            Direction.upVector,
            texPt8,
            texPt9,
            texPtM,
        )

        if (cutNW) {
            tess.addTriangle(
                upperPt9,
                upperPt10,
                upperPtM,
                upFaceIdx,
                Direction.upVector,
                texPt9,
                texPt10,
                texPtM,
            )
        }

        tess.addTriangle(
            upperPt10,
            upperPt11,
            upperPtM,
            upFaceIdx,
            Direction.upVector,
            texPt10,
            texPt11,
            texPtM,
        )
    }

    override fun tesselateDownFace(tess: Tesselator) {
        if (isBrickBelowFullyCoveringMyDownFace()) {
            return
        }

        if (cutSize >= 1.0f) {
            // Optimisations for special cases

            when {
                cutNE && !cutNW && !cutSE && !cutSW -> {
                    tess.addTriangle(
                        lowerPt2,
                        lowerPt1,
                        lowerPt0,
                        downFaceIdx,
                        Direction.downVector,
                        1.0f,
                    )
                    return
                }
                cutNW && !cutNE && !cutSE && !cutSW -> {
                    tess.addTriangle(
                        lowerPt3,
                        lowerPt1,
                        lowerPt0,
                        downFaceIdx,
                        Direction.downVector,
                        0.0f,
                    )
                    return
                }
                cutSE && !cutSW && !cutNE && !cutNW -> {
                    tess.addTriangle(
                        lowerPt0,
                        lowerPt2,
                        lowerPt3,
                        downFaceIdx,
                        Direction.downVector,
                        0.0f,
                        flipTexX = true,
                        flipTexY = true,
                    )
                    return
                }
                cutSW && !cutSE && !cutNE && !cutNW -> {
                    tess.addTriangle(
                        lowerPt1,
                        lowerPt2,
                        lowerPt3,
                        downFaceIdx,
                        Direction.downVector,
                        1.0f,
                        flipTexX = true,
                        flipTexY = true,
                    )
                    return
                }
            }
        }

        if (cutSW) {
            tess.addTriangle(
                lowerPt4,
                lowerPt11,
                lowerPtM,
                downFaceIdx,
                Direction.downVector,
                texPt4,
                texPt11,
                texPtM,
                flipTexX = true,
            )
        }

        tess.addTriangle(
            lowerPt5,
            lowerPt4,
            lowerPtM,
            downFaceIdx,
            Direction.downVector,
            texPt5,
            texPt4,
            texPtM,
            flipTexX = true,
        )

        if (cutSE) {
            tess.addTriangle(
                lowerPt6,
                lowerPt5,
                lowerPtM,
                downFaceIdx,
                Direction.downVector,
                texPt6,
                texPt5,
                texPtM,
                flipTexX = true,
            )
        }

        tess.addTriangle(
            lowerPt7,
            lowerPt6,
            lowerPtM,
            downFaceIdx,
            Direction.downVector,
            texPt7,
            texPt6,
            texPtM,
            flipTexX = true,
        )

        if (cutNE) {
            tess.addTriangle(
                lowerPt8,
                lowerPt7,
                lowerPtM,
                downFaceIdx,
                Direction.downVector,
                texPt8,
                texPt7,
                texPtM,
                flipTexX = true,
            )
        }

        tess.addTriangle(
            lowerPt9,
            lowerPt8,
            lowerPtM,
            downFaceIdx,
            Direction.downVector,
            texPt9,
            texPt8,
            texPtM,
            flipTexX = true,
        )

        if (cutNW) {
            tess.addTriangle(
                lowerPt10,
                lowerPt9,
                lowerPtM,
                downFaceIdx,
                Direction.downVector,
                texPt10,
                texPt9,
                texPtM,
                flipTexX = true,
            )
        }

        tess.addTriangle(
            lowerPt11,
            lowerPt10,
            lowerPtM,
            downFaceIdx,
            Direction.downVector,
            texPt11,
            texPt10,
            texPtM,
            flipTexX = true,
        )
    }

    override fun tesselateNorthFace(tess: Tesselator) {
        // We also render the NW face here.

        if (!cutNW && isBrickToNorthFullyCoveringMyNorthFace()) {
            return
        }

        val texRelX: Float
        val texRelHeight = 1.0f - relInsetUpFace - relInsetDownFace

        if ((cutNW && cutNE && cutSize >= 0.5f) || ((cutNW || cutNE) && cutSize >= 1.0f)) {
            // There is no north face.
            texRelX = 0.0f
        } else {
            // The north face.
            texRelX = 1.0f - (if (cutNW) cutSize else 0.0f) - (if (cutNE) cutSize else 0.0f)
            tess.addQuad(
                lowerPt8,
                lowerPt9,
                upperPt8,
                upperPt9,
                northFaceIdx,
                Direction.northVector,
                texRelX = 0.0f,
                texRelWidth = texRelX,
                texRelHeight = texRelHeight,
            )
        }

        if (cutNW) {
            // The NW face.
            tess.addQuad(
                lowerPt9,
                lowerPt10,
                upperPt9,
                upperPt10,
                northFaceIdx,
                null,
                texRelX = texRelX,
                texRelWidth = 1.0f - texRelX,
                texRelHeight = texRelHeight,
            )
        }
    }

    override fun tesselateEastFace(tess: Tesselator) {
        // We also render the NE face here.

        if (!cutNE && isBrickToEastFullyCoveringMyEastFace()) {
            return
        }

        val texRelX: Float
        val texRelHeight = 1.0f - relInsetUpFace - relInsetDownFace

        if ((cutNE && cutSE && cutSize >= 0.5f) || ((cutNE || cutSE) && cutSize >= 1.0f)) {
            // There is no east face.
            texRelX = 0.0f
        } else {
            // The east face.
            texRelX = 1.0f - (if (cutNE) cutSize else 0.0f) - (if (cutSE) cutSize else 0.0f)
            tess.addQuad(
                lowerPt6,
                lowerPt7,
                upperPt6,
                upperPt7,
                eastFaceIdx,
                Direction.eastVector,
                texRelX = 0.0f,
                texRelWidth = texRelX,
                texRelHeight = texRelHeight,
            )
        }

        if (cutNE) {
            // The NE face.
            tess.addQuad(
                lowerPt7,
                lowerPt8,
                upperPt7,
                upperPt8,
                eastFaceIdx,
                null,
                texRelX = texRelX,
                texRelWidth = 1.0f - texRelX,
                texRelHeight = texRelHeight,
            )
        }
    }

    override fun tesselateSouthFace(tess: Tesselator) {
        // We also render the SE face here.

        if (!cutSE && isBrickToSouthFullyCoveringMySouthFace()) {
            return
        }

        val texRelX: Float
        val texRelHeight = 1.0f - relInsetUpFace - relInsetDownFace

        if ((cutSE && cutSW && cutSize >= 0.5f) || ((cutSE || cutSW) && cutSize >= 1.0f)) {
            // There is no south face.
            texRelX = 0.0f
        } else {
            // The south face.
            texRelX = 1.0f - (if (cutSE) cutSize else 0.0f) - (if (cutSW) cutSize else 0.0f)
            tess.addQuad(
                lowerPt4,
                lowerPt5,
                upperPt4,
                upperPt5,
                southFaceIdx,
                Direction.southVector,
                texRelX = 0.0f,
                texRelWidth = texRelX,
                texRelHeight = texRelHeight,
            )
        }

        if (cutSE) {
            // The SE face.
            tess.addQuad(
                lowerPt5,
                lowerPt6,
                upperPt5,
                upperPt6,
                southFaceIdx,
                null,
                texRelX = texRelX,
                texRelWidth = 1.0f - texRelX,
                texRelHeight = texRelHeight,
            )
        }
    }

    override fun tesselateWestFace(tess: Tesselator) {
        // We also render the SW face here.

        if (!cutSW && isBrickToWestFullyCoveringMyWestFace()) {
            return
        }

        val texRelX: Float
        val texRelHeight = 1.0f - relInsetUpFace - relInsetDownFace

        if ((cutSW && cutNW && cutSize >= 0.5f) || ((cutSW || cutNW) && cutSize >= 1.0f)) {
            // There is no west face.
            texRelX = 0.0f
        } else {
            // The west face.
            texRelX = 1.0f - (if (cutSW) cutSize else 0.0f) - (if (cutNW) cutSize else 0.0f)
            tess.addQuad(
                lowerPt10,
                lowerPt11,
                upperPt10,
                upperPt11,
                westFaceIdx,
                Direction.westVector,
                texRelX = 0.0f,
                texRelWidth = texRelX,
                texRelHeight = texRelHeight,
            )
        }

        if (cutSW) {
            // The SW face.
            tess.addQuad(
                lowerPt11,
                lowerPt4,
                upperPt11,
                upperPt4,
                westFaceIdx,
                null,
                texRelX = texRelX,
                texRelWidth = 1.0f - texRelX,
                texRelHeight = texRelHeight,
            )
        }
    }

    override fun heightAt(x: Float, y: Float) =
        upperPt3.z // TODO
}
