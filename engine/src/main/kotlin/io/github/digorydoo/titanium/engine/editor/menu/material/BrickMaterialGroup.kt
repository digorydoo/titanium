package io.github.digorydoo.titanium.engine.editor.menu.material

import ch.digorydoo.kutils.string.initCap
import io.github.digorydoo.titanium.engine.brick.BrickMaterial

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
            BrickMaterial.BLUE_BRICK_WALL,
            BrickMaterial.DARK_RED_BRICK_WALL,
            BrickMaterial.DECORATIVE_WALL,
            BrickMaterial.GREY_BRICK_WALL,
            BrickMaterial.RED_BRICK_WALL,
            BrickMaterial.ROUGH_STONE_WALL,
            BrickMaterial.STONE_WALL_YELLOW,
        )
        CARPET -> listOf(
            BrickMaterial.CARPET_BLUE,
            BrickMaterial.CARPET_BROWN,
            BrickMaterial.CARPET_RED,
        )
        CONCRETE -> listOf(
            BrickMaterial.DARK_GREY_CONCRETE,
            BrickMaterial.DARKER_GREY_CONCRETE,
            BrickMaterial.GREEN_CONCRETE,
            BrickMaterial.GREY_CONCRETE,
            BrickMaterial.ORANGE_CONCRETE,
            BrickMaterial.WHITE_CONCRETE,
            BrickMaterial.YELLOW_CONCRETE,
        )
        NATURE -> listOf(
            BrickMaterial.FOREST_GROUND,
            BrickMaterial.GRASSY_GROUND,
            BrickMaterial.GRASSY_PATH,
        )
        ROAD -> listOf(
            BrickMaterial.ASPHALT_BLUE,
            BrickMaterial.ASPHALT_GREY,
            BrickMaterial.ASPHALT_RED,
            BrickMaterial.ROAD_PAVED,
            BrickMaterial.ROAD_PEBBLES,
            BrickMaterial.TILED_STREET,
            BrickMaterial.GREY_TILED_ROAD,
        )
        WALL_DETAILS -> listOf(
            BrickMaterial.CONCRETE_CELLAR_WINDOW,
            BrickMaterial.CONCRETE_FAKE_DOOR,
            BrickMaterial.CONCRETE_GRAFITTI,
            BrickMaterial.CONCRETE_LARGE_VENTILATION,
            BrickMaterial.CONCRETE_SMALL_VENTILATION,
            BrickMaterial.CONCRETE_SQUARE_WINDOW,
            BrickMaterial.CONCRETE_TALL_WINDOW,
            BrickMaterial.WINDOW_DETAILS,
            BrickMaterial.WINDOW_INTERIOR,
        )
        WOODEN -> listOf(
            BrickMaterial.WOODEN_PLANKS_H,
            BrickMaterial.WOODEN_PLANKS_V_BRITE,
            BrickMaterial.WOODEN_PLANKS_V_DARK,
            BrickMaterial.WOOD_DARK,
            BrickMaterial.WOOD_RED,
        )
        Z0_VARIOUS -> listOf(
            BrickMaterial.GLASS,
            BrickMaterial.METAL_RED,
            BrickMaterial.STANDING_WATER,
        )
        Z9_RESERVED -> listOf(
            BrickMaterial.ZZ_TEST,
        )
    }

    companion object {
        private const val MAX_NESTING_LEVEL = 4

        fun BrickMaterial.findFirstInnermostGroup() =
            entries.firstOrNull { it.materials().contains(this) }
    }
}
