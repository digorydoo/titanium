package ch.digorydoo.titanium.engine.brick

import ch.digorydoo.titanium.engine.brick.AbstrBrickModel.Companion.BEVEL_CUT_SIZE
import ch.digorydoo.titanium.engine.brick.AbstrBrickModel.Companion.THIN_INSET
import ch.digorydoo.titanium.engine.brick.bar.BarFrameNSModel
import ch.digorydoo.titanium.engine.brick.bar.BarFrameWEModel
import ch.digorydoo.titanium.engine.brick.stairs.StraightStairsModel
import ch.digorydoo.titanium.engine.brick.stairs.WindingStairsModel
import ch.digorydoo.titanium.engine.brick.wall.*
import ch.digorydoo.titanium.engine.brick.zz_various.BlockModel
import ch.digorydoo.titanium.engine.brick.zz_various.CutModel
import ch.digorydoo.titanium.engine.brick.zz_various.UprightTubeModel
import ch.digorydoo.titanium.engine.utils.Side

// Constants shared with other components are in AbstrBrickModel.Companion
private const val BEVEL_CUT_ALT_SIZE = 0.37f // length of cut diagonal should look identical to side of a regular cut
private const val OCTA_PILLAR_RADIUS = 0.42f
private const val WINDOW_TOP_INSET = 0.7f
private const val WINDOW_TOP_SLOPE = 0.1f
private const val FULL_WINDING_STAIRS_NUM_STEPS = 6
private const val HALF_WINDING_STAIRS_NUM_STEPS = 4
private const val FULL_STRAIGHT_STAIRS_NUM_STEPS = 4
private const val THICK_RELSIZE = 1.0f / FULL_STRAIGHT_STAIRS_NUM_STEPS // stair height must match thick ceiling
private const val THICK_INSET = 1.0f - THICK_RELSIZE

