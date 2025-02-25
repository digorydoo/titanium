package ch.digorydoo.titanium.engine.brick.zz_various

import ch.digorydoo.kutils.math.lerp
import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.titanium.engine.brick.AbstrBrickModel
import ch.digorydoo.titanium.engine.brick.BrickShape.*
import ch.digorydoo.titanium.engine.brick.BrickSubvolume
import ch.digorydoo.titanium.engine.brick.Tesselator
import ch.digorydoo.titanium.engine.utils.Direction

class FlatFaceModel: AbstrBrickModel() {
    private var insetNorth = 0.0f
    private var insetEast = 0.0f
    private var insetWest = 0.0f
    private var insetSouth = 0.0f
    private var insetUpside = 0.0f
    private val tempPt0 = MutablePoint3f()
    private val tempPt1 = MutablePoint3f()
    private val tempPt2 = MutablePoint3f()
    private val tempPt3 = MutablePoint3f()

    override fun prepare(ix: Int, iy: Int, iz: Int, subvolume: BrickSubvolume) {
        super.prepare(ix, iy, iz, subvolume)

        // Avoid z-fighting with neighbouring bricks by setting inset to make sure faces do not overlap.

        when (upside.brick.shape) {
            FLAT_WALL_NORTH -> {
                insetEast = when (shapeToNorth) {
                    UPRIGHT_DBL_BAR_EAST,
                    UPRIGHT_DBL_BAR_SOUTH,
                    BAR_FRAME_EAST,
                    BAR_FRAME_SOUTH,
                    UPRIGHT_BAR_SE,
                    -> THIN_RELSIZE
                    else -> 0.0f
                }
                insetWest = when (shapeToNorth) {
                    UPRIGHT_DBL_BAR_SOUTH,
                    UPRIGHT_DBL_BAR_WEST,
                    BAR_FRAME_SOUTH,
                    BAR_FRAME_WEST,
                    UPRIGHT_BAR_SW,
                    -> THIN_RELSIZE
                    else -> 0.0f
                }
                insetUpside = when (shapeToNorth) {
                    BAR_FRAME_SOUTH -> THIN_RELSIZE
                    else -> 0.0f
                }
            }
            FLAT_WALL_EAST -> {
                insetNorth = when (shapeToEast) {
                    UPRIGHT_DBL_BAR_NORTH,
                    UPRIGHT_DBL_BAR_WEST,
                    BAR_FRAME_NORTH,
                    BAR_FRAME_WEST,
                    UPRIGHT_BAR_NW,
                    -> THIN_RELSIZE
                    else -> 0.0f
                }
                insetSouth = when (shapeToEast) {
                    UPRIGHT_DBL_BAR_SOUTH,
                    UPRIGHT_DBL_BAR_WEST,
                    BAR_FRAME_SOUTH,
                    BAR_FRAME_WEST,
                    UPRIGHT_BAR_SW,
                    -> THIN_RELSIZE
                    else -> 0.0f
                }
                insetUpside = when (shapeToEast) {
                    BAR_FRAME_WEST -> THIN_RELSIZE
                    else -> 0.0f
                }
            }
            FLAT_WALL_SOUTH -> {
                insetEast = when (shapeToSouth) {
                    UPRIGHT_DBL_BAR_EAST,
                    UPRIGHT_DBL_BAR_NORTH,
                    BAR_FRAME_EAST,
                    BAR_FRAME_NORTH,
                    UPRIGHT_BAR_NE,
                    -> THIN_RELSIZE
                    else -> 0.0f
                }
                insetWest = when (shapeToSouth) {
                    UPRIGHT_DBL_BAR_NORTH,
                    UPRIGHT_DBL_BAR_WEST,
                    BAR_FRAME_NORTH,
                    BAR_FRAME_WEST,
                    UPRIGHT_BAR_NW,
                    -> THIN_RELSIZE
                    else -> 0.0f
                }
                insetUpside = when (shapeToSouth) {
                    BAR_FRAME_NORTH -> THIN_RELSIZE
                    else -> 0.0f
                }
            }
            FLAT_WALL_WEST -> {
                insetNorth = when (shapeToWest) {
                    UPRIGHT_DBL_BAR_EAST,
                    UPRIGHT_DBL_BAR_NORTH,
                    BAR_FRAME_EAST,
                    BAR_FRAME_NORTH,
                    UPRIGHT_BAR_NE,
                    -> THIN_RELSIZE
                    else -> 0.0f
                }
                insetSouth = when (shapeToWest) {
                    UPRIGHT_DBL_BAR_EAST,
                    UPRIGHT_DBL_BAR_SOUTH,
                    BAR_FRAME_EAST,
                    BAR_FRAME_SOUTH,
                    UPRIGHT_BAR_SE,
                    -> THIN_RELSIZE
                    else -> 0.0f
                }
                insetUpside = when (shapeToWest) {
                    BAR_FRAME_EAST -> THIN_RELSIZE
                    else -> 0.0f
                }
            }
            else -> Unit
        }
    }

