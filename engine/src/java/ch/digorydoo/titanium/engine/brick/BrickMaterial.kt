package ch.digorydoo.titanium.engine.brick

import ch.digorydoo.kutils.string.initCap

/**
 * The BrickMaterial constants define the various materials a brick can appear in.
 */
enum class BrickMaterial(val value: Int) {
    GREY_CONCRETE(0),
    ASPHALT_BLUE(1),
    ROAD_PAVED(2),
    ROAD_PEBBLES(3),
    WOODEN_PLANKS_H(4),
    FOREST_GROUND(5),
    WINDOW_INTERIOR(6),
    GRASSY_GROUND(7),
    GRASSY_PATH(8),
    CARPET_RED(9),
    METAL_RED(10),
    CARPET_BLUE(11),
    CARPET_BROWN(12),
    RED_BRICK_WALL(13),
    BLUE_BRICK_WALL(14),
    WOODEN_PLANKS_V_DARK(15),
    WOODEN_PLANKS_V_BRITE(16),
    GLASS(17),
    ASPHALT_RED(18),
    CONCRETE_LARGE_VENTILATION(19),
    CONCRETE_SMALL_VENTILATION(20),
    CONCRETE_TALL_WINDOW(21),
    CONCRETE_SQUARE_WINDOW(22),
    CONCRETE_CELLAR_WINDOW(23),
    CONCRETE_FAKE_DOOR(24),
    STONE_WALL_YELLOW(25),
    WOOD_DARK(26),
    WOOD_RED(27),
    ORANGE_CONCRETE(28),
    GREEN_CONCRETE(29),
    WHITE_CONCRETE(30),
    STANDING_WATER(31),
    WINDOW_DETAILS(32),
    CONCRETE_GRAFITTI(33),
    TILED_STREET(34),
    DARK_GREY_CONCRETE(35),
    GREY_BRICK_WALL(36),
    DARK_RED_BRICK_WALL(37),
    ZZ_TEST(9999);

    val displayText = initCap(toString().replace("_", " ").lowercase())

    val solid
        get() = when (this) {
            GLASS -> false
            STANDING_WATER -> false
            else -> true
        }

    val elasticity: Float
        get() = when (this) {
            GLASS,
            METAL_RED,
            ZZ_TEST,
            -> 0.8f

            ASPHALT_BLUE,
            ASPHALT_RED,
            BLUE_BRICK_WALL,
            CONCRETE_CELLAR_WINDOW,
            CONCRETE_FAKE_DOOR,
            CONCRETE_GRAFITTI,
            CONCRETE_LARGE_VENTILATION,
            CONCRETE_SMALL_VENTILATION,
            CONCRETE_SQUARE_WINDOW,
            CONCRETE_TALL_WINDOW,
            DARK_GREY_CONCRETE,
            DARK_RED_BRICK_WALL,
            GREEN_CONCRETE,
            GREY_BRICK_WALL,
            GREY_CONCRETE,
            ORANGE_CONCRETE,
            RED_BRICK_WALL,
            ROAD_PAVED,
            STONE_WALL_YELLOW,
            TILED_STREET,
            WHITE_CONCRETE,
            WINDOW_DETAILS,
            WINDOW_INTERIOR,
            -> 0.75f

            CARPET_BLUE,
            CARPET_BROWN,
            CARPET_RED,
            -> 0.6f

            WOODEN_PLANKS_H,
            WOODEN_PLANKS_V_BRITE,
            WOODEN_PLANKS_V_DARK,
            WOOD_DARK,
            WOOD_RED,
            -> 0.52f

            FOREST_GROUND,
            GRASSY_GROUND,
            GRASSY_PATH,
            ROAD_PEBBLES,
            -> 0.5f

            STANDING_WATER,
            -> 0.1f
        }

    val friction: Float
        get() = when (this) {
            GLASS,
            ZZ_TEST,
            -> 0.01f

            METAL_RED,
            -> 0.6f

            WOODEN_PLANKS_H,
            WOODEN_PLANKS_V_BRITE,
            WOODEN_PLANKS_V_DARK,
            WOOD_DARK,
            WOOD_RED,
            -> 0.8f

            ASPHALT_BLUE,
            ASPHALT_RED,
            BLUE_BRICK_WALL,
            CONCRETE_CELLAR_WINDOW,
            CONCRETE_FAKE_DOOR,
            CONCRETE_GRAFITTI,
            CONCRETE_LARGE_VENTILATION,
            CONCRETE_SMALL_VENTILATION,
            CONCRETE_SQUARE_WINDOW,
            CONCRETE_TALL_WINDOW,
            DARK_GREY_CONCRETE,
            DARK_RED_BRICK_WALL,
            GREEN_CONCRETE,
            GREY_BRICK_WALL,
            GREY_CONCRETE,
            ORANGE_CONCRETE,
            RED_BRICK_WALL,
            ROAD_PAVED,
            STONE_WALL_YELLOW,
            TILED_STREET,
            WHITE_CONCRETE,
            WINDOW_DETAILS,
            WINDOW_INTERIOR,
            -> 0.96f

            CARPET_BLUE,
            CARPET_BROWN,
            CARPET_RED,
            -> 0.97f

            FOREST_GROUND,
            GRASSY_GROUND,
            GRASSY_PATH,
            ROAD_PEBBLES,
            -> 0.98f

            STANDING_WATER,
            -> 0.99f
        }

    companion object {
        fun fromInt(value: Int) =
            entries.find { shape -> shape.value == value }
                ?: throw Exception("There is no BrickMaterial with value == $value")
    }
}