internal fun makeBarFrameEast() = BarFrameWEModel(relInsetWestFace = THIN_INSET)
internal fun makeBarFrameNorth() = BarFrameNSModel(relInsetSouthFace = THIN_INSET)
internal fun makeBarFrameSouth() = BarFrameNSModel(relInsetNorthFace = THIN_INSET)
internal fun makeBarFrameWest() = BarFrameWEModel(relInsetEastFace = THIN_INSET)
internal fun makeHalfDHighBarE() = BlockModel(relInsetDownFace = 0.5f, relInsetWestFace = 0.5f)
internal fun makeHalfDHighBarN() = BlockModel(relInsetDownFace = 0.5f, relInsetSouthFace = 0.5f)
internal fun makeHalfDHighBarS() = BlockModel(relInsetDownFace = 0.5f, relInsetNorthFace = 0.5f)
internal fun makeHalfDHighBarW() = BlockModel(relInsetDownFace = 0.5f, relInsetEastFace = 0.5f)
internal fun makeHalfDWallCornerNE() = WallCornerNEModel(relInset = 0.5f, ceiling = false)
internal fun makeHalfDWallCornerNW() = WallCornerNWModel(relInset = 0.5f, ceiling = false)
internal fun makeHalfDWallCornerSE() = WallCornerSEModel(relInset = 0.5f, ceiling = false)
internal fun makeHalfDWallCornerSW() = WallCornerSWModel(relInset = 0.5f, ceiling = false)
internal fun makeHalfDWallCornerTopNE() = WallCornerNEModel(relInset = 0.5f, ceiling = true)
internal fun makeHalfDWallCornerTopNW() = WallCornerNWModel(relInset = 0.5f, ceiling = true)
internal fun makeHalfDWallCornerTopSE() = WallCornerSEModel(relInset = 0.5f, ceiling = true)
internal fun makeHalfDWallCornerTopSW() = WallCornerSWModel(relInset = 0.5f, ceiling = true)
internal fun makeHalfDWallEast() = BlockModel(relInsetWestFace = 0.5f)
internal fun makeHalfDWallNorth() = BlockModel(relInsetSouthFace = 0.5f)
internal fun makeHalfDWallSouth() = BlockModel(relInsetNorthFace = 0.5f)
internal fun makeHalfDWallTopEast() = WallTopEastModel(relInset = 0.5f)
internal fun makeHalfDWallTopNorth() = WallTopNorthModel(relInset = 0.5f)
internal fun makeHalfDWallTopSouth() = WallTopSouthModel(relInset = 0.5f)
internal fun makeHalfDWallTopWest() = WallTopWestModel(relInset = 0.5f)
internal fun makeHalfDWallWest() = BlockModel(relInsetEastFace = 0.5f)
internal fun makeHalfHCeiling() = BlockModel(relInsetDownFace = 0.5f)
internal fun makeHalfHCeilingVCutNE() = CutModel(cutNE = true, relInsetDownFace = 0.5f)
internal fun makeHalfHCeilingVCutNW() = CutModel(cutNW = true, relInsetDownFace = 0.5f)
internal fun makeHalfHCeilingVCutSE() = CutModel(cutSE = true, relInsetDownFace = 0.5f)
internal fun makeHalfHCeilingVCutSW() = CutModel(cutSW = true, relInsetDownFace = 0.5f)
internal fun makeHalfHFloor() = BlockModel(relInsetUpFace = 0.5f)
internal fun makeHalfHFloorVCutNE() = CutModel(cutNE = true, relInsetUpFace = 0.5f)
internal fun makeHalfHFloorVCutNW() = CutModel(cutNW = true, relInsetUpFace = 0.5f)
internal fun makeHalfHFloorVCutSE() = CutModel(cutSE = true, relInsetUpFace = 0.5f)
internal fun makeHalfHFloorVCutSW() = CutModel(cutSW = true, relInsetUpFace = 0.5f)
internal fun makeHalfRampTopE() = BlockModel(relInsetUpFace = 0.25f, upFaceWESlope = -0.25f)
internal fun makeHalfRampTopN() = BlockModel(relInsetUpFace = 0.25f, upFaceNSSlope = 0.25f)
internal fun makeHalfRampTopS() = BlockModel(relInsetUpFace = 0.25f, upFaceNSSlope = -0.25f)
internal fun makeHalfRampTopW() = BlockModel(relInsetUpFace = 0.25f, upFaceWESlope = 0.25f)
internal fun makeHighBarEast() = BlockModel(relInsetDownFace = THIN_INSET, relInsetWestFace = THIN_INSET)
internal fun makeHighBarNorth() = BlockModel(relInsetDownFace = THIN_INSET, relInsetSouthFace = THIN_INSET)
internal fun makeHighBarSouth() = BlockModel(relInsetDownFace = THIN_INSET, relInsetNorthFace = THIN_INSET)
internal fun makeHighBarWest() = BlockModel(relInsetDownFace = THIN_INSET, relInsetEastFace = THIN_INSET)
internal fun makeLowBarEast() = BlockModel(relInsetUpFace = THIN_INSET, relInsetWestFace = THIN_INSET)
internal fun makeLowBarNorth() = BlockModel(relInsetUpFace = THIN_INSET, relInsetSouthFace = THIN_INSET)
internal fun makeLowBarSouth() = BlockModel(relInsetUpFace = THIN_INSET, relInsetNorthFace = THIN_INSET)
internal fun makeLowBarWest() = BlockModel(relInsetUpFace = THIN_INSET, relInsetEastFace = THIN_INSET)
internal fun makeOctaUprightPillar() = UprightTubeModel(numSides = 8, radius = OCTA_PILLAR_RADIUS)
internal fun makeThckWallCornerTopNE() = WallCornerNEModel(relInset = THICK_INSET, ceiling = true)
internal fun makeThckWallCornerTopNW() = WallCornerNWModel(relInset = THICK_INSET, ceiling = true)
internal fun makeThckWallCornerTopSE() = WallCornerSEModel(relInset = THICK_INSET, ceiling = true)
internal fun makeThckWallCornerTopSW() = WallCornerSWModel(relInset = THICK_INSET, ceiling = true)
internal fun makeThickCeiling() = BlockModel(relInsetDownFace = THICK_INSET)
internal fun makeThickCeilingVCutNE() = CutModel(cutNE = true, relInsetDownFace = THICK_INSET)
internal fun makeThickCeilingVCutNW() = CutModel(cutNW = true, relInsetDownFace = THICK_INSET)
internal fun makeThickCeilingVCutSE() = CutModel(cutSE = true, relInsetDownFace = THICK_INSET)
internal fun makeThickCeilingVCutSW() = CutModel(cutSW = true, relInsetDownFace = THICK_INSET)
internal fun makeThickFloor() = BlockModel(relInsetUpFace = THICK_INSET)
internal fun makeThickFloorVCutNE() = CutModel(cutNE = true, relInsetUpFace = THICK_INSET)
internal fun makeThickFloorVCutNW() = CutModel(cutNW = true, relInsetUpFace = THICK_INSET)
internal fun makeThickFloorVCutSE() = CutModel(cutSE = true, relInsetUpFace = THICK_INSET)
internal fun makeThickFloorVCutSW() = CutModel(cutSW = true, relInsetUpFace = THICK_INSET)
internal fun makeThickHalfHHighBarE() = BlockModel(relInsetDownFace = 0.5f, relInsetWestFace = THICK_INSET)
internal fun makeThickHalfHHighBarN() = BlockModel(relInsetDownFace = 0.5f, relInsetSouthFace = THICK_INSET)
internal fun makeThickHalfHHighBarS() = BlockModel(relInsetDownFace = 0.5f, relInsetNorthFace = THICK_INSET)
internal fun makeThickHalfHHighBarW() = BlockModel(relInsetDownFace = 0.5f, relInsetEastFace = THICK_INSET)
internal fun makeThickHalfHLowBarE() = BlockModel(relInsetUpFace = 0.5f, relInsetWestFace = THICK_INSET)
internal fun makeThickHalfHLowBarN() = BlockModel(relInsetUpFace = 0.5f, relInsetSouthFace = THICK_INSET)
internal fun makeThickHalfHLowBarS() = BlockModel(relInsetUpFace = 0.5f, relInsetNorthFace = THICK_INSET)
internal fun makeThickHalfHLowBarW() = BlockModel(relInsetUpFace = 0.5f, relInsetEastFace = THICK_INSET)
internal fun makeThickHighBarE() = BlockModel(relInsetDownFace = THICK_INSET, relInsetWestFace = THICK_INSET)
internal fun makeThickHighBarN() = BlockModel(relInsetDownFace = THICK_INSET, relInsetSouthFace = THICK_INSET)
internal fun makeThickHighBarS() = BlockModel(relInsetDownFace = THICK_INSET, relInsetNorthFace = THICK_INSET)
internal fun makeThickHighBarW() = BlockModel(relInsetDownFace = THICK_INSET, relInsetEastFace = THICK_INSET)
internal fun makeThickWallCornerNE() = WallCornerNEModel(relInset = THICK_INSET, ceiling = false)
internal fun makeThickWallCornerNW() = WallCornerNWModel(relInset = THICK_INSET, ceiling = false)
internal fun makeThickWallCornerSE() = WallCornerSEModel(relInset = THICK_INSET, ceiling = false)
internal fun makeThickWallCornerSW() = WallCornerSWModel(relInset = THICK_INSET, ceiling = false)
internal fun makeThickWallEast() = BlockModel(relInsetWestFace = THICK_INSET)
internal fun makeThickWallNorth() = BlockModel(relInsetSouthFace = THICK_INSET)
internal fun makeThickWallSouth() = BlockModel(relInsetNorthFace = THICK_INSET)
internal fun makeThickWallTopEast() = WallTopEastModel(relInset = THICK_INSET)
internal fun makeThickWallTopNorth() = WallTopNorthModel(relInset = THICK_INSET)
internal fun makeThickWallTopSouth() = WallTopSouthModel(relInset = THICK_INSET)
internal fun makeThickWallTopWest() = WallTopWestModel(relInset = THICK_INSET)
internal fun makeThickWallWest() = BlockModel(relInsetEastFace = THICK_INSET)
internal fun makeThinWallCornerNE() = WallCornerNEModel(relInset = THIN_INSET, ceiling = false)
internal fun makeThinWallCornerNW() = WallCornerNWModel(relInset = THIN_INSET, ceiling = false)
internal fun makeThinWallCornerSE() = WallCornerSEModel(relInset = THIN_INSET, ceiling = false)
internal fun makeThinWallCornerSW() = WallCornerSWModel(relInset = THIN_INSET, ceiling = false)
internal fun makeThinWallCornerTopNE() = WallCornerNEModel(relInset = THIN_INSET, ceiling = true)
internal fun makeThinWallCornerTopNW() = WallCornerNWModel(relInset = THIN_INSET, ceiling = true)
internal fun makeThinWallCornerTopSE() = WallCornerSEModel(relInset = THIN_INSET, ceiling = true)
internal fun makeThinWallCornerTopSW() = WallCornerSWModel(relInset = THIN_INSET, ceiling = true)
internal fun makeThinWallEast() = BlockModel(relInsetWestFace = THIN_INSET)
internal fun makeThinWallNorth() = BlockModel(relInsetSouthFace = THIN_INSET)
internal fun makeThinWallSouth() = BlockModel(relInsetNorthFace = THIN_INSET)
internal fun makeThinWallTopEast() = WallTopEastModel(relInset = THIN_INSET)
internal fun makeThinWallTopNorth() = WallTopNorthModel(relInset = THIN_INSET)
internal fun makeThinWallTopSouth() = WallTopSouthModel(relInset = THIN_INSET)
internal fun makeThinWallTopWest() = WallTopWestModel(relInset = THIN_INSET)
internal fun makeThinWallWest() = BlockModel(relInsetEastFace = THIN_INSET)
internal fun makeUprightBarNE() = BlockModel(relInsetSouthFace = THIN_INSET, relInsetWestFace = THIN_INSET)
internal fun makeUprightBarNW() = BlockModel(relInsetSouthFace = THIN_INSET, relInsetEastFace = THIN_INSET)
internal fun makeUprightBarSE() = BlockModel(relInsetNorthFace = THIN_INSET, relInsetWestFace = THIN_INSET)
internal fun makeUprightBarSW() = BlockModel(relInsetNorthFace = THIN_INSET, relInsetEastFace = THIN_INSET)
internal fun makeVerticalBevelNorth() = CutModel(cutSize = BEVEL_CUT_SIZE, cutNE = true, cutNW = true)
internal fun makeVerticalBevelEast() = CutModel(cutSize = BEVEL_CUT_SIZE, cutNE = true, cutSE = true)
internal fun makeVerticalBevelSouth() = CutModel(cutSize = BEVEL_CUT_SIZE, cutSE = true, cutSW = true)
internal fun makeVerticalBevelWest() = CutModel(cutSize = BEVEL_CUT_SIZE, cutSW = true, cutNW = true)
internal fun makeVerticalBevelNE() = CutModel(cutSize = BEVEL_CUT_SIZE, cutNE = true)
internal fun makeVerticalBevelNW() = CutModel(cutSize = BEVEL_CUT_SIZE, cutNW = true)
internal fun makeVerticalBevelSE() = CutModel(cutSize = BEVEL_CUT_SIZE, cutSE = true)
internal fun makeVerticalBevelSW() = CutModel(cutSize = BEVEL_CUT_SIZE, cutSW = true)
internal fun makeVerticalCutNE() = CutModel(cutNE = true)
internal fun makeVerticalCutNW() = CutModel(cutNW = true)
internal fun makeVerticalCutSE() = CutModel(cutSE = true)
internal fun makeVerticalCutSW() = CutModel(cutSW = true)

