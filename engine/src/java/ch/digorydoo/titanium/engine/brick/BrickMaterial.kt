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

    companion object {
        fun fromInt(value: Int) =
            entries.find { shape -> shape.value == value }
                ?: throw Exception("There is no BrickMaterial with value == $value")
    }
}
