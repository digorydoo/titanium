package ch.digorydoo.titanium.engine.editor.items

import ch.digorydoo.kutils.string.initCap
import ch.digorydoo.titanium.engine.brick.BrickShape
import ch.digorydoo.titanium.engine.brick.BrickShape.*

internal enum class BrickShapeGroup {
    ROOT,
    BARS,
    BAR_FRAMES,
    BEVELS_AND_CUTS,
    FLAT_WALLS,
    FLOORS_AND_CEILINGS,
    HALFH_CEILING_V_CUTS,
    HALFH_FLOOR_V_CUTS,
    HALF_BRICK_WALLS,
    HALF_BRICK_WALLS_REGULAR,
    HALF_BRICK_WALLS_TOP,
    HALF_BRICK_WALL_CORNERS,
    HALF_BRICK_WALL_CORNERS_TOP,
    HALF_RAMPS,
    HIGH_BARS,
    INVERSE_RAMP_RUNS,
    LOW_BARS,
    PILLARS,
    RAMPS,
    RAMP_CORNERS,
    RAMP_RUNS,
    STAIRS,
    STAIR_RUNS,
    STRAIGHT_STAIRS,
    THICK_CEILING_V_CUTS,
    THICK_FLOOR_V_CUTS,
    THICK_WALLS,
    THICK_WALLS_REGULAR,
    THICK_WALLS_TOP,
    THICK_WALL_CORNERS,
    THICK_WALL_CORNERS_TOP,
    THIN_WALLS,
    THIN_WALLS_DIAGONAL,
    THIN_WALLS_REGULAR,
    THIN_WALLS_TOP,
    THIN_WALL_CORNERS,
    THIN_WALL_CORNERS_TOP,
    TOP_BEVELS,
    UPRIGHT_BARS,
    VERTICAL_BEVELS,
    VERTICAL_CUTS_REGULAR,
    WALLS,
    WINDING_STAIRS,
    WINDING_STAIRS_FULL,
    WINDING_STAIRS_LOWER_HALF,
    WINDING_STAIRS_UPPER_HALF,
    WINDOWS,
    ;

    val displayText = toString().split("_").joinToString(" ") {
        when {
            it.length < 3 -> it.uppercase()
            else -> initCap(it.lowercase())
        }
    }

    fun findParent() =
        entries.firstOrNull { it.subgroups().contains(this) }

    fun findFirstContaining(grp: BrickShapeGroup, recursion: Int = 0): BrickShapeGroup? {
        require(recursion < MAX_NESTING_LEVEL) { "BrickShapeGroups are nested too deep: $this" }
        val subgroups = subgroups()
        if (subgroups.contains(grp)) return this
        else return subgroups.firstOrNull { it.findFirstContaining(grp, recursion + 1) != null }
    }

    fun findFirstContaining(shape: BrickShape, recursion: Int = 0): BrickShapeGroup? {
        require(recursion < MAX_NESTING_LEVEL) { "BrickShapeGroups are nested too deep: $this" }
        if (shapes().contains(shape)) return this
        else return subgroups().firstOrNull { it.findFirstContaining(shape, recursion + 1) != null }
    }

    fun subgroups() = when (this) {
        ROOT -> listOf(
            BARS,
            BEVELS_AND_CUTS,
            FLOORS_AND_CEILINGS,
            PILLARS,
            RAMPS,
            STAIRS,
            WALLS,
            WINDOWS,
        )
        BARS -> listOf(
            LOW_BARS,
            HIGH_BARS,
            UPRIGHT_BARS,
            BAR_FRAMES,
        )
        BEVELS_AND_CUTS -> listOf(
            TOP_BEVELS,
            VERTICAL_BEVELS,
            VERTICAL_CUTS_REGULAR,
            HALFH_CEILING_V_CUTS,
            HALFH_FLOOR_V_CUTS,
            THICK_CEILING_V_CUTS,
            THICK_FLOOR_V_CUTS,
        )
        HALF_BRICK_WALLS -> listOf(
            HALF_BRICK_WALLS_REGULAR,
            HALF_BRICK_WALLS_TOP,
            HALF_BRICK_WALL_CORNERS,
            HALF_BRICK_WALL_CORNERS_TOP,
        )
        RAMPS -> listOf(
            HALF_RAMPS,
            RAMP_RUNS,
            INVERSE_RAMP_RUNS,
            RAMP_CORNERS,
        )
        STAIRS -> listOf(
            STAIR_RUNS,
            STRAIGHT_STAIRS,
            WINDING_STAIRS,
        )
        THICK_WALLS -> listOf(
            THICK_WALLS_REGULAR,
            THICK_WALLS_TOP,
            THICK_WALL_CORNERS,
            THICK_WALL_CORNERS_TOP,
        )
        THIN_WALLS -> listOf(
            THIN_WALLS_DIAGONAL,
            THIN_WALLS_REGULAR,
            THIN_WALLS_TOP,
            THIN_WALL_CORNERS,
            THIN_WALL_CORNERS_TOP,
        )
        WALLS -> listOf(
            FLAT_WALLS,
            HALF_BRICK_WALLS,
            THICK_WALLS,
            THIN_WALLS,
        )
        WINDING_STAIRS -> listOf(
            WINDING_STAIRS_FULL,
            WINDING_STAIRS_LOWER_HALF,
            WINDING_STAIRS_UPPER_HALF,
        )
        else -> listOf()
    }