internal fun makeVerticalBevelFull() =
    CutModel(cutSize = BEVEL_CUT_SIZE, cutNE = true, cutNW = true, cutSE = true, cutSW = true)

internal fun makeVerticalBevelFullAlt() =
    CutModel(cutSize = BEVEL_CUT_ALT_SIZE, cutNE = true, cutNW = true, cutSE = true, cutSW = true)

internal fun makeLowBarNS() =
    BlockModel(relInsetUpFace = THIN_INSET, relInsetEastFace = THIN_INSET * 0.5f, relInsetWestFace = THIN_INSET * 0.5f)

internal fun makeLowBarWE() =
    BlockModel(
        relInsetUpFace = THIN_INSET,
        relInsetNorthFace = THIN_INSET * 0.5f,
        relInsetSouthFace = THIN_INSET * 0.5f
    )

internal fun makeWindowTopLNorth() =
    BlockModel(relInsetUpFace = WINDOW_TOP_INSET, relInsetSouthFace = THIN_INSET, upFaceWESlope = -WINDOW_TOP_SLOPE)

internal fun makeWindowTopLEast() =
    BlockModel(relInsetUpFace = WINDOW_TOP_INSET, relInsetWestFace = THIN_INSET, upFaceNSSlope = -WINDOW_TOP_SLOPE)

