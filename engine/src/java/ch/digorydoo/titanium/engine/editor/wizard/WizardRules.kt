package ch.digorydoo.titanium.engine.editor.wizard

import ch.digorydoo.titanium.engine.brick.BrickMaterial.METAL_RED
import ch.digorydoo.titanium.engine.brick.BrickShape.*
import ch.digorydoo.titanium.engine.editor.wizard.DrawingWizard.If

internal val wizardRules = arrayOf(
    // then = BAR_FRAME_NORTH
    If(below = UPRIGHT_DBL_BAR_NORTH, north = NONE, then = BAR_FRAME_NORTH),

    // then = BAR_FRAME_EAST
    If(below = UPRIGHT_DBL_BAR_EAST, east = NONE, then = BAR_FRAME_EAST),

    // then = BAR_FRAME_SOUTH
    If(below = UPRIGHT_DBL_BAR_SOUTH, south = NONE, then = BAR_FRAME_SOUTH),

    // then = BAR_FRAME_WEST
    If(below = UPRIGHT_DBL_BAR_WEST, west = NONE, then = BAR_FRAME_WEST),

    // then = BEVEL_NORTH
    If(east = BEVEL_NE, above = NONE, then = BEVEL_NORTH),
    If(west = BEVEL_NW, above = NONE, then = BEVEL_NORTH),

    // then = BEVEL_EAST
    If(north = BEVEL_NE, above = NONE, then = BEVEL_EAST),
    If(south = BEVEL_SE, above = NONE, then = BEVEL_EAST),

    // then = BEVEL_SOUTH
    If(east = BEVEL_SE, above = NONE, then = BEVEL_SOUTH),
    If(west = BEVEL_SW, above = NONE, then = BEVEL_SOUTH),

    // then = BEVEL_WEST
    If(north = BEVEL_NW, above = NONE, then = BEVEL_WEST),
    If(south = BEVEL_SW, above = NONE, then = BEVEL_WEST),

    // then = BEVEL_NN
    If(north = NONE, east = NONE, south = BASIC_BLOCK, west = NONE, above = NONE, below = BASIC_BLOCK, then = BEVEL_NN),
    If(south = BEVEL_SS, west = NONE, north = NONE, east = NONE, above = NONE, then = BEVEL_NN),

    // then = BEVEL_EE
    If(north = NONE, east = NONE, south = NONE, west = BASIC_BLOCK, above = NONE, below = BASIC_BLOCK, then = BEVEL_EE),
    If(west = BEVEL_WW, north = NONE, east = NONE, south = NONE, above = NONE, then = BEVEL_EE),

    // then = BEVEL_SS
    If(north = BEVEL_NN, east = NONE, south = NONE, west = NONE, above = NONE, then = BEVEL_SS),
    If(north = BASIC_BLOCK, east = NONE, south = NONE, west = NONE, above = NONE, below = BASIC_BLOCK, then = BEVEL_SS),

    // then = BEVEL_WW
    If(east = BEVEL_EE, south = NONE, west = NONE, north = NONE, above = NONE, then = BEVEL_WW),
    If(north = NONE, east = BASIC_BLOCK, south = NONE, west = NONE, above = NONE, below = BASIC_BLOCK, then = BEVEL_WW),

    // then = BEVEL_NE
    If(south = BEVEL_EAST, above = NONE, then = BEVEL_NE),
    If(south = BEVEL_SS, west = BEVEL_WW, north = NONE, east = NONE, above = NONE, then = BEVEL_NE),
    If(south = BASIC_BLOCK, west = BASIC_BLOCK, north = NONE, east = NONE, above = NONE, then = BEVEL_NE),
    If(west = BEVEL_NORTH, above = NONE, then = BEVEL_NE),

    // then = BEVEL_SE
    If(north = BEVEL_EAST, above = NONE, then = BEVEL_SE),
    If(west = BEVEL_SOUTH, above = NONE, then = BEVEL_SE),
    If(west = BEVEL_WW, north = BEVEL_NN, east = NONE, south = NONE, above = NONE, then = BEVEL_SE),
    If(west = BASIC_BLOCK, north = BASIC_BLOCK, east = NONE, south = NONE, above = NONE, then = BEVEL_SE),

    // then = BEVEL_SW
    If(east = BEVEL_SOUTH, above = NONE, then = BEVEL_SW),
    If(north = BEVEL_NN, east = BEVEL_EE, south = NONE, west = NONE, above = NONE, then = BEVEL_SW),
    If(north = BEVEL_WEST, above = NONE, then = BEVEL_SW),
    If(north = BASIC_BLOCK, east = BASIC_BLOCK, south = NONE, west = NONE, above = NONE, then = BEVEL_SW),

    // then = BEVEL_NW
    If(east = BEVEL_EE, south = BEVEL_SS, west = NONE, north = NONE, above = NONE, then = BEVEL_NW),
    If(east = BEVEL_NORTH, above = NONE, then = BEVEL_NW),
    If(east = BASIC_BLOCK, south = BASIC_BLOCK, west = NONE, north = NONE, above = NONE, then = BEVEL_NW),
    If(south = BEVEL_WEST, above = NONE, then = BEVEL_NW),

    // then = BEVEL_NS
    If(north = BEVEL_NN, east = NONE, south = BASIC_BLOCK, west = NONE, above = NONE, then = BEVEL_NS),
    If(north = BEVEL_NN, south = BEVEL_NS, above = NONE, then = BEVEL_NS),
    If(north = BEVEL_NN, south = BEVEL_SS, above = NONE, then = BEVEL_NS),
    If(north = BEVEL_NS, east = NONE, west = NONE, above = NONE, then = BEVEL_NS),
    If(north = BEVEL_NS, south = BEVEL_NS, above = NONE, then = BEVEL_NS),
    If(south = BEVEL_NS, east = NONE, west = NONE, above = NONE, then = BEVEL_NS),
    If(south = BEVEL_SS, north = BEVEL_NS, above = NONE, then = BEVEL_NS),
    If(south = BEVEL_SS, west = NONE, north = BASIC_BLOCK, east = NONE, above = NONE, then = BEVEL_NS),

    // then = BEVEL_WE
    If(east = BEVEL_EE, south = NONE, west = BASIC_BLOCK, north = NONE, above = NONE, then = BEVEL_WE),
    If(east = BEVEL_EE, west = BEVEL_WE, above = NONE, then = BEVEL_WE),
    If(east = BEVEL_EE, west = BEVEL_WW, above = NONE, then = BEVEL_WE),
    If(east = BEVEL_WE, north = NONE, south = NONE, above = NONE, then = BEVEL_WE),
    If(east = BEVEL_WE, west = BEVEL_WE, above = NONE, then = BEVEL_WE),
    If(west = BEVEL_WE, north = NONE, south = NONE, above = NONE, then = BEVEL_WE),
    If(west = BEVEL_WW, east = BEVEL_WE, above = NONE, then = BEVEL_WE),
    If(west = BEVEL_WW, north = NONE, east = BASIC_BLOCK, south = NONE, above = NONE, then = BEVEL_WE),

    // then = HALFD_HIGH_BAR_NORTH
    If(north = HALFH_CEILING, then = HALFD_HIGH_BAR_NORTH),

    // then = HALFD_HIGH_BAR_EAST
    If(east = HALFH_CEILING, then = HALFD_HIGH_BAR_EAST),

    // then = HALFD_HIGH_BAR_SOUTH
    If(south = HALFH_CEILING, then = HALFD_HIGH_BAR_SOUTH),

    // then = HALFD_HIGH_BAR_WEST
    If(west = HALFH_CEILING, then = HALFD_HIGH_BAR_WEST),

    // then = HALFD_WALL_NORTH
    If(above = HALFD_WALL_TOP_NORTH, then = HALFD_WALL_NORTH),
    If(west = HALFD_WALL_CORNER_NW, then = HALFD_WALL_NORTH),
    If(east = HALFD_WALL_CORNER_NE, then = HALFD_WALL_NORTH),

    // then = HALFD_WALL_EAST
    If(above = HALFD_WALL_TOP_EAST, then = HALFD_WALL_EAST),
    If(north = HALFD_WALL_CORNER_NE, then = HALFD_WALL_EAST),
    If(south = HALFD_WALL_CORNER_SE, then = HALFD_WALL_EAST),

    // then = HALFD_WALL_SOUTH
    If(above = HALFD_WALL_TOP_SOUTH, then = HALFD_WALL_SOUTH),
    If(west = HALFD_WALL_CORNER_SW, then = HALFD_WALL_SOUTH),
    If(east = HALFD_WALL_CORNER_SE, then = HALFD_WALL_SOUTH),

    // then = HALFD_WALL_WEST
    If(above = HALFD_WALL_TOP_WEST, then = HALFD_WALL_WEST),
    If(north = HALFD_WALL_CORNER_NW, then = HALFD_WALL_WEST),
    If(south = HALFD_WALL_CORNER_SW, then = HALFD_WALL_WEST),

    // then = HALFD_WALL_CORNER_NE
    If(west = HALFD_WALL_NORTH, south = HALFD_WALL_EAST, then = HALFD_WALL_CORNER_NE),

    // then = HALFD_WALL_CORNER_NW
    If(east = HALFD_WALL_NORTH, south = HALFD_WALL_WEST, then = HALFD_WALL_CORNER_NW),

    // then = HALFD_WALL_CORNER_SE
    If(north = HALFD_WALL_EAST, west = HALFD_WALL_SOUTH, then = HALFD_WALL_CORNER_SE),

    // then = HALFD_WALL_CORNER_SW
    If(north = HALFD_WALL_WEST, east = HALFD_WALL_SOUTH, then = HALFD_WALL_CORNER_SW),

    // then = HALFD_WALL_TOP_NORTH
    If(below = HALFD_WALL_NORTH, then = HALFD_WALL_TOP_NORTH),
    If(east = HALFD_WALL_CORNER_TOP_NE, then = HALFD_WALL_TOP_NORTH),
    If(west = HALFD_WALL_CORNER_TOP_NW, then = HALFD_WALL_TOP_NORTH),
    If(south = HALFH_CEILING, then = HALFD_WALL_TOP_NORTH),

    // then = HALFD_WALL_TOP_EAST
    If(below = HALFD_WALL_EAST, then = HALFD_WALL_TOP_EAST),
    If(north = HALFD_WALL_CORNER_TOP_NE, then = HALFD_WALL_TOP_EAST),
    If(south = HALFD_WALL_CORNER_TOP_SE, then = HALFD_WALL_TOP_EAST),
    If(west = HALFH_CEILING, then = HALFD_WALL_TOP_EAST),

    // then = HALFD_WALL_TOP_SOUTH
    If(below = HALFD_WALL_SOUTH, then = HALFD_WALL_TOP_SOUTH),
    If(east = HALFD_WALL_CORNER_TOP_SE, then = HALFD_WALL_TOP_SOUTH),
    If(west = HALFD_WALL_CORNER_TOP_SW, then = HALFD_WALL_TOP_SOUTH),
    If(north = HALFH_CEILING, then = HALFD_WALL_TOP_SOUTH),

    // then = HALFD_WALL_TOP_WEST
    If(below = HALFD_WALL_WEST, then = HALFD_WALL_TOP_WEST),
    If(north = HALFD_WALL_CORNER_TOP_NW, then = HALFD_WALL_TOP_WEST),
    If(south = HALFD_WALL_CORNER_TOP_SW, then = HALFD_WALL_TOP_WEST),
    If(east = HALFH_CEILING, then = HALFD_WALL_TOP_WEST),

    // then = HALFD_WALL_CORNER_TOP_NE
    If(west = HALFD_WALL_TOP_NORTH, then = HALFD_WALL_CORNER_TOP_NE),
    If(south = HALFD_WALL_TOP_EAST, then = HALFD_WALL_CORNER_TOP_NE),

    // then = HALFD_WALL_CORNER_TOP_NW
    If(east = HALFD_WALL_TOP_NORTH, then = HALFD_WALL_CORNER_TOP_NW),
    If(south = HALFD_WALL_TOP_WEST, then = HALFD_WALL_CORNER_TOP_NW),

    // then = HALFD_WALL_CORNER_TOP_SE
    If(west = HALFD_WALL_TOP_SOUTH, then = HALFD_WALL_CORNER_TOP_SE),
    If(north = HALFD_WALL_TOP_EAST, then = HALFD_WALL_CORNER_TOP_SE),

    // then = HALFD_WALL_CORNER_TOP_SW
    If(east = HALFD_WALL_TOP_SOUTH, then = HALFD_WALL_CORNER_TOP_SW),
    If(north = HALFD_WALL_TOP_WEST, then = HALFD_WALL_CORNER_TOP_SW),

    // then = HALFH_CEILING
    If(north = HALFD_WALL_TOP_NORTH, then = HALFH_CEILING),
    If(east = HALFD_WALL_TOP_EAST, then = HALFH_CEILING),
    If(south = HALFD_WALL_TOP_SOUTH, then = HALFH_CEILING),
    If(west = HALFD_WALL_TOP_WEST, then = HALFH_CEILING),
    If(north = HALFH_CEILING_V_CUT_NE, then = HALFH_CEILING),
    If(east = HALFH_CEILING_V_CUT_NE, then = HALFH_CEILING),
    If(north = HALFH_CEILING_V_CUT_NW, then = HALFH_CEILING),
    If(west = HALFH_CEILING_V_CUT_NW, then = HALFH_CEILING),
    If(south = HALFH_CEILING_V_CUT_SE, then = HALFH_CEILING),
    If(east = HALFH_CEILING_V_CUT_SE, then = HALFH_CEILING),
    If(south = HALFH_CEILING_V_CUT_SW, then = HALFH_CEILING),
    If(west = HALFH_CEILING_V_CUT_SW, then = HALFH_CEILING),
    If(west = THICK_HALFH_HIGH_BAR_EAST, belowIsnt = HALFH_CEILING, then = HALFH_CEILING),
    If(north = THICK_HALFH_HIGH_BAR_SOUTH, belowIsnt = HALFH_CEILING, then = HALFH_CEILING),
    If(east = THICK_HALFH_HIGH_BAR_WEST, belowIsnt = HALFH_CEILING, then = HALFH_CEILING),
    If(south = THICK_HALFH_HIGH_BAR_NORTH, belowIsnt = HALFH_CEILING, then = HALFH_CEILING),

    // then = HALFH_CEILING_V_CUT_NE
    If(above = VERTICAL_CUT_NE, then = HALFH_CEILING_V_CUT_NE),

    // then = HALFH_CEILING_V_CUT_NW
    If(above = VERTICAL_CUT_NW, then = HALFH_CEILING_V_CUT_NW),

    // then = HALFH_CEILING_V_CUT_SE
    If(above = VERTICAL_CUT_SE, then = HALFH_CEILING_V_CUT_SE),

    // then = HALFH_CEILING_V_CUT_SW
    If(above = VERTICAL_CUT_SW, then = HALFH_CEILING_V_CUT_SW),

    // then = HALFH_FLOOR
    If(north = HALFH_FLOOR_V_CUT_NE, then = HALFH_FLOOR),
    If(east = HALFH_FLOOR_V_CUT_NE, then = HALFH_FLOOR),
    If(north = HALFH_FLOOR_V_CUT_NW, then = HALFH_FLOOR),
    If(west = HALFH_FLOOR_V_CUT_NW, then = HALFH_FLOOR),
    If(south = HALFH_FLOOR_V_CUT_SE, then = HALFH_FLOOR),
    If(east = HALFH_FLOOR_V_CUT_SE, then = HALFH_FLOOR),
    If(south = HALFH_FLOOR_V_CUT_SW, then = HALFH_FLOOR),
    If(west = HALFH_FLOOR_V_CUT_SW, then = HALFH_FLOOR),

    // then = HALFH_FLOOR_V_CUT_NE
    If(below = VERTICAL_CUT_NE, then = HALFH_FLOOR_V_CUT_NE),

    // then = HALFH_FLOOR_V_CUT_NW
    If(below = VERTICAL_CUT_NW, then = HALFH_FLOOR_V_CUT_NW),

    // then = HALFH_FLOOR_V_CUT_SE
    If(below = VERTICAL_CUT_SE, then = HALFH_FLOOR_V_CUT_SE),

    // then = HALFH_FLOOR_V_CUT_SW
    If(below = VERTICAL_CUT_SW, then = HALFH_FLOOR_V_CUT_SW),

    // then = HALF_RAMP_TOP_NORTH
    If(south = HALFH_FLOOR, north = BASIC_BLOCK, then = HALF_RAMP_TOP_NORTH),

    // then = HALF_RAMP_TOP_EAST
    If(west = HALFH_FLOOR, east = BASIC_BLOCK, then = HALF_RAMP_TOP_EAST),

    // then = HALF_RAMP_TOP_SOUTH
    If(north = HALFH_FLOOR, south = BASIC_BLOCK, then = HALF_RAMP_TOP_SOUTH),

    // then = HALF_RAMP_TOP_WEST
    If(east = HALFH_FLOOR, west = BASIC_BLOCK, then = HALF_RAMP_TOP_WEST),

    // then = HIGH_BAR_NORTH
    If(above = UPRIGHT_BAR_NE, northIsnt = NONE, then = HIGH_BAR_NORTH, mat = METAL_RED),
    If(above = UPRIGHT_BAR_NW, northIsnt = NONE, then = HIGH_BAR_NORTH, mat = METAL_RED),
    If(above = UPRIGHT_DBL_BAR_NORTH, then = HIGH_BAR_NORTH, mat = METAL_RED),

    // then = HIGH_BAR_EAST
    If(above = UPRIGHT_BAR_NE, eastIsnt = NONE, then = HIGH_BAR_EAST, mat = METAL_RED),
    If(above = UPRIGHT_BAR_SE, eastIsnt = NONE, then = HIGH_BAR_EAST, mat = METAL_RED),
    If(above = UPRIGHT_DBL_BAR_EAST, then = HIGH_BAR_EAST, mat = METAL_RED),

    // then = HIGH_BAR_SOUTH
    If(above = UPRIGHT_BAR_SE, southIsnt = NONE, then = HIGH_BAR_SOUTH, mat = METAL_RED),
    If(above = UPRIGHT_BAR_SW, southIsnt = NONE, then = HIGH_BAR_SOUTH, mat = METAL_RED),
    If(above = UPRIGHT_DBL_BAR_SOUTH, then = HIGH_BAR_SOUTH, mat = METAL_RED),

    // then = HIGH_BAR_WEST
    If(above = UPRIGHT_BAR_NW, westIsnt = NONE, then = HIGH_BAR_WEST, mat = METAL_RED),
    If(above = UPRIGHT_BAR_SW, westIsnt = NONE, then = HIGH_BAR_WEST, mat = METAL_RED),
    If(above = UPRIGHT_DBL_BAR_WEST, then = HIGH_BAR_WEST, mat = METAL_RED),

    // then = INVERSE_RAMP_RUN_EAST
    If(above = RAMP_RUN_EAST, then = INVERSE_RAMP_RUN_EAST),
    If(above = STAIRS_RUN_EAST, then = INVERSE_RAMP_RUN_EAST),

    // then = INVERSE_RAMP_RUN_NORTH
    If(above = RAMP_RUN_NORTH, then = INVERSE_RAMP_RUN_NORTH),
    If(above = STAIRS_RUN_NORTH, then = INVERSE_RAMP_RUN_NORTH),

    // then = INVERSE_RAMP_RUN_SOUTH
    If(above = RAMP_RUN_SOUTH, then = INVERSE_RAMP_RUN_SOUTH),
    If(above = STAIRS_RUN_SOUTH, then = INVERSE_RAMP_RUN_SOUTH),

    // then = INVERSE_RAMP_RUN_WEST
    If(above = RAMP_RUN_WEST, then = INVERSE_RAMP_RUN_WEST),
    If(above = STAIRS_RUN_WEST, then = INVERSE_RAMP_RUN_WEST),

    // then = LOW_BAR_NORTH
    If(below = BASIC_BLOCK, above = NONE, north = NONE, northBelow = NONE, then = LOW_BAR_NORTH),
    If(below = UPRIGHT_BAR_NE, northIsnt = NONE, then = LOW_BAR_NORTH, mat = METAL_RED),
    If(below = UPRIGHT_BAR_NW, northIsnt = NONE, then = LOW_BAR_NORTH, mat = METAL_RED),
    If(below = UPRIGHT_DBL_BAR_NORTH, northIsnt = NONE, then = LOW_BAR_NORTH, mat = METAL_RED),
    If(northBelow = UPRIGHT_BAR_SE, northIsnt = NONE, then = LOW_BAR_NORTH, mat = METAL_RED),
    If(northBelow = UPRIGHT_BAR_SW, northIsnt = NONE, then = LOW_BAR_NORTH, mat = METAL_RED),
    If(northBelow = UPRIGHT_DBL_BAR_SOUTH, northIsnt = NONE, then = LOW_BAR_NORTH, mat = METAL_RED),

    // then = LOW_BAR_EAST
    If(below = BASIC_BLOCK, above = NONE, east = NONE, eastBelow = NONE, then = LOW_BAR_EAST),
    If(below = UPRIGHT_BAR_NE, eastIsnt = NONE, then = LOW_BAR_EAST, mat = METAL_RED),
    If(below = UPRIGHT_BAR_SE, eastIsnt = NONE, then = LOW_BAR_EAST, mat = METAL_RED),
    If(below = UPRIGHT_DBL_BAR_EAST, eastIsnt = NONE, then = LOW_BAR_EAST, mat = METAL_RED),
    If(eastBelow = UPRIGHT_BAR_NW, eastIsnt = NONE, then = LOW_BAR_EAST, mat = METAL_RED),
    If(eastBelow = UPRIGHT_BAR_SW, eastIsnt = NONE, then = LOW_BAR_EAST, mat = METAL_RED),
    If(eastBelow = UPRIGHT_DBL_BAR_WEST, eastIsnt = NONE, then = LOW_BAR_EAST, mat = METAL_RED),

    // then = LOW_BAR_SOUTH
    If(below = BASIC_BLOCK, above = NONE, south = NONE, southBelow = NONE, then = LOW_BAR_SOUTH),
    If(below = UPRIGHT_BAR_SE, southIsnt = NONE, then = LOW_BAR_SOUTH, mat = METAL_RED),
    If(below = UPRIGHT_BAR_SW, southIsnt = NONE, then = LOW_BAR_SOUTH, mat = METAL_RED),
    If(below = UPRIGHT_DBL_BAR_SOUTH, southIsnt = NONE, then = LOW_BAR_SOUTH, mat = METAL_RED),
    If(southBelow = UPRIGHT_BAR_NE, southIsnt = NONE, then = LOW_BAR_SOUTH, mat = METAL_RED),
    If(southBelow = UPRIGHT_BAR_NW, southIsnt = NONE, then = LOW_BAR_SOUTH, mat = METAL_RED),
    If(southBelow = UPRIGHT_DBL_BAR_NORTH, southIsnt = NONE, then = LOW_BAR_SOUTH, mat = METAL_RED),

    // then = LOW_BAR_WEST
    If(below = BASIC_BLOCK, above = NONE, west = NONE, westBelow = NONE, then = LOW_BAR_WEST),
    If(below = UPRIGHT_BAR_NW, westIsnt = NONE, then = LOW_BAR_WEST, mat = METAL_RED),
    If(below = UPRIGHT_BAR_SW, westIsnt = NONE, then = LOW_BAR_WEST, mat = METAL_RED),
    If(below = UPRIGHT_DBL_BAR_WEST, westIsnt = NONE, then = LOW_BAR_WEST, mat = METAL_RED),
    If(westBelow = UPRIGHT_BAR_NE, westIsnt = NONE, then = LOW_BAR_WEST, mat = METAL_RED),
    If(westBelow = UPRIGHT_BAR_SE, westIsnt = NONE, then = LOW_BAR_WEST, mat = METAL_RED),
    If(westBelow = UPRIGHT_DBL_BAR_EAST, westIsnt = NONE, then = LOW_BAR_WEST, mat = METAL_RED),

    // then = LOW_BAR_NS
    If(
        below = BASIC_BLOCK,
        above = NONE,
        east = NONE,
        west = NONE,
        eastBelowIsnt = NONE,
        westBelowIsnt = NONE,
        then = LOW_BAR_NS
    ),

    // then = LOW_BAR_WE
    If(
        below = BASIC_BLOCK,
        above = NONE,
        north = NONE,
        south = NONE,
        northBelowIsnt = NONE,
        southBelowIsnt = NONE,
        then = LOW_BAR_WE
    ),

    // then = OCTAGONAL_UPRIGHT_PILLAR
    If(below = BASIC_BLOCK, north = NONE, east = NONE, south = NONE, west = NONE, then = OCTAGONAL_UPRIGHT_PILLAR),

    // then = RAMP_CORNER_NE
    If(north = RAMP_RUN_EAST, east = RAMP_RUN_NORTH, then = RAMP_CORNER_NE),
    If(north = RAMP_CORNER_NE_ALT, then = RAMP_CORNER_NE),
    If(east = RAMP_CORNER_NE_ALT, then = RAMP_CORNER_NE),

    // then = RAMP_CORNER_NW
    If(west = RAMP_RUN_NORTH, north = RAMP_RUN_WEST, then = RAMP_CORNER_NW),
    If(north = RAMP_CORNER_NW_ALT, then = RAMP_CORNER_NW),
    If(west = RAMP_CORNER_NW_ALT, then = RAMP_CORNER_NW),

    // then = RAMP_CORNER_SE
    If(east = RAMP_RUN_SOUTH, south = RAMP_RUN_EAST, then = RAMP_CORNER_SE),
    If(south = RAMP_CORNER_SE_ALT, then = RAMP_CORNER_SE),
    If(east = RAMP_CORNER_SE_ALT, then = RAMP_CORNER_SE),

    // then = RAMP_CORNER_SW
    If(south = RAMP_RUN_WEST, west = RAMP_RUN_SOUTH, then = RAMP_CORNER_SW),
    If(south = RAMP_CORNER_SW_ALT, then = RAMP_CORNER_SW),
    If(west = RAMP_CORNER_SW_ALT, then = RAMP_CORNER_SW),

    // then = RAMP_CORNER_NE_ALT
    If(south = RAMP_RUN_EAST, west = RAMP_RUN_NORTH, then = RAMP_CORNER_NE_ALT),
    If(south = RAMP_CORNER_NE, then = RAMP_CORNER_NE_ALT),
    If(west = RAMP_CORNER_NE, then = RAMP_CORNER_NE_ALT),
    If(
        north = BASIC_BLOCK,
        east = BASIC_BLOCK,
        below = BASIC_BLOCK,
        above = VERTICAL_CUT_SW,
        then = RAMP_CORNER_NE_ALT
    ),

    // then = RAMP_CORNER_NW_ALT
    If(east = RAMP_RUN_NORTH, south = RAMP_RUN_WEST, then = RAMP_CORNER_NW_ALT),
    If(south = RAMP_CORNER_NW, then = RAMP_CORNER_NW_ALT),
    If(east = RAMP_CORNER_NW, then = RAMP_CORNER_NW_ALT),
    If(
        north = BASIC_BLOCK,
        west = BASIC_BLOCK,
        below = BASIC_BLOCK,
        above = VERTICAL_CUT_SE,
        then = RAMP_CORNER_NW_ALT
    ),

    // then = RAMP_CORNER_SE_ALT
    If(west = RAMP_RUN_SOUTH, north = RAMP_RUN_EAST, then = RAMP_CORNER_SE_ALT),
    If(north = RAMP_CORNER_SE, then = RAMP_CORNER_SE_ALT),
    If(west = RAMP_CORNER_SE, then = RAMP_CORNER_SE_ALT),
    If(
        south = BASIC_BLOCK,
        east = BASIC_BLOCK,
        below = BASIC_BLOCK,
        above = VERTICAL_CUT_NW,
        then = RAMP_CORNER_SE_ALT
    ),

    // then = RAMP_CORNER_SW_ALT
    If(north = RAMP_RUN_WEST, east = RAMP_RUN_SOUTH, then = RAMP_CORNER_SW_ALT),
    If(north = RAMP_CORNER_SW, then = RAMP_CORNER_SW_ALT),
    If(east = RAMP_CORNER_SW, then = RAMP_CORNER_SW_ALT),
    If(
        south = BASIC_BLOCK,
        west = BASIC_BLOCK,
        below = BASIC_BLOCK,
        above = VERTICAL_CUT_NE,
        then = RAMP_CORNER_SW_ALT
    ),

    // then = RAMP_RUN_EAST
    If(below = INVERSE_RAMP_RUN_EAST, then = RAMP_RUN_EAST),

    // then = RAMP_RUN_NORTH
    If(below = INVERSE_RAMP_RUN_NORTH, then = RAMP_RUN_NORTH),

    // then = RAMP_RUN_SOUTH
    If(below = INVERSE_RAMP_RUN_SOUTH, then = RAMP_RUN_SOUTH),

    // then = RAMP_RUN_WEST
    If(below = INVERSE_RAMP_RUN_WEST, then = RAMP_RUN_WEST),

    // then = STAIRS_RUN_EAST
    If(east = BASIC_BLOCK, below = BASIC_BLOCK, above = NONE, then = STAIRS_RUN_EAST),

    // then = STAIRS_RUN_NORTH
    If(north = BASIC_BLOCK, below = BASIC_BLOCK, above = NONE, then = STAIRS_RUN_NORTH),

    // then = STAIRS_RUN_SOUTH
    If(south = BASIC_BLOCK, below = BASIC_BLOCK, above = NONE, then = STAIRS_RUN_SOUTH),

    // then = STAIRS_RUN_WEST
    If(west = BASIC_BLOCK, below = BASIC_BLOCK, above = NONE, then = STAIRS_RUN_WEST),

    // then = STRAIGHT_STAIRS_NORTH
    If(north = THICK_CEILING, below = NONE, above = NONE, then = STRAIGHT_STAIRS_NORTH),
    If(south = THICK_FLOOR, below = NONE, above = NONE, then = STRAIGHT_STAIRS_NORTH),

    // then = STRAIGHT_STAIRS_EAST
    If(east = THICK_CEILING, below = NONE, above = NONE, then = STRAIGHT_STAIRS_EAST),
    If(west = THICK_FLOOR, below = NONE, above = NONE, then = STRAIGHT_STAIRS_EAST),

    // then = STRAIGHT_STAIRS_SOUTH
    If(south = THICK_CEILING, below = NONE, above = NONE, then = STRAIGHT_STAIRS_SOUTH),
    If(north = THICK_FLOOR, below = NONE, above = NONE, then = STRAIGHT_STAIRS_SOUTH),

    // then = STRAIGHT_STAIRS_WEST
    If(west = THICK_CEILING, below = NONE, above = NONE, then = STRAIGHT_STAIRS_WEST),
    If(east = THICK_FLOOR, below = NONE, above = NONE, then = STRAIGHT_STAIRS_WEST),

    // then = THICK_CEILING
    If(north = THICK_WALL_TOP_NORTH, belowIsnt = THICK_CEILING, then = THICK_CEILING),
    If(east = THICK_WALL_TOP_EAST, belowIsnt = THICK_CEILING, then = THICK_CEILING),
    If(south = THICK_WALL_TOP_SOUTH, belowIsnt = THICK_CEILING, then = THICK_CEILING),
    If(west = THICK_WALL_TOP_WEST, belowIsnt = THICK_CEILING, then = THICK_CEILING),
    If(north = STRAIGHT_STAIRS_SOUTH, belowIsnt = THICK_CEILING, then = THICK_CEILING),
    If(east = STRAIGHT_STAIRS_WEST, belowIsnt = THICK_CEILING, then = THICK_CEILING),
    If(south = STRAIGHT_STAIRS_NORTH, belowIsnt = THICK_CEILING, then = THICK_CEILING),
    If(west = STRAIGHT_STAIRS_EAST, belowIsnt = THICK_CEILING, then = THICK_CEILING),
    If(north = THICK_CEILING_V_CUT_NE, belowIsnt = THICK_CEILING, then = THICK_CEILING),
    If(east = THICK_CEILING_V_CUT_NE, belowIsnt = THICK_CEILING, then = THICK_CEILING),
    If(north = THICK_CEILING_V_CUT_NW, belowIsnt = THICK_CEILING, then = THICK_CEILING),
    If(west = THICK_CEILING_V_CUT_NW, belowIsnt = THICK_CEILING, then = THICK_CEILING),
    If(south = THICK_CEILING_V_CUT_SE, belowIsnt = THICK_CEILING, then = THICK_CEILING),
    If(east = THICK_CEILING_V_CUT_SE, belowIsnt = THICK_CEILING, then = THICK_CEILING),
    If(south = THICK_CEILING_V_CUT_SW, belowIsnt = THICK_CEILING, then = THICK_CEILING),
    If(west = THICK_CEILING_V_CUT_SW, belowIsnt = THICK_CEILING, then = THICK_CEILING),

    // then = THICK_CEILING_V_CUT_NE
    If(south = THICK_CEILING, then = THICK_CEILING_V_CUT_NE),
    If(west = THICK_CEILING, then = THICK_CEILING_V_CUT_NE),
    If(above = VERTICAL_CUT_NE, then = THICK_CEILING_V_CUT_NE),

    // then = THICK_CEILING_V_CUT_NW
    If(east = THICK_CEILING, then = THICK_CEILING_V_CUT_NW),
    If(south = THICK_CEILING, then = THICK_CEILING_V_CUT_NW),
    If(above = VERTICAL_CUT_NW, then = THICK_CEILING_V_CUT_NW),

    // then = THICK_CEILING_V_CUT_SE
    If(north = THICK_CEILING, then = THICK_CEILING_V_CUT_SE),
    If(west = THICK_CEILING, then = THICK_CEILING_V_CUT_SE),
    If(above = VERTICAL_CUT_SE, then = THICK_CEILING_V_CUT_SE),

    // then = THICK_CEILING_V_CUT_SW
    If(east = THICK_CEILING, then = THICK_CEILING_V_CUT_SW),
    If(north = THICK_CEILING, then = THICK_CEILING_V_CUT_SW),
    If(above = VERTICAL_CUT_SW, then = THICK_CEILING_V_CUT_SW),

    // then = THICK_FLOOR
    If(north = STRAIGHT_STAIRS_NORTH, then = THICK_FLOOR),
    If(east = STRAIGHT_STAIRS_EAST, then = THICK_FLOOR),
    If(south = STRAIGHT_STAIRS_SOUTH, then = THICK_FLOOR),
    If(west = STRAIGHT_STAIRS_WEST, then = THICK_FLOOR),
    If(north = THICK_FLOOR_V_CUT_NE, then = THICK_FLOOR),
    If(east = THICK_FLOOR_V_CUT_NE, then = THICK_FLOOR),
    If(north = THICK_FLOOR_V_CUT_NW, then = THICK_FLOOR),
    If(west = THICK_FLOOR_V_CUT_NW, then = THICK_FLOOR),
    If(south = THICK_FLOOR_V_CUT_SE, then = THICK_FLOOR),
    If(east = THICK_FLOOR_V_CUT_SE, then = THICK_FLOOR),
    If(south = THICK_FLOOR_V_CUT_SW, then = THICK_FLOOR),
    If(west = THICK_FLOOR_V_CUT_SW, then = THICK_FLOOR),

    // then = THICK_FLOOR_V_CUT_NE
    If(south = THICK_FLOOR, then = THICK_FLOOR_V_CUT_NE),
    If(west = THICK_FLOOR, then = THICK_FLOOR_V_CUT_NE),
    If(below = VERTICAL_CUT_NE, then = THICK_FLOOR_V_CUT_NE),

    // then = THICK_FLOOR_V_CUT_NW
    If(east = THICK_FLOOR, then = THICK_FLOOR_V_CUT_NW),
    If(south = THICK_FLOOR, then = THICK_FLOOR_V_CUT_NW),
    If(below = VERTICAL_CUT_NW, then = THICK_FLOOR_V_CUT_NW),

    // then = THICK_FLOOR_V_CUT_SE
    If(north = THICK_FLOOR, then = THICK_FLOOR_V_CUT_SE),
    If(west = THICK_FLOOR, then = THICK_FLOOR_V_CUT_SE),
    If(below = VERTICAL_CUT_SE, then = THICK_FLOOR_V_CUT_SE),

    // then = THICK_FLOOR_V_CUT_SW
    If(east = THICK_FLOOR, then = THICK_FLOOR_V_CUT_SW),
    If(north = THICK_FLOOR, then = THICK_FLOOR_V_CUT_SW),
    If(below = VERTICAL_CUT_SW, then = THICK_FLOOR_V_CUT_SW),

    // then = THICK_HALFH_HIGH_BAR_NORTH
    If(above = THICK_WALL_NORTH, then = THICK_HALFH_HIGH_BAR_NORTH),
    If(north = HALFH_CEILING, south = NONE, then = THICK_HALFH_HIGH_BAR_NORTH),

    // then = THICK_HALFH_HIGH_BAR_EAST
    If(above = THICK_WALL_EAST, then = THICK_HALFH_HIGH_BAR_EAST),
    If(east = HALFH_CEILING, west = NONE, then = THICK_HALFH_HIGH_BAR_EAST),

    // then = THICK_HALFH_HIGH_BAR_SOUTH
    If(above = THICK_WALL_SOUTH, then = THICK_HALFH_HIGH_BAR_SOUTH),
    If(south = HALFH_CEILING, north = NONE, then = THICK_HALFH_HIGH_BAR_SOUTH),

    // then = THICK_HALFH_HIGH_BAR_WEST
    If(above = THICK_WALL_WEST, then = THICK_HALFH_HIGH_BAR_WEST),
    If(west = HALFH_CEILING, east = NONE, then = THICK_HALFH_HIGH_BAR_WEST),

    // then = THICK_HALFH_LOW_BAR_NORTH
    If(below = THICK_WALL_NORTH, then = THICK_HALFH_LOW_BAR_NORTH),
    If(west = WINDOW_TOP_L_NORTH, east = WINDOW_TOP_R_NORTH, then = THICK_HALFH_LOW_BAR_NORTH),

    // then = THICK_HALFH_LOW_BAR_EAST
    If(below = THICK_WALL_EAST, then = THICK_HALFH_LOW_BAR_EAST),
    If(north = WINDOW_TOP_L_EAST, south = WINDOW_TOP_R_EAST, then = THICK_HALFH_LOW_BAR_EAST),

    // then = THICK_HALFH_LOW_BAR_SOUTH
    If(below = THICK_WALL_SOUTH, then = THICK_HALFH_LOW_BAR_SOUTH),
    If(east = WINDOW_TOP_L_SOUTH, west = WINDOW_TOP_R_SOUTH, then = THICK_HALFH_LOW_BAR_SOUTH),

    // then = THICK_HALFH_LOW_BAR_WEST
    If(below = THICK_WALL_WEST, then = THICK_HALFH_LOW_BAR_WEST),
    If(south = WINDOW_TOP_L_WEST, north = WINDOW_TOP_R_WEST, then = THICK_HALFH_LOW_BAR_WEST),

    // then = THICK_HIGH_BAR_NORTH
    If(north = THICK_CEILING, then = THICK_HIGH_BAR_NORTH),
    If(above = STRAIGHT_STAIRS_SOUTH, then = THICK_HIGH_BAR_NORTH),

    // then = THICK_HIGH_BAR_EAST
    If(east = THICK_CEILING, then = THICK_HIGH_BAR_EAST),
    If(above = STRAIGHT_STAIRS_WEST, then = THICK_HIGH_BAR_EAST),

    // then = THICK_HIGH_BAR_SOUTH
    If(south = THICK_CEILING, then = THICK_HIGH_BAR_SOUTH),
    If(above = STRAIGHT_STAIRS_NORTH, then = THICK_HIGH_BAR_SOUTH),

    // then = THICK_HIGH_BAR_WEST
    If(west = THICK_CEILING, then = THICK_HIGH_BAR_WEST),
    If(above = STRAIGHT_STAIRS_EAST, then = THICK_HIGH_BAR_WEST),

    // then = THICK_WALL_NORTH
    If(west = BASIC_BLOCK, below = BASIC_BLOCK, then = THICK_WALL_NORTH),
    If(above = THICK_WALL_TOP_NORTH, then = THICK_WALL_NORTH),
    If(east = THICK_WALL_CORNER_NE, then = THICK_WALL_NORTH),
    If(west = THICK_WALL_CORNER_NW, then = THICK_WALL_NORTH),

    // then = THICK_WALL_EAST
    If(north = BASIC_BLOCK, below = BASIC_BLOCK, then = THICK_WALL_EAST),
    If(above = THICK_WALL_TOP_EAST, then = THICK_WALL_EAST),
    If(north = THICK_WALL_CORNER_NE, then = THICK_WALL_EAST),
    If(south = THICK_WALL_CORNER_SE, then = THICK_WALL_EAST),

    // then = THICK_WALL_SOUTH
    If(east = BASIC_BLOCK, below = BASIC_BLOCK, then = THICK_WALL_SOUTH),
    If(above = THICK_WALL_TOP_SOUTH, then = THICK_WALL_SOUTH),
    If(east = THICK_WALL_CORNER_SE, then = THICK_WALL_SOUTH),
    If(west = THICK_WALL_CORNER_SW, then = THICK_WALL_SOUTH),

    // then = THICK_WALL_WEST
    If(south = BASIC_BLOCK, below = BASIC_BLOCK, then = THICK_WALL_WEST),
    If(above = THICK_WALL_TOP_WEST, then = THICK_WALL_WEST),
    If(north = THICK_WALL_CORNER_NW, then = THICK_WALL_WEST),
    If(south = THICK_WALL_CORNER_SW, then = THICK_WALL_WEST),

    // then = THICK_WALL_TOP_NORTH
    If(below = THICK_WALL_NORTH, then = THICK_WALL_TOP_NORTH),
    If(east = THICK_WALL_CORNER_TOP_NE, then = THICK_WALL_TOP_NORTH),
    If(west = THICK_WALL_CORNER_TOP_NW, then = THICK_WALL_TOP_NORTH),
    If(south = THICK_CEILING, then = THICK_WALL_TOP_NORTH),

    // then = THICK_WALL_TOP_EAST
    If(below = THICK_WALL_EAST, then = THICK_WALL_TOP_EAST),
    If(north = THICK_WALL_CORNER_TOP_NE, then = THICK_WALL_TOP_EAST),
    If(south = THICK_WALL_CORNER_TOP_SE, then = THICK_WALL_TOP_EAST),
    If(west = THICK_CEILING, then = THICK_WALL_TOP_EAST),

    // then = THICK_WALL_TOP_SOUTH
    If(below = THICK_WALL_SOUTH, then = THICK_WALL_TOP_SOUTH),
    If(east = THICK_WALL_CORNER_TOP_SE, then = THICK_WALL_TOP_SOUTH),
    If(west = THICK_WALL_CORNER_TOP_SW, then = THICK_WALL_TOP_SOUTH),
    If(north = THICK_CEILING, then = THICK_WALL_TOP_SOUTH),

    // then = THICK_WALL_TOP_WEST
    If(below = THICK_WALL_WEST, then = THICK_WALL_TOP_WEST),
    If(north = THICK_WALL_CORNER_TOP_NW, then = THICK_WALL_TOP_WEST),
    If(south = THICK_WALL_CORNER_TOP_SW, then = THICK_WALL_TOP_WEST),
    If(east = THICK_CEILING, then = THICK_WALL_TOP_WEST),

    // then = THICK_WALL_CORNER_NE
    If(west = THICK_WALL_NORTH, south = THICK_WALL_EAST, then = THICK_WALL_CORNER_NE),

    // then = THICK_WALL_CORNER_NW
    If(east = THICK_WALL_NORTH, south = THICK_WALL_WEST, then = THICK_WALL_CORNER_NW),

    // then = THICK_WALL_CORNER_SE
    If(north = THICK_WALL_EAST, west = THICK_WALL_SOUTH, then = THICK_WALL_CORNER_SE),

    // then = THICK_WALL_CORNER_SW
    If(north = THICK_WALL_WEST, east = THICK_WALL_SOUTH, then = THICK_WALL_CORNER_SW),

    // then = THICK_WALL_CORNER_TOP_NE
    If(west = THICK_WALL_TOP_NORTH, then = THICK_WALL_CORNER_TOP_NE),
    If(south = THICK_WALL_TOP_EAST, then = THICK_WALL_CORNER_TOP_NE),

    // then = THICK_WALL_CORNER_TOP_NW
    If(east = THICK_WALL_TOP_NORTH, then = THICK_WALL_CORNER_TOP_NW),
    If(south = THICK_WALL_TOP_WEST, then = THICK_WALL_CORNER_TOP_NW),

    // then = THICK_WALL_CORNER_TOP_SE
    If(west = THICK_WALL_TOP_SOUTH, then = THICK_WALL_CORNER_TOP_SE),
    If(north = THICK_WALL_TOP_EAST, then = THICK_WALL_CORNER_TOP_SE),

    // then = THICK_WALL_CORNER_TOP_SW
    If(east = THICK_WALL_TOP_SOUTH, then = THICK_WALL_CORNER_TOP_SW),
    If(north = THICK_WALL_TOP_WEST, then = THICK_WALL_CORNER_TOP_SW),

    // then = THIN_WALL_NORTH
    If(west = BASIC_BLOCK, below = BASIC_BLOCK, then = THIN_WALL_NORTH),

    // then = THIN_WALL_EAST
    If(north = BASIC_BLOCK, below = BASIC_BLOCK, then = THIN_WALL_EAST),

    // then = THIN_WALL_SOUTH
    If(east = BASIC_BLOCK, below = BASIC_BLOCK, then = THIN_WALL_SOUTH),

    // then = THIN_WALL_WEST
    If(south = BASIC_BLOCK, below = BASIC_BLOCK, then = THIN_WALL_WEST),

    // then = THIN_WALL_TOP_NORTH
    If(below = THIN_WALL_NORTH, then = THIN_WALL_TOP_NORTH),
    If(east = THIN_WALL_CORNER_TOP_NE, then = THIN_WALL_TOP_NORTH),
    If(west = THIN_WALL_CORNER_TOP_NW, then = THIN_WALL_TOP_NORTH),

    // then = THIN_WALL_TOP_EAST
    If(below = THIN_WALL_EAST, then = THIN_WALL_TOP_EAST),
    If(north = THIN_WALL_CORNER_TOP_NE, then = THIN_WALL_TOP_EAST),
    If(south = THIN_WALL_CORNER_TOP_SE, then = THIN_WALL_TOP_EAST),

    // then = THIN_WALL_TOP_SOUTH
    If(below = THIN_WALL_SOUTH, then = THIN_WALL_TOP_SOUTH),
    If(east = THIN_WALL_CORNER_TOP_SE, then = THIN_WALL_TOP_SOUTH),
    If(west = THIN_WALL_CORNER_TOP_SW, then = THIN_WALL_TOP_SOUTH),

    // then = THIN_WALL_TOP_WEST
    If(below = THIN_WALL_WEST, then = THIN_WALL_TOP_WEST),
    If(north = THIN_WALL_CORNER_TOP_NW, then = THIN_WALL_TOP_WEST),
    If(south = THIN_WALL_CORNER_TOP_SW, then = THIN_WALL_TOP_WEST),

    // then = THIN_WALL_CORNER_NE
    If(west = THIN_WALL_NORTH, south = THIN_WALL_EAST, then = THIN_WALL_CORNER_NE),

    // then = THIN_WALL_CORNER_NW
    If(east = THIN_WALL_NORTH, south = THIN_WALL_WEST, then = THIN_WALL_CORNER_NW),

    // then = THIN_WALL_CORNER_SE
    If(north = THIN_WALL_EAST, west = THIN_WALL_SOUTH, then = THIN_WALL_CORNER_SE),

    // then = THIN_WALL_CORNER_SW
    If(north = THIN_WALL_WEST, east = THIN_WALL_SOUTH, then = THIN_WALL_CORNER_SW),

    // then = THIN_WALL_CORNER_TOP_NE
    If(west = THIN_WALL_TOP_NORTH, then = THIN_WALL_CORNER_TOP_NE),
    If(south = THIN_WALL_TOP_EAST, then = THIN_WALL_CORNER_TOP_NE),

    // then = THIN_WALL_CORNER_TOP_NW
    If(east = THIN_WALL_TOP_NORTH, then = THIN_WALL_CORNER_TOP_NW),
    If(south = THIN_WALL_TOP_WEST, then = THIN_WALL_CORNER_TOP_NW),

    // then = THIN_WALL_CORNER_TOP_SE
    If(west = THIN_WALL_TOP_SOUTH, then = THIN_WALL_CORNER_TOP_SE),
    If(north = THIN_WALL_TOP_EAST, then = THIN_WALL_CORNER_TOP_SE),

    // then = THIN_WALL_CORNER_TOP_SW
    If(east = THIN_WALL_TOP_SOUTH, then = THIN_WALL_CORNER_TOP_SW),
    If(north = THIN_WALL_TOP_WEST, then = THIN_WALL_CORNER_TOP_SW),

    // then = THIN_WALL_DIAGONAL_NE
    If(south = THIN_WALL_EAST, west = THIN_WALL_NORTH, then = THIN_WALL_DIAGONAL_NE),

    // then = THIN_WALL_DIAGONAL_NW
    If(east = THIN_WALL_NORTH, south = THIN_WALL_WEST, then = THIN_WALL_DIAGONAL_NW),

    // then = THIN_WALL_DIAGONAL_SE
    If(west = THIN_WALL_SOUTH, north = THIN_WALL_EAST, then = THIN_WALL_DIAGONAL_SE),

    // then = THIN_WALL_DIAGONAL_SW
    If(north = THIN_WALL_WEST, east = THIN_WALL_SOUTH, then = THIN_WALL_DIAGONAL_SW),

    // then = UPRIGHT_BAR_NE
    If(east = NONE, ne = BASIC_BLOCK, north = NONE, then = UPRIGHT_BAR_NE, mat = METAL_RED),
    If(east = UPRIGHT_DBL_BAR_NORTH, then = UPRIGHT_BAR_NE, mat = METAL_RED),
    If(north = UPRIGHT_DBL_BAR_EAST, then = UPRIGHT_BAR_NE, mat = METAL_RED),
    If(above = WINDOW_TOP_R_NORTH, then = UPRIGHT_BAR_NE),
    If(above = WINDOW_TOP_L_EAST, then = UPRIGHT_BAR_NE),

    // then = UPRIGHT_BAR_NW
    If(north = NONE, nw = BASIC_BLOCK, west = NONE, then = UPRIGHT_BAR_NW, mat = METAL_RED),
    If(west = UPRIGHT_DBL_BAR_NORTH, then = UPRIGHT_BAR_NW, mat = METAL_RED),
    If(north = UPRIGHT_DBL_BAR_WEST, then = UPRIGHT_BAR_NW, mat = METAL_RED),
    If(above = WINDOW_TOP_L_NORTH, then = UPRIGHT_BAR_NW),
    If(above = WINDOW_TOP_R_WEST, then = UPRIGHT_BAR_NW),

    // then = UPRIGHT_BAR_SE
    If(south = NONE, se = BASIC_BLOCK, east = NONE, then = UPRIGHT_BAR_SE, mat = METAL_RED),
    If(east = UPRIGHT_DBL_BAR_SOUTH, then = UPRIGHT_BAR_SE, mat = METAL_RED),
    If(south = UPRIGHT_DBL_BAR_EAST, then = UPRIGHT_BAR_SE, mat = METAL_RED),
    If(above = WINDOW_TOP_L_SOUTH, then = UPRIGHT_BAR_SE),
    If(above = WINDOW_TOP_R_EAST, then = UPRIGHT_BAR_SE),

    // then = UPRIGHT_BAR_SW
    If(west = NONE, sw = BASIC_BLOCK, south = NONE, then = UPRIGHT_BAR_SW, mat = METAL_RED),
    If(west = UPRIGHT_DBL_BAR_SOUTH, then = UPRIGHT_BAR_SW, mat = METAL_RED),
    If(south = UPRIGHT_DBL_BAR_WEST, then = UPRIGHT_BAR_SW, mat = METAL_RED),
    If(above = WINDOW_TOP_R_SOUTH, then = UPRIGHT_BAR_SW),
    If(above = WINDOW_TOP_L_WEST, then = UPRIGHT_BAR_SW),

    // then = UPRIGHT_DBL_BAR_NORTH
    If(above = BAR_FRAME_NORTH, then = UPRIGHT_DBL_BAR_NORTH),
    If(north = NONE, nw = BASIC_BLOCK, ne = BASIC_BLOCK, then = UPRIGHT_DBL_BAR_NORTH, mat = METAL_RED),

    // then = UPRIGHT_DBL_BAR_EAST
    If(above = BAR_FRAME_EAST, then = UPRIGHT_DBL_BAR_EAST),
    If(east = NONE, ne = BASIC_BLOCK, se = BASIC_BLOCK, then = UPRIGHT_DBL_BAR_EAST, mat = METAL_RED),

    // then = UPRIGHT_DBL_BAR_SOUTH
    If(above = BAR_FRAME_SOUTH, then = UPRIGHT_DBL_BAR_SOUTH),
    If(south = NONE, se = BASIC_BLOCK, sw = BASIC_BLOCK, then = UPRIGHT_DBL_BAR_SOUTH, mat = METAL_RED),

    // then = UPRIGHT_DBL_BAR_WEST
    If(above = BAR_FRAME_WEST, then = UPRIGHT_DBL_BAR_WEST),
    If(west = NONE, nw = BASIC_BLOCK, sw = BASIC_BLOCK, then = UPRIGHT_DBL_BAR_WEST, mat = METAL_RED),

    // then = VERTICAL_BEVEL_FULL
    If(
        above = BASIC_BLOCK,
        below = BASIC_BLOCK,
        north = NONE,
        east = NONE,
        south = NONE,
        west = NONE,
        then = VERTICAL_BEVEL_FULL
    ),
    If(below = THICK_CEILING, north = NONE, east = NONE, south = NONE, west = NONE, then = VERTICAL_BEVEL_FULL),
    If(below = HALFH_CEILING, north = NONE, east = NONE, south = NONE, west = NONE, then = VERTICAL_BEVEL_FULL),

    // then = VERTICAL_BEVEL_FULL_ALT
    If(
        above = BASIC_BLOCK,
        below = BASIC_BLOCK,
        north = NONE,
        east = NONE,
        south = NONE,
        west = NONE,
        then = VERTICAL_BEVEL_FULL_ALT
    ),
    If(below = THICK_CEILING, north = NONE, east = NONE, south = NONE, west = NONE, then = VERTICAL_BEVEL_FULL_ALT),
    If(below = HALFH_CEILING, north = NONE, east = NONE, south = NONE, west = NONE, then = VERTICAL_BEVEL_FULL_ALT),

    // then = VERTICAL_BEVEL_NORTH
    If(north = NONE, south = BASIC_BLOCK, then = VERTICAL_BEVEL_NORTH),

    // then = VERTICAL_BEVEL_EAST
    If(east = NONE, west = BASIC_BLOCK, then = VERTICAL_BEVEL_EAST),

    // then = VERTICAL_BEVEL_SOUTH
    If(south = NONE, north = BASIC_BLOCK, then = VERTICAL_BEVEL_SOUTH),

    // then = VERTICAL_BEVEL_WEST
    If(west = NONE, east = BASIC_BLOCK, then = VERTICAL_BEVEL_WEST),

    // then = VERTICAL_BEVEL_NE
    If(south = BASIC_BLOCK, west = BASIC_BLOCK, north = NONE, east = NONE, then = VERTICAL_BEVEL_NE),

    // then = VERTICAL_BEVEL_NW
    If(east = BASIC_BLOCK, south = BASIC_BLOCK, west = NONE, north = NONE, then = VERTICAL_BEVEL_NW),

    // then = VERTICAL_BEVEL_SE
    If(west = BASIC_BLOCK, north = BASIC_BLOCK, east = NONE, south = NONE, then = VERTICAL_BEVEL_SE),

    // then = VERTICAL_BEVEL_SW
    If(north = BASIC_BLOCK, east = BASIC_BLOCK, south = NONE, west = NONE, then = VERTICAL_BEVEL_SW),

    // then = VERTICAL_CUT_NE
    If(
        south = BASIC_BLOCK,
        west = BASIC_BLOCK,
        north = NONE,
        east = NONE,
        aboveIsnt = VERTICAL_CUT_NE,
        belowIsnt = VERTICAL_CUT_NE,
        then = VERTICAL_CUT_NE
    ),

    // then = VERTICAL_CUT_NW
    If(
        east = BASIC_BLOCK,
        south = BASIC_BLOCK,
        west = NONE,
        north = NONE,
        aboveIsnt = VERTICAL_CUT_NW,
        belowIsnt = VERTICAL_CUT_NW,
        then = VERTICAL_CUT_NW
    ),

    // then = VERTICAL_CUT_SE
    If(
        west = BASIC_BLOCK,
        north = BASIC_BLOCK,
        east = NONE,
        south = NONE,
        aboveIsnt = VERTICAL_CUT_SE,
        belowIsnt = VERTICAL_CUT_SE,
        then = VERTICAL_CUT_SE
    ),

    // then = VERTICAL_CUT_SW
    If(
        north = BASIC_BLOCK,
        east = BASIC_BLOCK,
        south = NONE,
        west = NONE,
        aboveIsnt = VERTICAL_CUT_SW,
        belowIsnt = VERTICAL_CUT_SW,
        then = VERTICAL_CUT_SW
    ),

    // then = WINDING_STAIRS_NE
    If(north = NONE, northBelow = BASIC_BLOCK, east = BASIC_BLOCK, above = NONE, then = WINDING_STAIRS_NE),
    If(north = NONE, northBelow = HALFH_CEILING, east = HALFH_CEILING, above = NONE, then = WINDING_STAIRS_NE),

    // then = WINDING_STAIRS_NW
    If(north = NONE, northBelow = BASIC_BLOCK, west = BASIC_BLOCK, above = NONE, then = WINDING_STAIRS_NW),
    If(north = NONE, northBelow = HALFH_CEILING, west = HALFH_CEILING, above = NONE, then = WINDING_STAIRS_NW),

    // then = WINDING_STAIRS_SE
    If(south = NONE, southBelow = BASIC_BLOCK, east = BASIC_BLOCK, above = NONE, then = WINDING_STAIRS_SE),
    If(south = NONE, southBelow = HALFH_CEILING, east = HALFH_CEILING, above = NONE, then = WINDING_STAIRS_SE),

    // then = WINDING_STAIRS_SW
    If(south = NONE, southBelow = BASIC_BLOCK, west = BASIC_BLOCK, above = NONE, then = WINDING_STAIRS_SW),
    If(south = NONE, southBelow = HALFH_CEILING, west = HALFH_CEILING, above = NONE, then = WINDING_STAIRS_SW),

    // then = WINDING_STAIRS_EN
    If(north = BASIC_BLOCK, east = NONE, above = NONE, eastBelow = BASIC_BLOCK, then = WINDING_STAIRS_EN),
    If(north = HALFH_CEILING, east = NONE, above = NONE, eastBelow = HALFH_CEILING, then = WINDING_STAIRS_EN),

    // then = WINDING_STAIRS_ES
    If(south = BASIC_BLOCK, east = NONE, above = NONE, eastBelow = BASIC_BLOCK, then = WINDING_STAIRS_ES),
    If(south = HALFH_CEILING, east = NONE, above = NONE, eastBelow = HALFH_CEILING, then = WINDING_STAIRS_ES),

    // then = WINDING_STAIRS_WN
    If(north = BASIC_BLOCK, west = NONE, above = NONE, westBelow = BASIC_BLOCK, then = WINDING_STAIRS_WN),
    If(north = HALFH_CEILING, west = NONE, above = NONE, westBelow = HALFH_CEILING, then = WINDING_STAIRS_WN),

    // then = WINDING_STAIRS_WS
    If(south = BASIC_BLOCK, west = NONE, above = NONE, westBelow = BASIC_BLOCK, then = WINDING_STAIRS_WS),
    If(south = HALFH_CEILING, west = NONE, above = NONE, westBelow = HALFH_CEILING, then = WINDING_STAIRS_WS),

    // then = WINDING_STAIRS_LOWER_NE
    If(north = NONE, northBelow = BASIC_BLOCK, east = HALFH_FLOOR, above = NONE, then = WINDING_STAIRS_LOWER_NE),
    If(north = NONE, northBelow = HALFH_CEILING, east = HALFH_FLOOR, above = NONE, then = WINDING_STAIRS_LOWER_NE),

    // then = WINDING_STAIRS_LOWER_SE
    If(south = NONE, southBelow = BASIC_BLOCK, east = HALFH_FLOOR, above = NONE, then = WINDING_STAIRS_LOWER_SE),
    If(south = NONE, southBelow = HALFH_CEILING, east = HALFH_FLOOR, above = NONE, then = WINDING_STAIRS_LOWER_SE),

    // then = WINDING_STAIRS_LOWER_SW
    If(south = NONE, southBelow = BASIC_BLOCK, west = HALFH_FLOOR, above = NONE, then = WINDING_STAIRS_LOWER_SW),
    If(south = NONE, southBelow = HALFH_CEILING, west = HALFH_FLOOR, above = NONE, then = WINDING_STAIRS_LOWER_SW),

    // then = WINDING_STAIRS_LOWER_NW
    If(north = NONE, northBelow = BASIC_BLOCK, west = HALFH_FLOOR, above = NONE, then = WINDING_STAIRS_LOWER_NW),
    If(north = NONE, northBelow = HALFH_CEILING, west = HALFH_FLOOR, above = NONE, then = WINDING_STAIRS_LOWER_NW),

    // then = WINDING_STAIRS_LOWER_EN
    If(east = NONE, eastBelow = BASIC_BLOCK, north = HALFH_FLOOR, above = NONE, then = WINDING_STAIRS_LOWER_EN),
    If(east = NONE, eastBelow = HALFH_CEILING, north = HALFH_FLOOR, above = NONE, then = WINDING_STAIRS_LOWER_EN),

    // then = WINDING_STAIRS_LOWER_ES
    If(east = NONE, eastBelow = BASIC_BLOCK, south = HALFH_FLOOR, above = NONE, then = WINDING_STAIRS_LOWER_ES),
    If(east = NONE, eastBelow = HALFH_CEILING, south = HALFH_FLOOR, above = NONE, then = WINDING_STAIRS_LOWER_ES),

    // then = WINDING_STAIRS_LOWER_WN
    If(west = NONE, westBelow = BASIC_BLOCK, north = HALFH_FLOOR, above = NONE, then = WINDING_STAIRS_LOWER_WN),
    If(west = NONE, westBelow = HALFH_CEILING, north = HALFH_FLOOR, above = NONE, then = WINDING_STAIRS_LOWER_WN),

    // then = WINDING_STAIRS_LOWER_WS
    If(west = NONE, westBelow = BASIC_BLOCK, south = HALFH_FLOOR, above = NONE, then = WINDING_STAIRS_LOWER_WS),
    If(west = NONE, westBelow = HALFH_CEILING, south = HALFH_FLOOR, above = NONE, then = WINDING_STAIRS_LOWER_WS),

    // then = WINDING_STAIRS_UPPER_NE
    If(north = HALFH_FLOOR, east = BASIC_BLOCK, eastAbove = NONE, above = NONE, then = WINDING_STAIRS_UPPER_NE),
    If(north = HALFH_FLOOR, east = HALFH_CEILING, eastAbove = NONE, above = NONE, then = WINDING_STAIRS_UPPER_NE),

    // then = WINDING_STAIRS_UPPER_NW
    If(north = HALFH_FLOOR, west = BASIC_BLOCK, westAbove = NONE, above = NONE, then = WINDING_STAIRS_UPPER_NW),
    If(north = HALFH_FLOOR, west = HALFH_CEILING, westAbove = NONE, above = NONE, then = WINDING_STAIRS_UPPER_NW),

    // then = WINDING_STAIRS_UPPER_SE
    If(south = HALFH_FLOOR, east = BASIC_BLOCK, eastAbove = NONE, above = NONE, then = WINDING_STAIRS_UPPER_SE),
    If(south = HALFH_FLOOR, east = HALFH_CEILING, eastAbove = NONE, above = NONE, then = WINDING_STAIRS_UPPER_SE),

    // then = WINDING_STAIRS_UPPER_SW
    If(south = HALFH_FLOOR, west = BASIC_BLOCK, westAbove = NONE, above = NONE, then = WINDING_STAIRS_UPPER_SW),
    If(south = HALFH_FLOOR, west = HALFH_CEILING, westAbove = NONE, above = NONE, then = WINDING_STAIRS_UPPER_SW),

    // then = WINDING_STAIRS_UPPER_EN
    If(east = HALFH_FLOOR, north = BASIC_BLOCK, northAbove = NONE, above = NONE, then = WINDING_STAIRS_UPPER_EN),
    If(east = HALFH_FLOOR, north = HALFH_CEILING, northAbove = NONE, above = NONE, then = WINDING_STAIRS_UPPER_EN),

    // then = WINDING_STAIRS_UPPER_ES
    If(east = HALFH_FLOOR, south = BASIC_BLOCK, southAbove = NONE, above = NONE, then = WINDING_STAIRS_UPPER_ES),
    If(east = HALFH_FLOOR, south = HALFH_CEILING, southAbove = NONE, above = NONE, then = WINDING_STAIRS_UPPER_ES),

    // then = WINDING_STAIRS_UPPER_WN
    If(west = HALFH_FLOOR, north = BASIC_BLOCK, northAbove = NONE, above = NONE, then = WINDING_STAIRS_UPPER_WN),
    If(west = HALFH_FLOOR, north = HALFH_CEILING, northAbove = NONE, above = NONE, then = WINDING_STAIRS_UPPER_WN),

    // then = WINDING_STAIRS_UPPER_WS
    If(west = HALFH_FLOOR, south = BASIC_BLOCK, southAbove = NONE, above = NONE, then = WINDING_STAIRS_UPPER_WS),
    If(west = HALFH_FLOOR, south = HALFH_CEILING, southAbove = NONE, above = NONE, then = WINDING_STAIRS_UPPER_WS),

    // then = WINDOW_TOP_L_NORTH
    If(east = WINDOW_TOP_R_NORTH, westIsnt = WINDOW_TOP_L_NORTH, then = WINDOW_TOP_L_NORTH),
    If(west = WINDOW_TOP_R_NORTH, eastIsnt = WINDOW_TOP_L_NORTH, then = WINDOW_TOP_L_NORTH),
    If(below = THIN_WALL_NORTH, eastIsnt = WINDOW_TOP_L_NORTH, then = WINDOW_TOP_L_NORTH),

    // then = WINDOW_TOP_L_EAST
    If(north = WINDOW_TOP_R_EAST, southIsnt = WINDOW_TOP_L_EAST, then = WINDOW_TOP_L_EAST),
    If(south = WINDOW_TOP_R_EAST, northIsnt = WINDOW_TOP_L_EAST, then = WINDOW_TOP_L_EAST),
    If(below = THIN_WALL_EAST, northIsnt = WINDOW_TOP_L_EAST, then = WINDOW_TOP_L_EAST),

    // then = WINDOW_TOP_L_SOUTH
    If(east = WINDOW_TOP_R_SOUTH, westIsnt = WINDOW_TOP_L_SOUTH, then = WINDOW_TOP_L_SOUTH),
    If(west = WINDOW_TOP_R_SOUTH, eastIsnt = WINDOW_TOP_L_SOUTH, then = WINDOW_TOP_L_SOUTH),
    If(below = THIN_WALL_SOUTH, westIsnt = WINDOW_TOP_L_SOUTH, then = WINDOW_TOP_L_SOUTH),

    // then = WINDOW_TOP_L_WEST
    If(north = WINDOW_TOP_R_WEST, southIsnt = WINDOW_TOP_L_WEST, then = WINDOW_TOP_L_WEST),
    If(south = WINDOW_TOP_R_WEST, northIsnt = WINDOW_TOP_L_WEST, then = WINDOW_TOP_L_WEST),
    If(below = THIN_WALL_WEST, southIsnt = WINDOW_TOP_L_WEST, then = WINDOW_TOP_L_WEST),

    // then = WINDOW_TOP_R_NORTH
    If(east = WINDOW_TOP_L_NORTH, westIsnt = WINDOW_TOP_R_NORTH, then = WINDOW_TOP_R_NORTH),
    If(west = WINDOW_TOP_L_NORTH, eastIsnt = WINDOW_TOP_R_NORTH, then = WINDOW_TOP_R_NORTH),
    If(below = THIN_WALL_NORTH, eastIsnt = WINDOW_TOP_R_NORTH, then = WINDOW_TOP_R_NORTH),

    // then = WINDOW_TOP_R_EAST
    If(north = WINDOW_TOP_L_EAST, southIsnt = WINDOW_TOP_R_EAST, then = WINDOW_TOP_R_EAST),
    If(south = WINDOW_TOP_L_EAST, northIsnt = WINDOW_TOP_R_EAST, then = WINDOW_TOP_R_EAST),
    If(below = THIN_WALL_EAST, northIsnt = WINDOW_TOP_R_EAST, then = WINDOW_TOP_R_EAST),

    // then = WINDOW_TOP_R_SOUTH
    If(east = WINDOW_TOP_L_SOUTH, westIsnt = WINDOW_TOP_R_SOUTH, then = WINDOW_TOP_R_SOUTH),
    If(west = WINDOW_TOP_L_SOUTH, eastIsnt = WINDOW_TOP_R_SOUTH, then = WINDOW_TOP_R_SOUTH),
    If(below = THIN_WALL_SOUTH, westIsnt = WINDOW_TOP_R_SOUTH, then = WINDOW_TOP_R_SOUTH),

    // then = WINDOW_TOP_R_WEST
    If(north = WINDOW_TOP_L_WEST, southIsnt = WINDOW_TOP_R_WEST, then = WINDOW_TOP_R_WEST),
    If(south = WINDOW_TOP_L_WEST, northIsnt = WINDOW_TOP_R_WEST, then = WINDOW_TOP_R_WEST),
    If(below = THIN_WALL_WEST, southIsnt = WINDOW_TOP_R_WEST, then = WINDOW_TOP_R_WEST),
)