    fun shapes() = when (this) {
        ROOT -> listOf(BASIC_BLOCK)
        BAR_FRAMES -> listOf(
            BAR_FRAME_EAST,
            BAR_FRAME_NORTH,
            BAR_FRAME_SOUTH,
            BAR_FRAME_WEST,
        )
        FLAT_WALLS -> listOf(
            FLAT_WALL_EAST,
            FLAT_WALL_NORTH,
            FLAT_WALL_SOUTH,
            FLAT_WALL_WEST,
        )
        FLOORS_AND_CEILINGS -> listOf(
            FLAT_CEILING,
            FLAT_FLOOR,
            HALFH_CEILING,
            HALFH_CEILING_V_CUT_NE,
            HALFH_CEILING_V_CUT_NW,
            HALFH_CEILING_V_CUT_SE,
            HALFH_CEILING_V_CUT_SW,
            HALFH_FLOOR,
            THICK_CEILING,
            THICK_CEILING_V_CUT_NE,
            THICK_CEILING_V_CUT_NW,
            THICK_CEILING_V_CUT_SE,
            THICK_CEILING_V_CUT_SW,
            THICK_FLOOR,
        )
        HALF_BRICK_WALLS_REGULAR -> listOf(
            HALFD_WALL_EAST,
            HALFD_WALL_NORTH,
            HALFD_WALL_SOUTH,
            HALFD_WALL_WEST,
        )
        HALF_BRICK_WALLS_TOP -> listOf(
            HALFD_WALL_TOP_EAST,
            HALFD_WALL_TOP_NORTH,
            HALFD_WALL_TOP_SOUTH,
            HALFD_WALL_TOP_WEST,
        )
        HALF_BRICK_WALL_CORNERS -> listOf(
            HALFD_WALL_CORNER_NE,
            HALFD_WALL_CORNER_NW,
            HALFD_WALL_CORNER_SE,
            HALFD_WALL_CORNER_SW,
        )
        HALF_BRICK_WALL_CORNERS_TOP -> listOf(
            HALFD_WALL_CORNER_TOP_NE,
            HALFD_WALL_CORNER_TOP_NW,
            HALFD_WALL_CORNER_TOP_SE,
            HALFD_WALL_CORNER_TOP_SW,
        )
        HALF_RAMPS -> listOf(
            HALF_RAMP_TOP_EAST,
            HALF_RAMP_TOP_NORTH,
            HALF_RAMP_TOP_SOUTH,
            HALF_RAMP_TOP_WEST,
        )
        HIGH_BARS -> listOf(
            HALFD_HIGH_BAR_EAST,
            HALFD_HIGH_BAR_NORTH,
            HALFD_HIGH_BAR_SOUTH,
            HALFD_HIGH_BAR_WEST,
            HIGH_BAR_EAST,
            HIGH_BAR_NORTH,
            HIGH_BAR_SOUTH,
            HIGH_BAR_WEST,
            THICK_HALFH_HIGH_BAR_EAST,
            THICK_HALFH_HIGH_BAR_NORTH,
            THICK_HALFH_HIGH_BAR_SOUTH,
            THICK_HALFH_HIGH_BAR_WEST,
            THICK_HIGH_BAR_EAST,
            THICK_HIGH_BAR_NORTH,
            THICK_HIGH_BAR_SOUTH,
            THICK_HIGH_BAR_WEST,
        )
        INVERSE_RAMP_RUNS -> listOf(
            INVERSE_RAMP_RUN_EAST,
            INVERSE_RAMP_RUN_NORTH,
            INVERSE_RAMP_RUN_SOUTH,
            INVERSE_RAMP_RUN_WEST,
        )
        LOW_BARS -> listOf(
            LOW_BAR_EAST,
            LOW_BAR_NORTH,
            LOW_BAR_NS,
            LOW_BAR_SOUTH,
            LOW_BAR_WE,
            LOW_BAR_WEST,
            THICK_HALFH_LOW_BAR_NORTH,
            THICK_HALFH_LOW_BAR_EAST,
            THICK_HALFH_LOW_BAR_SOUTH,
            THICK_HALFH_LOW_BAR_WEST,
        )
        PILLARS -> listOf(
            OCTAGONAL_UPRIGHT_PILLAR,
            VERTICAL_BEVEL_FULL,
            VERTICAL_BEVEL_FULL_ALT,
        )
        RAMP_CORNERS -> listOf(
            RAMP_CORNER_NE,
            RAMP_CORNER_NE_ALT,
            RAMP_CORNER_NW,
            RAMP_CORNER_NW_ALT,
            RAMP_CORNER_SE,
            RAMP_CORNER_SE_ALT,
            RAMP_CORNER_SW,
            RAMP_CORNER_SW_ALT,
        )
        RAMP_RUNS -> listOf(
            RAMP_RUN_EAST,
            RAMP_RUN_NORTH,
            RAMP_RUN_SOUTH,
            RAMP_RUN_WEST,
        )
        STAIR_RUNS -> listOf(
            STAIRS_RUN_EAST,
            STAIRS_RUN_NORTH,
            STAIRS_RUN_SOUTH,
            STAIRS_RUN_WEST,
        )
        STRAIGHT_STAIRS -> listOf(
            STRAIGHT_STAIRS_EAST,
            STRAIGHT_STAIRS_NORTH,
            STRAIGHT_STAIRS_SOUTH,
            STRAIGHT_STAIRS_WEST,
        )
        THICK_WALLS_REGULAR -> listOf(
            THICK_WALL_EAST,
            THICK_WALL_NORTH,
            THICK_WALL_SOUTH,
            THICK_WALL_WEST,
        )
        THICK_WALLS_TOP -> listOf(
            THICK_WALL_TOP_EAST,
            THICK_WALL_TOP_NORTH,
            THICK_WALL_TOP_SOUTH,
            THICK_WALL_TOP_WEST,
        )
        THICK_WALL_CORNERS -> listOf(
            THICK_WALL_CORNER_NE,
            THICK_WALL_CORNER_NW,
            THICK_WALL_CORNER_SE,
            THICK_WALL_CORNER_SW,
        )
        THICK_WALL_CORNERS_TOP -> listOf(
            THICK_WALL_CORNER_TOP_NE,
            THICK_WALL_CORNER_TOP_NW,
            THICK_WALL_CORNER_TOP_SE,
            THICK_WALL_CORNER_TOP_SW,
        )
        THIN_WALLS_DIAGONAL -> listOf(
            THIN_WALL_DIAGONAL_NE,
            THIN_WALL_DIAGONAL_NW,
            THIN_WALL_DIAGONAL_SE,
            THIN_WALL_DIAGONAL_SW,
        )
        THIN_WALLS_REGULAR -> listOf(
            THIN_WALL_EAST,
            THIN_WALL_NORTH,
            THIN_WALL_SOUTH,
            THIN_WALL_WEST,
        )
        THIN_WALLS_TOP -> listOf(
            THIN_WALL_TOP_EAST,
            THIN_WALL_TOP_NORTH,
            THIN_WALL_TOP_SOUTH,
            THIN_WALL_TOP_WEST,
        )
        THIN_WALL_CORNERS -> listOf(
            THIN_WALL_CORNER_NE,
            THIN_WALL_CORNER_NW,
            THIN_WALL_CORNER_SE,
            THIN_WALL_CORNER_SW,
        )
        THIN_WALL_CORNERS_TOP -> listOf(
            THIN_WALL_CORNER_TOP_NE,
            THIN_WALL_CORNER_TOP_NW,
            THIN_WALL_CORNER_TOP_SE,
            THIN_WALL_CORNER_TOP_SW,
        )
        TOP_BEVELS -> listOf(
            BEVEL_NORTH,
            BEVEL_EAST,
            BEVEL_SOUTH,
            BEVEL_WEST,
            BEVEL_EE,
            BEVEL_NE,
            BEVEL_NN,
            BEVEL_NS,
            BEVEL_NW,
            BEVEL_SE,
            BEVEL_SS,
            BEVEL_SW,
            BEVEL_WE,
            BEVEL_WW,
        )
        UPRIGHT_BARS -> listOf(
            UPRIGHT_BAR_NE,
            UPRIGHT_BAR_NW,
            UPRIGHT_BAR_SE,
            UPRIGHT_BAR_SW,
            UPRIGHT_DBL_BAR_EAST,
            UPRIGHT_DBL_BAR_NORTH,
            UPRIGHT_DBL_BAR_SOUTH,
            UPRIGHT_DBL_BAR_WEST,
        )
        VERTICAL_BEVELS -> listOf(
            VERTICAL_BEVEL_EAST,
            VERTICAL_BEVEL_FULL,
            VERTICAL_BEVEL_FULL_ALT,
            VERTICAL_BEVEL_NE,
            VERTICAL_BEVEL_NORTH,
            VERTICAL_BEVEL_NW,
            VERTICAL_BEVEL_SE,
            VERTICAL_BEVEL_SOUTH,
            VERTICAL_BEVEL_SW,
            VERTICAL_BEVEL_WEST,
        )
        VERTICAL_CUTS_REGULAR -> listOf(
            VERTICAL_CUT_NE,
            VERTICAL_CUT_NW,
            VERTICAL_CUT_SE,
            VERTICAL_CUT_SW,
        )
        HALFH_CEILING_V_CUTS -> listOf(
            HALFH_CEILING_V_CUT_NE,
            HALFH_CEILING_V_CUT_NW,
            HALFH_CEILING_V_CUT_SE,
            HALFH_CEILING_V_CUT_SW,
        )
        HALFH_FLOOR_V_CUTS -> listOf(
            HALFH_FLOOR_V_CUT_NE,
            HALFH_FLOOR_V_CUT_NW,
            HALFH_FLOOR_V_CUT_SE,
            HALFH_FLOOR_V_CUT_SW,
        )
        THICK_CEILING_V_CUTS -> listOf(
            THICK_CEILING_V_CUT_NE,
            THICK_CEILING_V_CUT_NW,
            THICK_CEILING_V_CUT_SE,
            THICK_CEILING_V_CUT_SW,
        )
        THICK_FLOOR_V_CUTS -> listOf(
            THICK_FLOOR_V_CUT_NE,
            THICK_FLOOR_V_CUT_NW,
            THICK_FLOOR_V_CUT_SE,
            THICK_FLOOR_V_CUT_SW,
        )
        WINDING_STAIRS_FULL -> listOf(
            WINDING_STAIRS_EN,
            WINDING_STAIRS_ES,
            WINDING_STAIRS_NE,
            WINDING_STAIRS_NW,
            WINDING_STAIRS_SE,
            WINDING_STAIRS_SW,
            WINDING_STAIRS_WN,
            WINDING_STAIRS_WS,
        )
        WINDING_STAIRS_LOWER_HALF -> listOf(
            WINDING_STAIRS_LOWER_EN,
            WINDING_STAIRS_LOWER_ES,
            WINDING_STAIRS_LOWER_NE,
            WINDING_STAIRS_LOWER_NW,
            WINDING_STAIRS_LOWER_SE,
            WINDING_STAIRS_LOWER_SW,
            WINDING_STAIRS_LOWER_WN,
            WINDING_STAIRS_LOWER_WS,
        )
        WINDING_STAIRS_UPPER_HALF -> listOf(
            WINDING_STAIRS_UPPER_EN,
            WINDING_STAIRS_UPPER_ES,
            WINDING_STAIRS_UPPER_NE,
            WINDING_STAIRS_UPPER_NW,
            WINDING_STAIRS_UPPER_SE,
            WINDING_STAIRS_UPPER_SW,
            WINDING_STAIRS_UPPER_WN,
            WINDING_STAIRS_UPPER_WS,
        )
        WINDOWS -> listOf(
            WINDOW_TOP_L_EAST,
            WINDOW_TOP_L_NORTH,
            WINDOW_TOP_L_SOUTH,
            WINDOW_TOP_L_WEST,
            WINDOW_TOP_R_EAST,
            WINDOW_TOP_R_NORTH,
            WINDOW_TOP_R_SOUTH,
            WINDOW_TOP_R_WEST,
        )
        else -> listOf()
    }

    companion object {
        private const val MAX_NESTING_LEVEL = 4

        fun BrickShape.findFirstInnermostGroup() =
            entries.firstOrNull { it.shapes().contains(this) }
    }
}