internal fun makeWindowTopLSouth() =
    BlockModel(relInsetUpFace = WINDOW_TOP_INSET, relInsetNorthFace = THIN_INSET, upFaceWESlope = WINDOW_TOP_SLOPE)

internal fun makeWindowTopLWest() =
    BlockModel(relInsetUpFace = WINDOW_TOP_INSET, relInsetEastFace = THIN_INSET, upFaceNSSlope = WINDOW_TOP_SLOPE)

internal fun makeWindowTopRNorth() =
    BlockModel(relInsetUpFace = WINDOW_TOP_INSET, relInsetSouthFace = THIN_INSET, upFaceWESlope = WINDOW_TOP_SLOPE)

internal fun makeWindowTopREast() =
    BlockModel(relInsetUpFace = WINDOW_TOP_INSET, relInsetWestFace = THIN_INSET, upFaceNSSlope = WINDOW_TOP_SLOPE)

internal fun makeWindowTopRSouth() =
    BlockModel(relInsetUpFace = WINDOW_TOP_INSET, relInsetNorthFace = THIN_INSET, upFaceWESlope = -WINDOW_TOP_SLOPE)

internal fun makeWindowTopRWest() =
    BlockModel(relInsetUpFace = WINDOW_TOP_INSET, relInsetEastFace = THIN_INSET, upFaceNSSlope = -WINDOW_TOP_SLOPE)

