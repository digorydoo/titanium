package ch.digorydoo.titanium.engine.editor.menu.shape

import ch.digorydoo.kutils.string.initCap
import ch.digorydoo.titanium.engine.brick.BrickShape

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
    THICK_CEILING_V_CUTS,
    THICK_FLOOR_V_CUTS,
    THICK_STAIRS,
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
            THICK_STAIRS,
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
        ROOT -> listOf(BrickShape.BASIC_BLOCK)
        BAR_FRAMES -> listOf(
            BrickShape.BAR_FRAME_EAST,
            BrickShape.BAR_FRAME_NORTH,
            BrickShape.BAR_FRAME_SOUTH,
            BrickShape.BAR_FRAME_WEST,
        )
        FLAT_WALLS -> listOf(
            BrickShape.FLAT_WALL_EAST,
            BrickShape.FLAT_WALL_NORTH,
            BrickShape.FLAT_WALL_SOUTH,
            BrickShape.FLAT_WALL_WEST,
        )
        FLOORS_AND_CEILINGS -> listOf(
            BrickShape.FLAT_CEILING,
            BrickShape.FLAT_FLOOR,
            BrickShape.HALFH_CEILING,
            BrickShape.HALFH_CEILING_V_CUT_NE,
            BrickShape.HALFH_CEILING_V_CUT_NW,
            BrickShape.HALFH_CEILING_V_CUT_SE,
            BrickShape.HALFH_CEILING_V_CUT_SW,
            BrickShape.HALFH_FLOOR,
            BrickShape.THICK_CEILING,
            BrickShape.THICK_CEILING_V_CUT_NE,
            BrickShape.THICK_CEILING_V_CUT_NW,
            BrickShape.THICK_CEILING_V_CUT_SE,
            BrickShape.THICK_CEILING_V_CUT_SW,
            BrickShape.THICK_FLOOR,
        )
        HALF_BRICK_WALLS_REGULAR -> listOf(
            BrickShape.HALFD_WALL_EAST,
            BrickShape.HALFD_WALL_NORTH,
            BrickShape.HALFD_WALL_SOUTH,
            BrickShape.HALFD_WALL_WEST,
        )
        HALF_BRICK_WALLS_TOP -> listOf(
            BrickShape.HALFD_WALL_TOP_EAST,
            BrickShape.HALFD_WALL_TOP_NORTH,
            BrickShape.HALFD_WALL_TOP_SOUTH,
            BrickShape.HALFD_WALL_TOP_WEST,
        )
        HALF_BRICK_WALL_CORNERS -> listOf(
            BrickShape.HALFD_WALL_CORNER_NE,
            BrickShape.HALFD_WALL_CORNER_NW,
            BrickShape.HALFD_WALL_CORNER_SE,
            BrickShape.HALFD_WALL_CORNER_SW,
        )
        HALF_BRICK_WALL_CORNERS_TOP -> listOf(
            BrickShape.HALFD_WALL_CORNER_TOP_NE,
            BrickShape.HALFD_WALL_CORNER_TOP_NW,
            BrickShape.HALFD_WALL_CORNER_TOP_SE,
            BrickShape.HALFD_WALL_CORNER_TOP_SW,
        )
        HALF_RAMPS -> listOf(
            BrickShape.HALF_RAMP_TOP_EAST,
            BrickShape.HALF_RAMP_TOP_NORTH,
            BrickShape.HALF_RAMP_TOP_SOUTH,
            BrickShape.HALF_RAMP_TOP_WEST,
        )
        HIGH_BARS -> listOf(
            BrickShape.HALFD_HIGH_BAR_EAST,
            BrickShape.HALFD_HIGH_BAR_NORTH,
            BrickShape.HALFD_HIGH_BAR_SOUTH,
            BrickShape.HALFD_HIGH_BAR_WEST,
            BrickShape.HIGH_BAR_EAST,
            BrickShape.HIGH_BAR_NORTH,
            BrickShape.HIGH_BAR_SOUTH,
            BrickShape.HIGH_BAR_WEST,
            BrickShape.THICK_HALFH_HIGH_BAR_EAST,
            BrickShape.THICK_HALFH_HIGH_BAR_NORTH,
            BrickShape.THICK_HALFH_HIGH_BAR_SOUTH,
            BrickShape.THICK_HALFH_HIGH_BAR_WEST,
            BrickShape.THICK_HIGH_BAR_EAST,
            BrickShape.THICK_HIGH_BAR_NORTH,
            BrickShape.THICK_HIGH_BAR_SOUTH,
            BrickShape.THICK_HIGH_BAR_WEST,
        )
        INVERSE_RAMP_RUNS -> listOf(
            BrickShape.INVERSE_RAMP_RUN_EAST,
            BrickShape.INVERSE_RAMP_RUN_NORTH,
            BrickShape.INVERSE_RAMP_RUN_SOUTH,
            BrickShape.INVERSE_RAMP_RUN_WEST,
        )
        LOW_BARS -> listOf(
            BrickShape.LOW_BAR_EAST,
            BrickShape.LOW_BAR_NORTH,
            BrickShape.LOW_BAR_NS,
            BrickShape.LOW_BAR_SOUTH,
            BrickShape.LOW_BAR_WE,
            BrickShape.LOW_BAR_WEST,
            BrickShape.THICK_HALFH_LOW_BAR_NORTH,
            BrickShape.THICK_HALFH_LOW_BAR_EAST,
            BrickShape.THICK_HALFH_LOW_BAR_SOUTH,
            BrickShape.THICK_HALFH_LOW_BAR_WEST,
        )
        PILLARS -> listOf(
            BrickShape.OCTAGONAL_UPRIGHT_PILLAR,
            BrickShape.VERTICAL_BEVEL_FULL,
            BrickShape.VERTICAL_BEVEL_FULL_ALT,
        )
        RAMP_CORNERS -> listOf(
            BrickShape.RAMP_CORNER_NE,
            BrickShape.RAMP_CORNER_NE_ALT,
            BrickShape.RAMP_CORNER_NW,
            BrickShape.RAMP_CORNER_NW_ALT,
            BrickShape.RAMP_CORNER_SE,
            BrickShape.RAMP_CORNER_SE_ALT,
            BrickShape.RAMP_CORNER_SW,
            BrickShape.RAMP_CORNER_SW_ALT,
        )
        RAMP_RUNS -> listOf(
            BrickShape.RAMP_RUN_EAST,
            BrickShape.RAMP_RUN_NORTH,
            BrickShape.RAMP_RUN_SOUTH,
            BrickShape.RAMP_RUN_WEST,
        )
        THICK_STAIRS -> listOf(
            BrickShape.THICK_STAIRS_11_EAST,
            BrickShape.THICK_STAIRS_11_NORTH,
            BrickShape.THICK_STAIRS_11_SOUTH,
            BrickShape.THICK_STAIRS_11_WEST,
            BrickShape.THICK_STAIRS_32_LOWER_EAST,
            BrickShape.THICK_STAIRS_32_LOWER_NORTH,
            BrickShape.THICK_STAIRS_32_LOWER_SOUTH,
            BrickShape.THICK_STAIRS_32_LOWER_WEST,
            BrickShape.THICK_STAIRS_32_UPPER_EAST,
            BrickShape.THICK_STAIRS_32_UPPER_NORTH,
            BrickShape.THICK_STAIRS_32_UPPER_SOUTH,
            BrickShape.THICK_STAIRS_32_UPPER_WEST,
        )
        THICK_WALLS_REGULAR -> listOf(
            BrickShape.THICK_WALL_EAST,
            BrickShape.THICK_WALL_NORTH,
            BrickShape.THICK_WALL_SOUTH,
            BrickShape.THICK_WALL_WEST,
        )
        THICK_WALLS_TOP -> listOf(
            BrickShape.THICK_WALL_TOP_EAST,
            BrickShape.THICK_WALL_TOP_NORTH,
            BrickShape.THICK_WALL_TOP_SOUTH,
            BrickShape.THICK_WALL_TOP_WEST,
        )
        THICK_WALL_CORNERS -> listOf(
            BrickShape.THICK_WALL_CORNER_NE,
            BrickShape.THICK_WALL_CORNER_NW,
            BrickShape.THICK_WALL_CORNER_SE,
            BrickShape.THICK_WALL_CORNER_SW,
        )
        THICK_WALL_CORNERS_TOP -> listOf(
            BrickShape.THICK_WALL_CORNER_TOP_NE,
            BrickShape.THICK_WALL_CORNER_TOP_NW,
            BrickShape.THICK_WALL_CORNER_TOP_SE,
            BrickShape.THICK_WALL_CORNER_TOP_SW,
        )
        THIN_WALLS_DIAGONAL -> listOf(
            BrickShape.THIN_WALL_DIAGONAL_NE,
            BrickShape.THIN_WALL_DIAGONAL_NW,
            BrickShape.THIN_WALL_DIAGONAL_SE,
            BrickShape.THIN_WALL_DIAGONAL_SW,
        )
        THIN_WALLS_REGULAR -> listOf(
            BrickShape.THIN_WALL_EAST,
            BrickShape.THIN_WALL_NORTH,
            BrickShape.THIN_WALL_SOUTH,
            BrickShape.THIN_WALL_WEST,
        )
        THIN_WALLS_TOP -> listOf(
            BrickShape.THIN_WALL_TOP_EAST,
            BrickShape.THIN_WALL_TOP_NORTH,
            BrickShape.THIN_WALL_TOP_SOUTH,
            BrickShape.THIN_WALL_TOP_WEST,
        )
        THIN_WALL_CORNERS -> listOf(
            BrickShape.THIN_WALL_CORNER_NE,
            BrickShape.THIN_WALL_CORNER_NW,
            BrickShape.THIN_WALL_CORNER_SE,
            BrickShape.THIN_WALL_CORNER_SW,
        )
        THIN_WALL_CORNERS_TOP -> listOf(
            BrickShape.THIN_WALL_CORNER_TOP_NE,
            BrickShape.THIN_WALL_CORNER_TOP_NW,
            BrickShape.THIN_WALL_CORNER_TOP_SE,
            BrickShape.THIN_WALL_CORNER_TOP_SW,
        )
        TOP_BEVELS -> listOf(
            BrickShape.BEVEL_NORTH,
            BrickShape.BEVEL_EAST,
            BrickShape.BEVEL_SOUTH,
            BrickShape.BEVEL_WEST,
            BrickShape.BEVEL_EE,
            BrickShape.BEVEL_NE,
            BrickShape.BEVEL_NN,
            BrickShape.BEVEL_NS,
            BrickShape.BEVEL_NW,
            BrickShape.BEVEL_SE,
            BrickShape.BEVEL_SS,
            BrickShape.BEVEL_SW,
            BrickShape.BEVEL_WE,
            BrickShape.BEVEL_WW,
        )
        UPRIGHT_BARS -> listOf(
            BrickShape.UPRIGHT_BAR_NE,
            BrickShape.UPRIGHT_BAR_NW,
            BrickShape.UPRIGHT_BAR_SE,
            BrickShape.UPRIGHT_BAR_SW,
            BrickShape.UPRIGHT_DBL_BAR_EAST,
            BrickShape.UPRIGHT_DBL_BAR_NORTH,
            BrickShape.UPRIGHT_DBL_BAR_SOUTH,
            BrickShape.UPRIGHT_DBL_BAR_WEST,
        )
        VERTICAL_BEVELS -> listOf(
            BrickShape.VERTICAL_BEVEL_EAST,
            BrickShape.VERTICAL_BEVEL_FULL,
            BrickShape.VERTICAL_BEVEL_FULL_ALT,
            BrickShape.VERTICAL_BEVEL_NE,
            BrickShape.VERTICAL_BEVEL_NORTH,
            BrickShape.VERTICAL_BEVEL_NW,
            BrickShape.VERTICAL_BEVEL_SE,
            BrickShape.VERTICAL_BEVEL_SOUTH,
            BrickShape.VERTICAL_BEVEL_SW,
            BrickShape.VERTICAL_BEVEL_WEST,
        )
        VERTICAL_CUTS_REGULAR -> listOf(
            BrickShape.VERTICAL_CUT_NE,
            BrickShape.VERTICAL_CUT_NW,
            BrickShape.VERTICAL_CUT_SE,
            BrickShape.VERTICAL_CUT_SW,
        )
        HALFH_CEILING_V_CUTS -> listOf(
            BrickShape.HALFH_CEILING_V_CUT_NE,
            BrickShape.HALFH_CEILING_V_CUT_NW,
            BrickShape.HALFH_CEILING_V_CUT_SE,
            BrickShape.HALFH_CEILING_V_CUT_SW,
        )
        HALFH_FLOOR_V_CUTS -> listOf(
            BrickShape.HALFH_FLOOR_V_CUT_NE,
            BrickShape.HALFH_FLOOR_V_CUT_NW,
            BrickShape.HALFH_FLOOR_V_CUT_SE,
            BrickShape.HALFH_FLOOR_V_CUT_SW,
        )
        THICK_CEILING_V_CUTS -> listOf(
            BrickShape.THICK_CEILING_V_CUT_NE,
            BrickShape.THICK_CEILING_V_CUT_NW,
            BrickShape.THICK_CEILING_V_CUT_SE,
            BrickShape.THICK_CEILING_V_CUT_SW,
        )
        THICK_FLOOR_V_CUTS -> listOf(
            BrickShape.THICK_FLOOR_V_CUT_NE,
            BrickShape.THICK_FLOOR_V_CUT_NW,
            BrickShape.THICK_FLOOR_V_CUT_SE,
            BrickShape.THICK_FLOOR_V_CUT_SW,
        )
        WINDING_STAIRS_FULL -> listOf(
            BrickShape.WINDING_STAIRS_EN,
            BrickShape.WINDING_STAIRS_ES,
            BrickShape.WINDING_STAIRS_NE,
            BrickShape.WINDING_STAIRS_NW,
            BrickShape.WINDING_STAIRS_SE,
            BrickShape.WINDING_STAIRS_SW,
            BrickShape.WINDING_STAIRS_WN,
            BrickShape.WINDING_STAIRS_WS,
        )
        WINDING_STAIRS_LOWER_HALF -> listOf(
            BrickShape.WINDING_STAIRS_LOWER_EN,
            BrickShape.WINDING_STAIRS_LOWER_ES,
            BrickShape.WINDING_STAIRS_LOWER_NE,
            BrickShape.WINDING_STAIRS_LOWER_NW,
            BrickShape.WINDING_STAIRS_LOWER_SE,
            BrickShape.WINDING_STAIRS_LOWER_SW,
            BrickShape.WINDING_STAIRS_LOWER_WN,
            BrickShape.WINDING_STAIRS_LOWER_WS,
        )
        WINDING_STAIRS_UPPER_HALF -> listOf(
            BrickShape.WINDING_STAIRS_UPPER_EN,
            BrickShape.WINDING_STAIRS_UPPER_ES,
            BrickShape.WINDING_STAIRS_UPPER_NE,
            BrickShape.WINDING_STAIRS_UPPER_NW,
            BrickShape.WINDING_STAIRS_UPPER_SE,
            BrickShape.WINDING_STAIRS_UPPER_SW,
            BrickShape.WINDING_STAIRS_UPPER_WN,
            BrickShape.WINDING_STAIRS_UPPER_WS,
        )
        WINDOWS -> listOf(
            BrickShape.WINDOW_TOP_L_EAST,
            BrickShape.WINDOW_TOP_L_NORTH,
            BrickShape.WINDOW_TOP_L_SOUTH,
            BrickShape.WINDOW_TOP_L_WEST,
            BrickShape.WINDOW_TOP_R_EAST,
            BrickShape.WINDOW_TOP_R_NORTH,
            BrickShape.WINDOW_TOP_R_SOUTH,
            BrickShape.WINDOW_TOP_R_WEST,
        )
        else -> listOf()
    }

    companion object {
        private const val MAX_NESTING_LEVEL = 4

        fun BrickShape.findFirstInnermostGroup() =
            entries.firstOrNull { it.shapes().contains(this) }
    }
}