    override fun heightAt(x: Float, y: Float) = when (upside.brick.shape) {
        FLAT_FLOOR -> downside.pt0.z
        FLAT_CEILING -> upside.pt0.z
        else -> null
    }

    override fun tesselateUpFace(tess: Tesselator) {
        if (upside.brick.shape == FLAT_FLOOR) {
            tess.addQuad(
                downside.pt0,
                downside.pt1,
                downside.pt2,
                downside.pt3,
                upFaceIdx,
                Direction.upVector,
            )
        }

        // Also tesselate the backside. Non-opaque materials such as GLASS still need to use cull facing, because
        // otherwise strange effects can happen due to incorrect ordering of faces.

        if (upside.brick.shape == FLAT_CEILING) {
            tess.addQuad(
                upside.pt0,
                upside.pt1,
                upside.pt2,
                upside.pt3,
                upFaceIdx,
                Direction.upVector,
            )
        }
    }

    override fun tesselateDownFace(tess: Tesselator) {
        if (upside.brick.shape == FLAT_CEILING) {
            tess.addQuad(
                upside.pt1,
                upside.pt0,
                upside.pt3,
                upside.pt2,
                downFaceIdx,
                Direction.downVector,
            )
        }

        if (upside.brick.shape == FLAT_FLOOR) {
            tess.addQuad(
                downside.pt1,
                downside.pt0,
                downside.pt3,
                downside.pt2,
                downFaceIdx,
                Direction.downVector,
            )
        }
    }

    override fun tesselateNorthFace(tess: Tesselator) {
        if (upside.brick.shape == FLAT_WALL_SOUTH) {
            tempPt0.set(
                downside.pt1.x,
                lerp(downside.pt1.y, downside.pt0.y, insetEast),
                downside.pt1.z,
            )
            tempPt1.set(
                downside.pt0.x,
                lerp(downside.pt0.y, downside.pt1.y, insetWest),
                downside.pt0.z,
            )
            val upZ = lerp(upside.pt0.z, downside.pt0.z, insetUpside)
            tempPt2.set(
                upside.pt1.x,
                lerp(upside.pt1.y, upside.pt0.y, insetEast),
                upZ,
            )
            tempPt3.set(
                upside.pt0.x,
                lerp(upside.pt0.y, upside.pt1.y, insetWest),
                upZ,
            )
            tess.addQuad(
                tempPt0,
                tempPt1,
                tempPt2,
                tempPt3,
                northFaceIdx,
                Direction.northVector,
            )
        }

        // Backside
        if (upside.brick.shape == FLAT_WALL_NORTH) {
            tempPt0.set(
                downside.pt2.x,
                lerp(downside.pt2.y, downside.pt3.y, insetWest),
                downside.pt2.z,
            )
            tempPt1.set(
                downside.pt3.x,
                lerp(downside.pt3.y, downside.pt2.y, insetEast),
                downside.pt3.z,
            )
            val upZ = lerp(upside.pt0.z, downside.pt0.z, insetUpside)
            tempPt2.set(
                upside.pt2.x,
                lerp(upside.pt2.y, upside.pt3.y, insetWest),
                upZ,
            )
            tempPt3.set(
                upside.pt3.x,
                lerp(upside.pt3.y, upside.pt2.y, insetEast),
                upZ,
            )
            tess.addQuad(
                tempPt1,
                tempPt0,
                tempPt3,
                tempPt2,
                northFaceIdx,
                Direction.northVector,
            )
        }
    }

    override fun tesselateSouthFace(tess: Tesselator) {
        if (upside.brick.shape == FLAT_WALL_NORTH) {
            tempPt0.set(
                downside.pt2.x,
                lerp(downside.pt2.y, downside.pt3.y, insetWest),
                downside.pt2.z,
            )
            tempPt1.set(
                downside.pt3.x,
                lerp(downside.pt3.y, downside.pt2.y, insetEast),
                downside.pt3.z,
            )
            val upZ = lerp(upside.pt0.z, downside.pt0.z, insetUpside)
            tempPt2.set(
                upside.pt2.x,
                lerp(upside.pt2.y, upside.pt3.y, insetWest),
                upZ,
            )
            tempPt3.set(
                upside.pt3.x,
                lerp(upside.pt3.y, upside.pt2.y, insetEast),
                upZ,
            )
            tess.addQuad(
                tempPt0,
                tempPt1,
                tempPt2,
                tempPt3,
                southFaceIdx,
                Direction.southVector,
            )
        }

        // Backside
        if (upside.brick.shape == FLAT_WALL_SOUTH) {
            tempPt0.set(
                downside.pt1.x,
                lerp(downside.pt1.y, downside.pt0.y, insetEast),
                downside.pt1.z,
            )
            tempPt1.set(
                downside.pt0.x,
                lerp(downside.pt0.y, downside.pt1.y, insetWest),
                downside.pt0.z,
            )
            val upZ = lerp(upside.pt0.z, downside.pt0.z, insetUpside)
            tempPt2.set(
                upside.pt1.x,
                lerp(upside.pt1.y, upside.pt0.y, insetEast),
                upZ,
            )
            tempPt3.set(
                upside.pt0.x,
                lerp(upside.pt0.y, upside.pt1.y, insetWest),
                upZ,
            )
            tess.addQuad(
                tempPt1,
                tempPt0,
                tempPt3,
                tempPt2,
                southFaceIdx,
                Direction.southVector,
            )
        }
    }