internal fun makeStraightStairsNorth() =
    StraightStairsModel(lowEdge = Side.SOUTH, numSteps = FULL_STRAIGHT_STAIRS_NUM_STEPS)

internal fun makeStraightStairsEast() =
    StraightStairsModel(lowEdge = Side.WEST, numSteps = FULL_STRAIGHT_STAIRS_NUM_STEPS)

internal fun makeStraightStairsSouth() =
    StraightStairsModel(lowEdge = Side.NORTH, numSteps = FULL_STRAIGHT_STAIRS_NUM_STEPS)

internal fun makeStraightStairsWest() =
    StraightStairsModel(lowEdge = Side.EAST, numSteps = FULL_STRAIGHT_STAIRS_NUM_STEPS)

internal fun makeWindingStairsEN() =
    WindingStairsModel(lowEdge = Side.EAST, numSteps = FULL_WINDING_STAIRS_NUM_STEPS, ccw = false)

internal fun makeWindingStairsNW() =
    WindingStairsModel(lowEdge = Side.NORTH, numSteps = FULL_WINDING_STAIRS_NUM_STEPS, ccw = false)

internal fun makeWindingStairsSE() =
    WindingStairsModel(lowEdge = Side.SOUTH, numSteps = FULL_WINDING_STAIRS_NUM_STEPS, ccw = false)

internal fun makeWindingStairsWS() =
    WindingStairsModel(lowEdge = Side.WEST, numSteps = FULL_WINDING_STAIRS_NUM_STEPS, ccw = false)

internal fun makeWindingStairsES() =
    WindingStairsModel(lowEdge = Side.EAST, numSteps = FULL_WINDING_STAIRS_NUM_STEPS, ccw = true)

internal fun makeWindingStairsNE() =
    WindingStairsModel(lowEdge = Side.NORTH, numSteps = FULL_WINDING_STAIRS_NUM_STEPS, ccw = true)

internal fun makeWindingStairsSW() =
    WindingStairsModel(lowEdge = Side.SOUTH, numSteps = FULL_WINDING_STAIRS_NUM_STEPS, ccw = true)

internal fun makeWindingStairsWN() =
    WindingStairsModel(lowEdge = Side.WEST, numSteps = FULL_WINDING_STAIRS_NUM_STEPS, ccw = true)

