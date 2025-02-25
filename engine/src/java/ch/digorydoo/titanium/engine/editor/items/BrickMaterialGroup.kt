package ch.digorydoo.titanium.engine.editor.items

import ch.digorydoo.kutils.string.initCap
import ch.digorydoo.titanium.engine.brick.BrickMaterial
import ch.digorydoo.titanium.engine.brick.BrickMaterial.*

internal enum class BrickMaterialGroup {
    ROOT,
    BRICK_AND_STONE_WALLS,
    CARPET,
    CONCRETE,
    NATURE,
    ROAD,
    WALL_DETAILS,
    WOODEN,
    Z0_VARIOUS,
    Z9_RESERVED;

    val displayText = toString().split("_").joinToString(" ") {
        initCap(it.lowercase())
    }

    fun findParent() =
        entries.firstOrNull { it.subgroups().contains(this) }

    fun findFirstContaining(grp: BrickMaterialGroup, recursion: Int = 0): BrickMaterialGroup? {
        require(recursion < MAX_NESTING_LEVEL) { "BrickMaterialGroups are nested too deep: $this" }
        val subgroups = subgroups()
        if (subgroups.contains(grp)) return this
        else return subgroups.firstOrNull { it.findFirstContaining(grp, recursion + 1) != null }
    }

    fun findFirstContaining(mat: BrickMaterial, recursion: Int = 0): BrickMaterialGroup? {
        require(recursion < MAX_NESTING_LEVEL) { "BrickMaterialGroups are nested too deep: $this" }
        if (materials().contains(mat)) return this
        else return subgroups().firstOrNull { it.findFirstContaining(mat, recursion + 1) != null }
    }

    fun subgroups() = when (this) {
        ROOT -> listOf(
            BRICK_AND_STONE_WALLS,
            CARPET,
            CONCRETE,
            NATURE,
            ROAD,
            WALL_DETAILS,
            WOODEN,
            Z0_VARIOUS,
        )
        Z0_VARIOUS -> listOf(Z9_RESERVED)
        else -> listOf()
    }

    fun materials(): List<BrickMaterial> = when (this) {
        ROOT -> listOf()
        BRICK_AND_STONE_WALLS -> listOf(
            BLUE_BRICK_WALL,
            DARK_RED_BRICK_WALL,
            GREY_BRICK_WALL,
            RED_BRICK_WALL,
            STONE_WALL_YELLOW,
        )
        CARPET -> listOf(
            CARPET_BLUE,
            CARPET_BROWN,
            CARPET_RED,
        )
        CONCRETE -> listOf(
            DARK_GREY_CONCRETE,
            GREEN_CONCRETE,
            GREY_CONCRETE,
            ORANGE_CONCRETE,
            WHITE_CONCRETE,
        )
        NATURE -> listOf(
            FOREST_GROUND,
            GRASSY_GROUND,
            GRASSY_PATH,
        )
        ROAD -> listOf(
            ASPHALT_BLUE,
            ASPHALT_RED,
            ROAD_PAVED,
            ROAD_PEBBLES,
            TILED_STREET,
        )
        WALL_DETAILS -> listOf(
            CONCRETE_CELLAR_WINDOW,
            CONCRETE_FAKE_DOOR,
            CONCRETE_GRAFITTI,
            CONCRETE_LARGE_VENTILATION,
            CONCRETE_SMALL_VENTILATION,
            CONCRETE_SQUARE_WINDOW,
            CONCRETE_TALL_WINDOW,
            WINDOW_DETAILS,
            WINDOW_INTERIOR,
        )
        WOODEN -> listOf(
            WOODEN_PLANKS_H,
            WOODEN_PLANKS_V_BRITE,
            WOODEN_PLANKS_V_DARK,
            WOOD_DARK,
            WOOD_RED,
        )
        Z0_VARIOUS -> listOf(
            GLASS,
            METAL_RED,
            STANDING_WATER,
        )
        Z9_RESERVED -> listOf(
            ZZ_TEST,
        )
    }

    companion object {
        private const val MAX_NESTING_LEVEL = 4

        fun BrickMaterial.findFirstInnermostGroup() =
            entries.firstOrNull { it.materials().contains(this) }
    }
}