    override fun tesselateEastFace(tess: Tesselator) {
        if (upside.brick.shape == FLAT_WALL_WEST) {
            tempPt0.set(
                lerp(downside.pt0.x, downside.pt2.x, insetSouth),
                downside.pt0.y,
                downside.pt0.z,
            )
            tempPt1.set(
                lerp(downside.pt2.x, downside.pt0.x, insetNorth),
                downside.pt2.y,
                downside.pt2.z,
            )
            val upZ = lerp(upside.pt0.z, downside.pt0.z, insetUpside)
            tempPt2.set(
                lerp(upside.pt0.x, upside.pt2.x, insetSouth),
                upside.pt0.y,
                upZ,
            )
            tempPt3.set(
                lerp(upside.pt2.x, upside.pt0.x, insetNorth),
                upside.pt2.y,
                upZ,
            )
            tess.addQuad(
                tempPt0,
                tempPt1,
                tempPt2,
                tempPt3,
                eastFaceIdx,
                Direction.eastVector,
            )
        }

        // Backside
        if (upside.brick.shape == FLAT_WALL_EAST) {
            tempPt0.set(
                lerp(downside.pt3.x, downside.pt1.x, insetNorth),
                downside.pt3.y,
                downside.pt3.z,
            )
            tempPt1.set(
                lerp(downside.pt1.x, downside.pt3.x, insetSouth),
                downside.pt1.y,
                downside.pt1.z,
            )
            val upZ = lerp(upside.pt0.z, downside.pt0.z, insetUpside)
            tempPt2.set(
                lerp(upside.pt3.x, upside.pt1.x, insetNorth),
                upside.pt3.y,
                upZ,
            )
            tempPt3.set(
                lerp(upside.pt1.x, upside.pt3.x, insetSouth),
                upside.pt1.y,
                upZ,
            )
            tess.addQuad(
                tempPt1,
                tempPt0,
                tempPt3,
                tempPt2,
                eastFaceIdx,
                Direction.eastVector,
            )
        }
    }

    override fun tesselateWestFace(tess: Tesselator) {
        if (upside.brick.shape == FLAT_WALL_EAST) {
            tempPt0.set(
                lerp(downside.pt3.x, downside.pt1.x, insetNorth),
                downside.pt3.y,
                downside.pt3.z,
            )
            tempPt1.set(
                lerp(downside.pt1.x, downside.pt3.x, insetSouth),
                downside.pt1.y,
                downside.pt1.z,
            )
            val upZ = lerp(upside.pt0.z, downside.pt0.z, insetUpside)
            tempPt2.set(
                lerp(upside.pt3.x, upside.pt1.x, insetNorth),
                upside.pt3.y,
                upZ,
            )
            tempPt3.set(
                lerp(upside.pt1.x, upside.pt3.x, insetSouth),
                upside.pt1.y,
                upZ,
            )
            tess.addQuad(
                tempPt0,
                tempPt1,
                tempPt2,
                tempPt3,
                westFaceIdx,
                Direction.westVector,
            )
        }

        // Backside
        if (upside.brick.shape == FLAT_WALL_WEST) {
            tempPt0.set(
                lerp(downside.pt0.x, downside.pt2.x, insetSouth),
                downside.pt0.y,
                downside.pt0.z,
            )
            tempPt1.set(
                lerp(downside.pt2.x, downside.pt0.x, insetNorth),
                downside.pt2.y,
                downside.pt2.z,
            )
            val upZ = lerp(upside.pt0.z, downside.pt0.z, insetUpside)
            tempPt2.set(
                lerp(upside.pt0.x, upside.pt2.x, insetSouth),
                upside.pt0.y,
                upZ,
            )
            tempPt3.set(
                lerp(upside.pt2.x, upside.pt0.x, insetNorth),
                upside.pt2.y,
                upZ,
            )
            tess.addQuad(
                tempPt1,
                tempPt0,
                tempPt3,
                tempPt2,
                westFaceIdx,
                Direction.westVector,
            )
        }
    }
}