internal fun makeWindingStairsLoEN() =
    WindingStairsModel(
        lowEdge = Side.EAST,
        numSteps = HALF_WINDING_STAIRS_NUM_STEPS,
        ccw = false,
        relInsetUpFace = 0.5f
    )

internal fun makeWindingStairsLoNW() =
    WindingStairsModel(
        lowEdge = Side.NORTH,
        numSteps = HALF_WINDING_STAIRS_NUM_STEPS,
        ccw = false,
        relInsetUpFace = 0.5f
    )

internal fun makeWindingStairsLoSE() =
    WindingStairsModel(
        lowEdge = Side.SOUTH,
        numSteps = HALF_WINDING_STAIRS_NUM_STEPS,
        ccw = false,
        relInsetUpFace = 0.5f
    )

internal fun makeWindingStairsLoWS() =
    WindingStairsModel(
        lowEdge = Side.WEST,
        numSteps = HALF_WINDING_STAIRS_NUM_STEPS,
        ccw = false,
        relInsetUpFace = 0.5f
    )

internal fun makeWindingStairsLoES() =
    WindingStairsModel(
        lowEdge = Side.EAST,
        numSteps = HALF_WINDING_STAIRS_NUM_STEPS,
        ccw = true,
        relInsetUpFace = 0.5f
    )

internal fun makeWindingStairsLoNE() =
    WindingStairsModel(
        lowEdge = Side.NORTH,
        numSteps = HALF_WINDING_STAIRS_NUM_STEPS,
        ccw = true,
        relInsetUpFace = 0.5f
    )

internal fun makeWindingStairsLoSW() =
    WindingStairsModel(
        lowEdge = Side.SOUTH,
        numSteps = HALF_WINDING_STAIRS_NUM_STEPS,
        ccw = true,
        relInsetUpFace = 0.5f
    )

internal fun makeWindingStairsLoWN() =
    WindingStairsModel(
        lowEdge = Side.WEST,
        numSteps = HALF_WINDING_STAIRS_NUM_STEPS,
        ccw = true,
        relInsetUpFace = 0.5f
    )

internal fun makeWindingStairsHiEN() =
    WindingStairsModel(
        lowEdge = Side.EAST,
        numSteps = HALF_WINDING_STAIRS_NUM_STEPS,
        ccw = false,
        relInsetDownFace = 0.5f
    )

internal fun makeWindingStairsHiNW() =
    WindingStairsModel(
        lowEdge = Side.NORTH,
        numSteps = HALF_WINDING_STAIRS_NUM_STEPS,
        ccw = false,
        relInsetDownFace = 0.5f
    )

internal fun makeWindingStairsHiSE() =
    WindingStairsModel(
        lowEdge = Side.SOUTH,
        numSteps = HALF_WINDING_STAIRS_NUM_STEPS,
        ccw = false,
        relInsetDownFace = 0.5f
    )

internal fun makeWindingStairsHiWS() =
    WindingStairsModel(
        lowEdge = Side.WEST,
        numSteps = HALF_WINDING_STAIRS_NUM_STEPS,
        ccw = false,
        relInsetDownFace = 0.5f
    )

internal fun makeWindingStairsHiES() =
    WindingStairsModel(
        lowEdge = Side.EAST,
        numSteps = HALF_WINDING_STAIRS_NUM_STEPS,
        ccw = true,
        relInsetDownFace = 0.5f
    )

internal fun makeWindingStairsHiNE() =
    WindingStairsModel(
        lowEdge = Side.NORTH,
        numSteps = HALF_WINDING_STAIRS_NUM_STEPS,
        ccw = true,
        relInsetDownFace = 0.5f
    )

internal fun makeWindingStairsHiSW() =
    WindingStairsModel(
        lowEdge = Side.SOUTH,
        numSteps = HALF_WINDING_STAIRS_NUM_STEPS,
        ccw = true,
        relInsetDownFace = 0.5f
    )

internal fun makeWindingStairsHiWN() =
    WindingStairsModel(
        lowEdge = Side.WEST,
        numSteps = HALF_WINDING_STAIRS_NUM_STEPS,
        ccw = true,
        relInsetDownFace = 0.5f
    )
