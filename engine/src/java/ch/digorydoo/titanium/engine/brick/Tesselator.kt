package ch.digorydoo.titanium.engine.brick

import ch.digorydoo.kutils.math.lerp
import ch.digorydoo.kutils.point.MutablePoint2f
import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.kutils.point.Point2f
import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.kutils.utils.Log
import kotlin.math.floor
import kotlin.math.min

/**
 * This class is used by the various brick models to generate the triangle lists of a BrickVolume.
 */
class Tesselator(
    private val translation: Point3f,
    private val texWidth: Int,
    private val texHeight: Int,
    trianglesList: BrickModelData.FloatLists,
) {
    init {
        require(texWidth == NUM_GROUPS_IN_TEX_X * GROUP_WIDTH_IN_TEXELS)
        require(texHeight == NUM_GROUPS_IN_TEX_Y * GROUP_HEIGHT_IN_TEXELS)
    }

    private val posList = trianglesList.positions
    private val normalList = trianglesList.normals
    private val texCoordList = trianglesList.texCoords

    // Using Pair here instead of Point2i hoping that it gets inlined
    private fun texelOrigin(faceIdx: Int): Pair<Int, Int> {
        val brickInGroupIdx = faceIdx % NUM_BRICKS_IN_GROUP_XY
        val cumulatedGroupIdx = faceIdx / NUM_BRICKS_IN_GROUP_XY

        val groupXIdx = cumulatedGroupIdx % NUM_GROUPS_IN_TEX_X
        val cumulatedBrickRowIdx = cumulatedGroupIdx / NUM_GROUPS_IN_TEX_X

        val rowOfGroupIdx = cumulatedBrickRowIdx % NUM_BRICKS_IN_GROUP_XY
        val groupYIdx = cumulatedBrickRowIdx / NUM_BRICKS_IN_GROUP_XY

        val groupOriginX = GROUP_WIDTH_IN_TEXELS * groupXIdx
        val groupOriginY = GROUP_HEIGHT_IN_TEXELS * groupYIdx

        val brickOriginX = groupOriginX + TEX_GROUP_PADDING + brickInGroupIdx * TEX_BRICK_WIDTH
        val brickOriginY = groupOriginY + TEX_GROUP_PADDING + rowOfGroupIdx * TEX_BRICK_HEIGHT

        return Pair(brickOriginX, brickOriginY)
    }

    fun addQuad(
        pt1: Point3f,
        pt2: Point3f,
        pt3: Point3f,
        pt4: Point3f,
        faceIdx: Int,
        normal: Point3f?,
        texRelPt1: Point2f,
        texRelPt2: Point2f,
        texRelPt3: Point2f,
        texRelPt4: Point2f,
        flipTexX: Boolean = false,
        flipDiagonal: Boolean = false,
    ) {
        if (flipDiagonal) {
            // First triangle
            addPos(pt1)
            addPos(pt2)
            addPos(pt4)

            // Second triangle
            addPos(pt3)
            addPos(pt1)
            addPos(pt4)
        } else {
            // First triangle
            addPos(pt1)
            addPos(pt2)
            addPos(pt3)

            // Second triangle
            addPos(pt3)
            addPos(pt2)
            addPos(pt4)
        }

        val n = normal ?: computeNormal(pt1, pt2, pt3)

        // 2 triangles à 3 points each = 6 normals
        (0 ..< 6).forEach {
            addNormal(n)
        }

        val (ox, oy) = texelOrigin(faceIdx)

        val tr1x: Float
        val tr2x: Float
        val tr3x: Float
        val tr4x: Float

        if (flipTexX) {
            tr1x = 1.0f - texRelPt1.x
            tr2x = 1.0f - texRelPt2.x
            tr3x = 1.0f - texRelPt3.x
            tr4x = 1.0f - texRelPt4.x
        } else {
            tr1x = texRelPt1.x
            tr2x = texRelPt2.x
            tr3x = texRelPt3.x
            tr4x = texRelPt4.x
        }

        val tf1x = floor(ox + (tr1x * TEX_BRICK_WIDTH)) / texWidth
        val tf1y = floor(oy + (texRelPt1.y * TEX_BRICK_HEIGHT)) / texHeight

        val tf2x = floor(ox + (tr2x * TEX_BRICK_WIDTH)) / texWidth
        val tf2y = floor(oy + (texRelPt2.y * TEX_BRICK_HEIGHT)) / texHeight

        val tf3x = floor(ox + (tr3x * TEX_BRICK_WIDTH)) / texWidth
        val tf3y = floor(oy + (texRelPt3.y * TEX_BRICK_HEIGHT)) / texHeight

        val tf4x = floor(ox + (tr4x * TEX_BRICK_WIDTH)) / texWidth
        val tf4y = floor(oy + (texRelPt4.y * TEX_BRICK_HEIGHT)) / texHeight

        if (flipDiagonal) {
            texCoordList.apply {
                // First triangle
                add(tf1x); add(tf1y)
                add(tf2x); add(tf2y)
                add(tf4x); add(tf4y)

                // Second triangle
                add(tf3x); add(tf3y)
                add(tf1x); add(tf1y)
                add(tf4x); add(tf4y)
            }
        } else {
            texCoordList.apply {
                // First triangle
                add(tf1x); add(tf1y)
                add(tf2x); add(tf2y)
                add(tf3x); add(tf3y)

                // Second triangle
                add(tf3x); add(tf3y)
                add(tf2x); add(tf2y)
                add(tf4x); add(tf4y)
            }
        }
    }

    fun addQuad(
        pt1: Point3f,
        pt2: Point3f,
        pt3: Point3f,
        pt4: Point3f,
        faceIdx: Int,
        normal: Point3f?,
        flipDiagonal: Boolean = false,
        texRelX: Float = 0.0f,
        texRelY: Float = 0.0f,
        texRelWidth: Float = 1.0f,
        texRelHeight: Float = 1.0f,
    ) {
        if (flipDiagonal) {
            // First triangle
            addPos(pt1)
            addPos(pt2)
            addPos(pt4)

            // Second triangle
            addPos(pt3)
            addPos(pt1)
            addPos(pt4)
        } else {
            // First triangle
            addPos(pt1)
            addPos(pt2)
            addPos(pt3)

            // Second triangle
            addPos(pt3)
            addPos(pt2)
            addPos(pt4)
        }

        val n = normal ?: computeNormal(pt1, pt2, pt3)

        // 2 triangles à 3 points each = 6 normals
        (0 ..< 6).forEach {
            addNormal(n)
        }

        val (ox, oy) = texelOrigin(faceIdx)
        val tx1: Int = ox + (texRelX * TEX_BRICK_WIDTH).toInt()
        val ty1: Int = oy + (texRelY * TEX_BRICK_HEIGHT).toInt()
        val tx2 = tx1 + texRelWidth * TEX_BRICK_WIDTH
        val ty2 = ty1 + texRelHeight * TEX_BRICK_HEIGHT
        val ftx1 = tx1.toFloat() / texWidth
        val fty1 = ty1.toFloat() / texHeight
        val ftx2 = tx2 / texWidth
        val fty2 = ty2 / texHeight

        if (flipDiagonal) {
            texCoordList.apply {
                // First triangle
                add(ftx1); add(fty2)
                add(ftx2); add(fty2)
                add(ftx2); add(fty1)

                // Second triangle
                add(ftx1); add(fty1)
                add(ftx1); add(fty2)
                add(ftx2); add(fty1)
            }
        } else {
            texCoordList.apply {
                // First triangle
                add(ftx1); add(fty2)
                add(ftx2); add(fty2)
                add(ftx1); add(fty1)

                // Second triangle
                add(ftx1); add(fty1)
                add(ftx2); add(fty2)
                add(ftx2); add(fty1)
            }
        }
    }

    fun addFan(
        centre: Point3f,
        circum: Array<MutablePoint3f>,
        texMid: Point2f,
        texCirc: Array<MutablePoint2f>,
        faceIdx: Int,
        ccw: Boolean = true,
        flipTexX: Boolean = false,
    ) {
        // TODO: Implement this as a GL fan

        if (circum.size < 2) {
            Log.warn("Tesselator: addFan: Too few points for circumference: ${circum.size}")
            return
        }

        val (tx1, ty1) = texelOrigin(faceIdx)
        val tx2 = tx1 + TEX_BRICK_WIDTH
        val ty2 = ty1 + TEX_BRICK_HEIGHT
        var ftx1 = tx1.toFloat() / texWidth
        val fty1 = ty1.toFloat() / texHeight
        var ftx2 = tx2.toFloat() / texWidth
        val fty2 = ty2.toFloat() / texHeight

        if (flipTexX) {
            val tmp = ftx1
            ftx1 = ftx2
            ftx2 = tmp
        }

        for (i in circum.indices) {
            val p: Point3f
            val q: Point3f

            if (ccw) {
                p = circum[i]
                q = circum[(i + 1) % circum.size]
            } else {
                p = circum[(i + 1) % circum.size]
                q = circum[i]
            }

            addPos(centre)
            addPos(p)
            addPos(q)

            val tp: Point2f
            val tq: Point2f

            if (ccw) {
                tp = texCirc[i]
                tq = texCirc[(i + 1) % texCirc.size]
            } else {
                tp = texCirc[(i + 1) % texCirc.size]
                tq = texCirc[i]
            }

            addTexCoord(lerp(ftx1, ftx2, texMid.x), lerp(fty1, fty2, texMid.y))
            addTexCoord(lerp(ftx1, ftx2, tp.x), lerp(fty1, fty2, tp.y))
            addTexCoord(lerp(ftx1, ftx2, tq.x), lerp(fty1, fty2, tq.y))

            val normal = computeNormal(centre, p, q)
            addNormal(normal)
            addNormal(normal)
            addNormal(normal)
        }
    }

    fun addStrip(
        upperPts: Array<MutablePoint3f>,
        lowerPts: Array<MutablePoint3f>,
        faceIdx: Int,
        close: Boolean,
        texWrapCount: Int,
    ) {
        // TODO: Implement this as a GL strip

        val count = min(upperPts.size, lowerPts.size)

        if (count < 2) {
            Log.warn("Tesselator: addStrip: Too few points for upper and/or lower: $count")
            return
        }

        var p = upperPts[0]
        var q = lowerPts[0]
        val texRelWidth = texWrapCount.toFloat() / count

        for (i in 1 ..< count) {
            val r = upperPts[i]
            val s = lowerPts[i]
            val texRelX = ((i - 1).toFloat() * texRelWidth) % 1.0f

            addTriangle(
                q,
                r,
                p,
                faceIdx,
                null,
                1.0f,
                texRelX = texRelX,
                texRelWidth = texRelWidth,
                flipTexX = true,
                flipTexY = true,
            )
            addTriangle(
                r,
                q,
                s,
                faceIdx,
                null,
                1.0f,
                texRelX = texRelX,
                texRelWidth = texRelWidth,
            )

            p = r
            q = s
        }

        if (close) {
            val texRelX = ((count - 1).toFloat() * texRelWidth) % 1.0f

            addTriangle(
                q,
                upperPts[0],
                p,
                faceIdx,
                null,
                1.0f,
                texRelX = texRelX,
                texRelWidth = texRelWidth,
                flipTexX = true,
                flipTexY = true
            )
            addTriangle(
                upperPts[0],
                q,
                lowerPts[0],
                faceIdx,
                null,
                1.0f,
                texRelX = texRelX,
                texRelWidth = texRelWidth,
            )
        }
    }

    fun addTriangle(
        pt1: Point3f,
        pt2: Point3f,
        pt3: Point3f,
        faceIdx: Int,
        normal: Point3f?,
        texRelPt1: Point2f,
        texRelPt2: Point2f,
        texRelPt3: Point2f,
        flipTexX: Boolean = false,
    ) {
        addPos(pt1)
        addPos(pt2)
        addPos(pt3)

        val n = normal ?: computeNormal(pt1, pt2, pt3)
        addNormal(n)
        addNormal(n)
        addNormal(n)

        val (ox, oy) = texelOrigin(faceIdx)
        val tr1x: Float
        val tr2x: Float
        val tr3x: Float

        if (flipTexX) {
            tr1x = 1.0f - texRelPt1.x
            tr2x = 1.0f - texRelPt2.x
            tr3x = 1.0f - texRelPt3.x
        } else {
            tr1x = texRelPt1.x
            tr2x = texRelPt2.x
            tr3x = texRelPt3.x
        }

        addTexCoord(
            floor(ox + (tr1x * TEX_BRICK_WIDTH)) / texWidth,
            floor(oy + (texRelPt1.y * TEX_BRICK_HEIGHT)) / texHeight,
        )
        addTexCoord(
            floor(ox + (tr2x * TEX_BRICK_WIDTH)) / texWidth,
            floor(oy + (texRelPt2.y * TEX_BRICK_HEIGHT)) / texHeight,
        )
        addTexCoord(
            floor(ox + (tr3x * TEX_BRICK_WIDTH)) / texWidth,
            floor(oy + (texRelPt3.y * TEX_BRICK_HEIGHT)) / texHeight,
        )
    }

    fun addTriangle(
        pt1: Point3f, // called pivot
        pt2: Point3f,
        pt3: Point3f,
        faceIdx: Int,
        normal: Point3f?,
        texPivotX: Float,
        pivotIsBrickRelative: Boolean = false,
        texRelX: Float = 0.0f,
        texRelY: Float = 0.0f,
        texRelWidth: Float = 1.0f,
        texRelHeight: Float = 1.0f,
        flipTexX: Boolean = false,
        flipTexY: Boolean = false,
        rotateTex: Boolean = false,
    ) {
        addPos(pt1)
        addPos(pt2)
        addPos(pt3)

        val n = normal ?: computeNormal(pt1, pt2, pt3)
        addNormal(n)
        addNormal(n)
        addNormal(n)

        val (ox, oy) = texelOrigin(faceIdx)

        val tx1: Int
        val ty1: Int
        val tx2: Int
        val ty2: Int

        if (rotateTex) {
            tx2 = ox + ((1.0f - texRelY) * TEX_BRICK_WIDTH).toInt()
            ty1 = oy + (texRelX * TEX_BRICK_HEIGHT).toInt()
            tx1 = (tx2 - texRelHeight * TEX_BRICK_WIDTH).toInt()
            ty2 = (ty1 + texRelWidth * TEX_BRICK_HEIGHT).toInt()
        } else {
            tx1 = ox + (texRelX * TEX_BRICK_WIDTH).toInt()
            ty1 = oy + (texRelY * TEX_BRICK_HEIGHT).toInt()
            tx2 = (tx1 + texRelWidth * TEX_BRICK_WIDTH).toInt()
            ty2 = (ty1 + texRelHeight * TEX_BRICK_HEIGHT).toInt()
        }

        var ftx1 = tx1.toFloat() / texWidth
        var fty1 = ty1.toFloat() / texHeight
        var ftx2 = tx2.toFloat() / texWidth
        var fty2 = ty2.toFloat() / texHeight

        if (flipTexX) {
            val tmp = ftx1
            ftx1 = ftx2
            ftx2 = tmp
        }

        if (flipTexY) {
            val tmp = fty1
            fty1 = fty2
            fty2 = tmp
        }

        if (rotateTex) {
            val yp = if (pivotIsBrickRelative) {
                floor(oy + (texPivotX * TEX_BRICK_HEIGHT)) / texHeight
            } else {
                lerp(fty1, fty2, texPivotX)
            }

            texCoordList.apply {
                add(ftx2); add(yp)
                add(ftx1); add(fty1)
                add(ftx1); add(fty2)
            }
        } else {
            val xp = if (pivotIsBrickRelative) {
                floor(ox + (texPivotX * TEX_BRICK_WIDTH)) / texWidth
            } else {
                lerp(ftx1, ftx2, texPivotX)
            }

            texCoordList.apply {
                add(xp); add(fty1)
                add(ftx1); add(fty2)
                add(ftx2); add(fty2)
            }
        }
    }

    private fun computeNormal(a: Point3f, b: Point3f, c: Point3f) =
        (b.toMutable() - a).cross(c - a).normalize()

    private fun addPos(pt: Point3f) {
        posList.add(pt.x + translation.x)
        posList.add(pt.y + translation.y)
        posList.add(pt.z + translation.z)
    }

    private fun addNormal(normal: Point3f) {
        normalList.add(normal.x)
        normalList.add(normal.y)
        normalList.add(normal.z)
    }

    private fun addTexCoord(x: Float, y: Float) {
        texCoordList.add(x)
        texCoordList.add(y)
    }

    companion object {
        private const val TEX_BRICK_WIDTH = 64
        private const val TEX_BRICK_HEIGHT = 64
        private const val NUM_GROUPS_IN_TEX_X = 9
        private const val NUM_GROUPS_IN_TEX_Y = 5
        private const val NUM_BRICKS_IN_GROUP_XY = 3 // 3x3 bricks make up a group
        private const val TEX_GROUP_PADDING = 2 // each group is padded so that inaccuracies don't lead to a seam
        private const val GROUP_WIDTH_IN_TEXELS = (2 * TEX_GROUP_PADDING + NUM_BRICKS_IN_GROUP_XY * TEX_BRICK_WIDTH)
        private const val GROUP_HEIGHT_IN_TEXELS = (2 * TEX_GROUP_PADDING + NUM_BRICKS_IN_GROUP_XY * TEX_BRICK_HEIGHT)
    }
}
