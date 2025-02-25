package ch.digorydoo.titanium.engine.brick

import ch.digorydoo.kutils.string.initCap

/**
 * The constants of BrickShape define the shape of the brick. Each shape corresponds to a brick model that will be used
 * for generating the tesselated model data (see BrickModelHolder).
 */
enum class BrickShape(
    val id: Int,
    val relVolume: Float,
    val coversBrickAbove: Boolean = false,
    val coversBrickBelow: Boolean = false,
    val coversNorth: Boolean = false,
    val coversEast: Boolean = false,
    val coversSouth: Boolean = false,
    val coversWest: Boolean = false,
) {
    NONE(id = 0, relVolume = 0.0f),
    BASIC_BLOCK(
        id = 1,
        relVolume = 1.0f,
        coversBrickAbove = true,
        coversBrickBelow = true,
        coversNorth = true,
        coversEast = true,
        coversSouth = true,
        coversWest = true,
    ),
    RAMP_RUN_NORTH(id = 2, relVolume = 0.5f, coversBrickBelow = true, coversNorth = true),
    RAMP_RUN_EAST(id = 3, relVolume = 0.5f, coversBrickBelow = true, coversEast = true),
    RAMP_RUN_SOUTH(id = 4, relVolume = 0.5f, coversBrickBelow = true, coversSouth = true),
    RAMP_RUN_WEST(id = 5, relVolume = 0.5f, coversBrickBelow = true, coversWest = true),
    RAMP_CORNER_NW(id = 6, relVolume = 0.6f),
    RAMP_CORNER_NE(id = 7, relVolume = 0.6f),
    RAMP_CORNER_SE(id = 8, relVolume = 0.6f),
    RAMP_CORNER_SW(id = 9, relVolume = 0.6f),
    VERTICAL_CUT_SE(id = 10, relVolume = 0.6f, coversNorth = true, coversWest = true),
    VERTICAL_CUT_SW(id = 11, relVolume = 0.6f, coversNorth = true, coversEast = true),
    VERTICAL_CUT_NW(id = 12, relVolume = 0.6f, coversEast = true, coversSouth = true),
    VERTICAL_CUT_NE(id = 13, relVolume = 0.6f, coversSouth = true, coversWest = true),
    BEVEL_NN(id = 14, relVolume = 0.8f, coversBrickBelow = true),
    BEVEL_NS(id = 15, relVolume = 0.8f, coversBrickBelow = true),
    BEVEL_SS(id = 16, relVolume = 0.8f, coversBrickBelow = true),
    BEVEL_WW(id = 17, relVolume = 0.8f, coversBrickBelow = true),
    BEVEL_WE(id = 18, relVolume = 0.8f, coversBrickBelow = true),
    BEVEL_EE(id = 19, relVolume = 0.8f, coversBrickBelow = true),
    BEVEL_NW(id = 20, relVolume = 0.8f, coversBrickBelow = true),
    BEVEL_NE(id = 21, relVolume = 0.8f, coversBrickBelow = true),
    BEVEL_SE(id = 22, relVolume = 0.8f, coversBrickBelow = true),
    BEVEL_SW(id = 23, relVolume = 0.8f, coversBrickBelow = true),
    BEVEL_WEST(id = 24, relVolume = 0.8f, coversBrickBelow = true, coversEast = true),
    BEVEL_NORTH(id = 25, relVolume = 0.8f, coversBrickBelow = true, coversSouth = true),
    BEVEL_EAST(id = 26, relVolume = 0.8f, coversBrickBelow = true, coversWest = true),
    BEVEL_SOUTH(id = 27, relVolume = 0.8f, coversBrickBelow = true, coversNorth = true),
    THIN_WALL_NORTH(id = 28, relVolume = 0.17f, coversNorth = true),
    THIN_WALL_EAST(id = 29, relVolume = 0.17f, coversEast = true),
    THIN_WALL_SOUTH(id = 30, relVolume = 0.17f, coversSouth = true),
    THIN_WALL_WEST(id = 31, relVolume = 0.17f, coversWest = true),
    THIN_WALL_DIAGONAL_NW(id = 32, relVolume = 0.23f),
    THIN_WALL_DIAGONAL_NE(id = 33, relVolume = 0.23f),
    THIN_WALL_DIAGONAL_SE(id = 34, relVolume = 0.23f),
    THIN_WALL_DIAGONAL_SW(id = 35, relVolume = 0.23f),
    UPRIGHT_BAR_NW(id = 36, relVolume = 0.1f),
    UPRIGHT_BAR_NE(id = 37, relVolume = 0.1f),
    UPRIGHT_BAR_SE(id = 38, relVolume = 0.1f),
    UPRIGHT_BAR_SW(id = 39, relVolume = 0.1f),
    UPRIGHT_DBL_BAR_NORTH(id = 40, relVolume = 0.15f),
    UPRIGHT_DBL_BAR_EAST(id = 41, relVolume = 0.15f),
    UPRIGHT_DBL_BAR_SOUTH(id = 42, relVolume = 0.15f),
    UPRIGHT_DBL_BAR_WEST(id = 43, relVolume = 0.15f),
    BAR_FRAME_NORTH(id = 44, relVolume = 0.19f),
    BAR_FRAME_EAST(id = 45, relVolume = 0.19f),
    BAR_FRAME_SOUTH(id = 46, relVolume = 0.19f),
    BAR_FRAME_WEST(id = 47, relVolume = 0.19f),
    INVERSE_RAMP_RUN_NORTH(id = 48, relVolume = 0.5f, coversBrickAbove = true, coversSouth = true),
    INVERSE_RAMP_RUN_EAST(id = 49, relVolume = 0.5f, coversBrickAbove = true, coversWest = true),
    INVERSE_RAMP_RUN_SOUTH(id = 50, relVolume = 0.5f, coversBrickAbove = true, coversNorth = true),
    INVERSE_RAMP_RUN_WEST(id = 51, relVolume = 0.5f, coversBrickAbove = true, coversEast = true),
    VERTICAL_BEVEL_SE(id = 52, relVolume = 0.8f, coversNorth = true, coversWest = true),
    VERTICAL_BEVEL_SW(id = 53, relVolume = 0.8f, coversNorth = true, coversEast = true),
    VERTICAL_BEVEL_NW(id = 54, relVolume = 0.8f, coversEast = true, coversSouth = true),
    VERTICAL_BEVEL_NE(id = 55, relVolume = 0.8f, coversSouth = true, coversWest = true),
    RAMP_CORNER_NW_ALT(id = 56, relVolume = 0.6f, coversBrickBelow = true, coversNorth = true, coversWest = true),
    RAMP_CORNER_NE_ALT(id = 57, relVolume = 0.6f, coversBrickBelow = true, coversNorth = true, coversEast = true),
    RAMP_CORNER_SE_ALT(id = 58, relVolume = 0.6f, coversBrickBelow = true, coversEast = true, coversSouth = true),
    RAMP_CORNER_SW_ALT(id = 59, relVolume = 0.6f, coversBrickBelow = true, coversSouth = true, coversWest = true),
    FLAT_FLOOR(id = 60, relVolume = 0.05f, coversBrickBelow = true),
    FLAT_CEILING(id = 61, relVolume = 0.05f, coversBrickAbove = true),
    STAIRS_RUN_NORTH(id = 62, relVolume = 0.5f, coversBrickBelow = true, coversNorth = true),
    STAIRS_RUN_EAST(id = 63, relVolume = 0.5f, coversBrickBelow = true, coversEast = true),
    STAIRS_RUN_SOUTH(id = 64, relVolume = 0.5f, coversBrickBelow = true, coversSouth = true),
    STAIRS_RUN_WEST(id = 65, relVolume = 0.5f, coversBrickBelow = true, coversWest = true),
    THICK_FLOOR(id = 66, relVolume = 0.25f, coversBrickBelow = true),
    THICK_CEILING(id = 67, relVolume = 0.25f, coversBrickAbove = true),
    OCTAGONAL_UPRIGHT_PILLAR(id = 68, relVolume = 0.8f),
    FLAT_WALL_NORTH(id = 69, relVolume = 0.05f, coversNorth = true),
    FLAT_WALL_EAST(id = 70, relVolume = 0.05f, coversEast = true),
    FLAT_WALL_SOUTH(id = 71, relVolume = 0.05f, coversSouth = true),
    FLAT_WALL_WEST(id = 72, relVolume = 0.05f, coversWest = true),
    THICK_WALL_NORTH(id = 73, relVolume = 0.25f, coversNorth = true),
    THICK_WALL_EAST(id = 74, relVolume = 0.25f, coversEast = true),
    THICK_WALL_SOUTH(id = 75, relVolume = 0.25f, coversSouth = true),
    THICK_WALL_WEST(id = 76, relVolume = 0.25f, coversWest = true),
    LOW_BAR_NORTH(id = 77, relVolume = 0.1f),
    LOW_BAR_EAST(id = 78, relVolume = 0.1f),
    LOW_BAR_SOUTH(id = 79, relVolume = 0.1f),
    LOW_BAR_WEST(id = 80, relVolume = 0.1f),
    LOW_BAR_NS(id = 81, relVolume = 0.1f),
    LOW_BAR_WE(id = 82, relVolume = 0.1f),
    THICK_HALFH_LOW_BAR_EAST(id = 83, relVolume = 0.13f),
    THICK_HALFH_LOW_BAR_NORTH(id = 84, relVolume = 0.13f),
    THICK_HALFH_LOW_BAR_SOUTH(id = 85, relVolume = 0.13f),
    THICK_HALFH_LOW_BAR_WEST(id = 86, relVolume = 0.13f),
    VERTICAL_BEVEL_NORTH(id = 87, relVolume = 0.8f, coversSouth = true),
    VERTICAL_BEVEL_EAST(id = 88, relVolume = 0.8f, coversWest = true),
    VERTICAL_BEVEL_SOUTH(id = 89, relVolume = 0.8f, coversNorth = true),
    VERTICAL_BEVEL_WEST(id = 90, relVolume = 0.8f, coversEast = true),
    HIGH_BAR_NORTH(id = 91, relVolume = 0.1f),
    HIGH_BAR_EAST(id = 92, relVolume = 0.1f),
    HIGH_BAR_SOUTH(id = 93, relVolume = 0.1f),
    HIGH_BAR_WEST(id = 94, relVolume = 0.1f),
    WINDOW_TOP_L_NORTH(id = 95, relVolume = 0.15f),
    WINDOW_TOP_R_NORTH(id = 96, relVolume = 0.15f),
    WINDOW_TOP_L_EAST(id = 97, relVolume = 0.15f),
    WINDOW_TOP_R_EAST(id = 98, relVolume = 0.15f),
    WINDOW_TOP_L_SOUTH(id = 99, relVolume = 0.15f),
    WINDOW_TOP_R_SOUTH(id = 100, relVolume = 0.15f),
    WINDOW_TOP_L_WEST(id = 101, relVolume = 0.15f),
    WINDOW_TOP_R_WEST(id = 102, relVolume = 0.15f),
    WINDING_STAIRS_SE(id = 103, relVolume = 0.42f),
    WINDING_STAIRS_SW(id = 104, relVolume = 0.42f),
    WINDING_STAIRS_WS(id = 105, relVolume = 0.42f),
    WINDING_STAIRS_WN(id = 106, relVolume = 0.42f),
    WINDING_STAIRS_NW(id = 107, relVolume = 0.42f),
    WINDING_STAIRS_NE(id = 108, relVolume = 0.42f),
    WINDING_STAIRS_EN(id = 109, relVolume = 0.42f),
    WINDING_STAIRS_ES(id = 110, relVolume = 0.42f),
    WINDING_STAIRS_LOWER_SE(id = 111, relVolume = 0.42f),
    WINDING_STAIRS_LOWER_SW(id = 112, relVolume = 0.42f),
    WINDING_STAIRS_LOWER_WS(id = 113, relVolume = 0.42f),
    WINDING_STAIRS_LOWER_WN(id = 114, relVolume = 0.42f),
    WINDING_STAIRS_LOWER_NW(id = 115, relVolume = 0.42f),
    WINDING_STAIRS_LOWER_NE(id = 116, relVolume = 0.42f),
    WINDING_STAIRS_LOWER_EN(id = 117, relVolume = 0.42f),
    WINDING_STAIRS_LOWER_ES(id = 118, relVolume = 0.42f),
    WINDING_STAIRS_UPPER_SE(id = 119, relVolume = 0.42f),
    WINDING_STAIRS_UPPER_SW(id = 120, relVolume = 0.42f),
    WINDING_STAIRS_UPPER_WS(id = 121, relVolume = 0.42f),
    WINDING_STAIRS_UPPER_WN(id = 122, relVolume = 0.42f),
    WINDING_STAIRS_UPPER_NW(id = 123, relVolume = 0.42f),
    WINDING_STAIRS_UPPER_NE(id = 124, relVolume = 0.42f),
    WINDING_STAIRS_UPPER_EN(id = 125, relVolume = 0.42f),
    WINDING_STAIRS_UPPER_ES(id = 126, relVolume = 0.42f),
    STRAIGHT_STAIRS_NORTH(id = 127, relVolume = 0.42f),
    STRAIGHT_STAIRS_EAST(id = 128, relVolume = 0.42f),
    STRAIGHT_STAIRS_SOUTH(id = 129, relVolume = 0.42f),
    STRAIGHT_STAIRS_WEST(id = 130, relVolume = 0.42f),
    HALFD_WALL_NORTH(id = 131, relVolume = 0.5f, coversNorth = true),
    HALFD_WALL_EAST(id = 132, relVolume = 0.5f, coversEast = true),
    HALFD_WALL_SOUTH(id = 133, relVolume = 0.5f, coversSouth = true),
    HALFD_WALL_WEST(id = 134, relVolume = 0.5f, coversWest = true),
    THIN_WALL_CORNER_NE(id = 135, relVolume = 0.34f, coversNorth = true, coversEast = true),
    THIN_WALL_CORNER_NW(id = 136, relVolume = 0.34f, coversNorth = true, coversWest = true),
    THIN_WALL_CORNER_SE(id = 137, relVolume = 0.34f, coversEast = true, coversSouth = true),
    THIN_WALL_CORNER_SW(id = 138, relVolume = 0.34f, coversSouth = true, coversWest = true),
    THICK_WALL_CORNER_NE(id = 139, relVolume = 0.5f, coversNorth = true, coversEast = true),
    THICK_WALL_CORNER_NW(id = 140, relVolume = 0.5f, coversNorth = true, coversWest = true),
    THICK_WALL_CORNER_SE(id = 141, relVolume = 0.5f, coversEast = true, coversSouth = true),
    THICK_WALL_CORNER_SW(id = 142, relVolume = 0.5f, coversSouth = true, coversWest = true),
    THIN_WALL_TOP_NORTH(id = 143, relVolume = 0.34f, coversBrickAbove = true, coversNorth = true),
    THIN_WALL_TOP_EAST(id = 144, relVolume = 0.34f, coversBrickAbove = true, coversEast = true),
    THIN_WALL_TOP_SOUTH(id = 145, relVolume = 0.34f, coversBrickAbove = true, coversSouth = true),
    THIN_WALL_TOP_WEST(id = 146, relVolume = 0.34f, coversBrickAbove = true, coversWest = true),
    THICK_WALL_TOP_NORTH(id = 147, relVolume = 0.5f, coversBrickAbove = true, coversNorth = true),
    THICK_WALL_TOP_EAST(id = 148, relVolume = 0.5f, coversBrickAbove = true, coversEast = true),
    THICK_WALL_TOP_SOUTH(id = 149, relVolume = 0.5f, coversBrickAbove = true, coversSouth = true),
    THICK_WALL_TOP_WEST(id = 150, relVolume = 0.5f, coversBrickAbove = true, coversWest = true),
    HALFD_WALL_CORNER_NE(id = 151, relVolume = 0.75f, coversNorth = true, coversEast = true),
    HALFD_WALL_CORNER_NW(id = 152, relVolume = 0.75f, coversNorth = true, coversWest = true),
    HALFD_WALL_CORNER_SE(id = 153, relVolume = 0.75f, coversEast = true, coversSouth = true),
    HALFD_WALL_CORNER_SW(id = 154, relVolume = 0.75f, coversSouth = true, coversWest = true),
    HALFD_WALL_TOP_NORTH(id = 155, relVolume = 0.75f, coversBrickAbove = true, coversNorth = true),
    HALFD_WALL_TOP_EAST(id = 156, relVolume = 0.75f, coversBrickAbove = true, coversEast = true),
    HALFD_WALL_TOP_SOUTH(id = 157, relVolume = 0.75f, coversBrickAbove = true, coversSouth = true),
    HALFD_WALL_TOP_WEST(id = 158, relVolume = 0.75f, coversBrickAbove = true, coversWest = true),
    THIN_WALL_CORNER_TOP_NE(
        id = 159,
        relVolume = 0.34f,
        coversBrickAbove = true,
        coversNorth = true,
        coversEast = true,
        coversSouth = true,
        coversWest = true,
    ),
    THIN_WALL_CORNER_TOP_NW(
        id = 160,
        relVolume = 0.34f,
        coversBrickAbove = true,
        coversNorth = true,
        coversEast = true,
        coversSouth = true,
        coversWest = true,
    ),
    THIN_WALL_CORNER_TOP_SE(
        id = 161,
        relVolume = 0.34f,
        coversBrickAbove = true,
        coversNorth = true,
        coversEast = true,
        coversSouth = true,
        coversWest = true,
    ),
    THIN_WALL_CORNER_TOP_SW(
        id = 162,
        relVolume = 0.34f,
        coversBrickAbove = true,
        coversNorth = true,
        coversEast = true,
        coversSouth = true,
        coversWest = true,
    ),
    THICK_WALL_CORNER_TOP_NE(
        id = 163,
        relVolume = 0.64f,
        coversBrickAbove = true,
        coversNorth = true,
        coversEast = true,
        coversSouth = true,
        coversWest = true,
    ),
    THICK_WALL_CORNER_TOP_NW(
        id = 164,
        relVolume = 0.64f,
        coversBrickAbove = true,
        coversNorth = true,
        coversEast = true,
        coversSouth = true,
        coversWest = true,
    ),
    THICK_WALL_CORNER_TOP_SE(
        id = 165,
        relVolume = 0.64f,
        coversBrickAbove = true,
        coversNorth = true,
        coversEast = true,
        coversSouth = true,
        coversWest = true,
    ),
    THICK_WALL_CORNER_TOP_SW(
        id = 166,
        relVolume = 0.64f,
        coversBrickAbove = true,
        coversNorth = true,
        coversEast = true,
        coversSouth = true,
        coversWest = true,
    ),
    HALFD_WALL_CORNER_TOP_NE(
        id = 167,
        relVolume = 0.8f,
        coversBrickAbove = true,
        coversNorth = true,
        coversEast = true,
        coversSouth = true,
        coversWest = true,
    ),
    HALFD_WALL_CORNER_TOP_NW(
        id = 168,
        relVolume = 0.8f,
        coversBrickAbove = true,
        coversNorth = true,
        coversEast = true,
        coversSouth = true,
        coversWest = true,
    ),
    HALFD_WALL_CORNER_TOP_SE(
        id = 169,
        relVolume = 0.8f,
        coversBrickAbove = true,
        coversNorth = true,
        coversEast = true,
        coversSouth = true,
        coversWest = true,
    ),
    HALFD_WALL_CORNER_TOP_SW(
        id = 170,
        relVolume = 0.8f,
        coversBrickAbove = true,
        coversNorth = true,
        coversEast = true,
        coversSouth = true,
        coversWest = true,
    ),
    THICK_HALFH_HIGH_BAR_NORTH(id = 171, relVolume = 0.125f),
    THICK_HALFH_HIGH_BAR_EAST(id = 172, relVolume = 0.125f),
    THICK_HALFH_HIGH_BAR_SOUTH(id = 173, relVolume = 0.125f),
    THICK_HALFH_HIGH_BAR_WEST(id = 174, relVolume = 0.125f),
    HALFH_CEILING_V_CUT_NE(id = 175, relVolume = 0.3f, coversBrickAbove = true),
    HALFH_CEILING_V_CUT_NW(id = 176, relVolume = 0.3f, coversBrickAbove = true),
    HALFH_CEILING_V_CUT_SE(id = 177, relVolume = 0.3f, coversBrickAbove = true),
    HALFH_CEILING_V_CUT_SW(id = 178, relVolume = 0.3f, coversBrickAbove = true),
    HALFH_FLOOR_V_CUT_NE(id = 179, relVolume = 0.3f, coversBrickBelow = true),
    HALFH_FLOOR_V_CUT_NW(id = 180, relVolume = 0.3f, coversBrickBelow = true),
    HALFH_FLOOR_V_CUT_SE(id = 181, relVolume = 0.3f, coversBrickBelow = true),
    HALFH_FLOOR_V_CUT_SW(id = 182, relVolume = 0.3f, coversBrickBelow = true),
    THICK_CEILING_V_CUT_NE(id = 183, relVolume = 0.15f, coversBrickAbove = true),
    THICK_CEILING_V_CUT_NW(id = 184, relVolume = 0.15f, coversBrickAbove = true),
    THICK_CEILING_V_CUT_SE(id = 185, relVolume = 0.15f, coversBrickAbove = true),
    THICK_CEILING_V_CUT_SW(id = 186, relVolume = 0.15f, coversBrickAbove = true),
    THICK_FLOOR_V_CUT_NE(id = 187, relVolume = 0.15f, coversBrickBelow = true),
    THICK_FLOOR_V_CUT_NW(id = 188, relVolume = 0.15f, coversBrickBelow = true),
    THICK_FLOOR_V_CUT_SE(id = 189, relVolume = 0.15f, coversBrickBelow = true),
    THICK_FLOOR_V_CUT_SW(id = 190, relVolume = 0.15f, coversBrickBelow = true),
    HALFH_CEILING(id = 191, relVolume = 0.5f, coversBrickAbove = true),
    HALFH_FLOOR(id = 192, relVolume = 0.5f, coversBrickBelow = true),
    VERTICAL_BEVEL_FULL(id = 193, relVolume = 0.7f),
    VERTICAL_BEVEL_FULL_ALT(id = 194, relVolume = 0.7f),
    THICK_HIGH_BAR_NORTH(id = 195, relVolume = 0.1f),
    THICK_HIGH_BAR_EAST(id = 196, relVolume = 0.1f),
    THICK_HIGH_BAR_SOUTH(id = 197, relVolume = 0.1f),
    THICK_HIGH_BAR_WEST(id = 198, relVolume = 0.1f),
    HALFD_HIGH_BAR_NORTH(id = 199, relVolume = 0.25f),
    HALFD_HIGH_BAR_EAST(id = 200, relVolume = 0.25f),
    HALFD_HIGH_BAR_SOUTH(id = 201, relVolume = 0.25f),
    HALFD_HIGH_BAR_WEST(id = 202, relVolume = 0.25f),
    HALF_RAMP_TOP_NORTH(id = 203, relVolume = 0.75f),
    HALF_RAMP_TOP_EAST(id = 204, relVolume = 0.75f),
    HALF_RAMP_TOP_SOUTH(id = 205, relVolume = 0.75f),
    HALF_RAMP_TOP_WEST(id = 206, relVolume = 0.75f),
    ;

    val displayText = toString().split("_").joinToString(" ") {
        when {
            it.length < 3 -> it.uppercase()
            else -> initCap(it.lowercase())
        }
    }

    companion object {
        fun fromInt(id: Int) =
            entries.find { shape -> shape.id == id }
                ?: throw Exception("There is no BrickShape with id == $id")
    }
}
