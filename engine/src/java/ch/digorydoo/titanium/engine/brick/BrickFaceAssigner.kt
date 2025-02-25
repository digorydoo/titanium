package ch.digorydoo.titanium.engine.brick

import ch.digorydoo.titanium.engine.brick.BrickMaterial.*

/**
 * This class is used to assign brick face indices to a brick based on its BrickMaterial. Brick face indices define the
 * portion of the brick texture that will be rendered for that brick. The brick texture (scene.brickTexFileName) is
 * divided by TEX_BRICK_WIDTH and TEX_BRICK_WIDTH into brick faces. Indexes are counted from left to right and down.
 * Each material can have one or more up faces, one or more side faces, and one or more down faces.
 */
class BrickFaceAssigner(private val brick: Brick) {
    private interface FaceArray {
        fun pickFaceIdx(u: Int, v: Int): Int
    }

    private class F1x1(private val f0: Int): FaceArray {
        override fun pickFaceIdx(u: Int, v: Int) = f0
    }

    private class F1x2(f0: Int, f1: Int): FaceArray {
        private val arr = arrayOf(f0, f1)
        override fun pickFaceIdx(u: Int, v: Int): Int {
            val pv = if (v >= 0) (v % 2) else 1 - (-v % 2)
            return arr[pv]
        }
    }

    private class F1x3(f0: Int, f1: Int, f2: Int): FaceArray {
        private val arr = arrayOf(f0, f1, f2)
        override fun pickFaceIdx(u: Int, v: Int): Int {
            val pv = if (v >= 0) (v % 3) else 2 - (-v % 3)
            return arr[pv]
        }
    }

    private class F2x2(f0: Int, f1: Int, f2: Int, f3: Int): FaceArray {
        private val arr = arrayOf(f0, f1, f2, f3)
        override fun pickFaceIdx(u: Int, v: Int): Int {
            val pu = if (u >= 0) (u % 2) else 1 - (-u % 2)
            val pv = if (v >= 0) (v % 2) else 1 - (-v % 2)
            return arr[pu + 2 * pv]
        }
    }

    private class F3x3(f0: Int, f1: Int, f2: Int, f3: Int, f4: Int, f5: Int, f6: Int, f7: Int, f8: Int): FaceArray {
        private val arr = arrayOf(f0, f1, f2, f3, f4, f5, f6, f7, f8)
        override fun pickFaceIdx(u: Int, v: Int): Int {
            val pu = if (u >= 0) (u % 3) else 2 - (-u % 3)
            val pv = if (v >= 0) (v % 3) else 2 - (-v % 3)
            return arr[pu + 3 * pv]
        }
    }

    private class BrickFaceArrays(
        val up: FaceArray,
        val side: FaceArray = up,
        val down: FaceArray = side,
    ) {
        fun pickFaces(brick: Brick) {
            val brickCoords = brick.brickCoords
            val bx = brickCoords.x
            val by = brickCoords.y
            val bz = brickCoords.z
            brick.upFaceIdx = up.pickFaceIdx(by, bx)
            brick.downFaceIdx = down.pickFaceIdx(-by, bx)
            brick.northFaceIdx = side.pickFaceIdx(-by, -bz)
            brick.eastFaceIdx = side.pickFaceIdx(-bx, -bz)
            brick.southFaceIdx = side.pickFaceIdx(by, -bz)
            brick.westFaceIdx = side.pickFaceIdx(bx, -bz)
        }
    }

    fun setFacesFromMaterialAndBrickCoords() {
        getBrickFaceArrays(brick.material).pickFaces(brick)
    }

    companion object {
        private fun getBrickFaceArrays(material: BrickMaterial) = when (material) {
            ASPHALT_BLUE -> asphaltBlueFaces
            ASPHALT_RED -> asphaltRedFaces
            BLUE_BRICK_WALL -> blueBrickWallFaces
            CARPET_BLUE -> carpetBlueFaces
            CARPET_BROWN -> carpetBrownFaces
            CARPET_RED -> carpetRedFaces
            CONCRETE_CELLAR_WINDOW -> concreteCellarWindowFaces
            CONCRETE_FAKE_DOOR -> concreteFakeDoorFaces
            CONCRETE_GRAFITTI -> concreteGrafittiFaces
            CONCRETE_LARGE_VENTILATION -> concreteLargeVentilationFaces
            CONCRETE_SMALL_VENTILATION -> concreteSmallVentilationFaces
            CONCRETE_SQUARE_WINDOW -> concreteSquareWindowFaces
            CONCRETE_TALL_WINDOW -> concreteTallWindowFaces
            DARK_GREY_CONCRETE -> darkGreyConcreteFaces
            DARK_RED_BRICK_WALL -> darkRedBrickWallFaces
            FOREST_GROUND -> forestGroundFaces
            GLASS -> glassFaces
            GRASSY_GROUND -> grassyGroundFaces
            GRASSY_PATH -> grassyPathFaces
            GREEN_CONCRETE -> greenConcreteFaces
            GREY_BRICK_WALL -> greyBrickWallFaces
            GREY_CONCRETE -> greyConcreteFaces
            METAL_RED -> metalRedFaces
            ORANGE_CONCRETE -> orangeConcreteFaces
            RED_BRICK_WALL -> redBrickWallFaces
            ROAD_PAVED -> roadPavedFaces
            ROAD_PEBBLES -> roadPebblesFaces
            STANDING_WATER -> glassFaces
            STONE_WALL_YELLOW -> stoneWallYellowFaces
            TILED_STREET -> tiledStreetFaces
            WHITE_CONCRETE -> whiteConcreteFaces
            WINDOW_DETAILS -> windowDetailsFaces
            WINDOW_INTERIOR -> windowInteriorFaces
            WOODEN_PLANKS_H -> woodenPlanksHFaces
            WOODEN_PLANKS_V_BRITE -> woodenPlanksVBriteFaces
            WOODEN_PLANKS_V_DARK -> woodenPlanksVDarkFaces
            WOOD_DARK -> woodDarkFaces
            WOOD_RED -> woodRedFaces
            ZZ_TEST -> zzTestFaces
        }

        private val asphaltBlueFaces = BrickFaceArrays(up = F3x3(9, 10, 11, 36, 37, 38, 63, 64, 65))
        private val asphaltRedFaces = BrickFaceArrays(up = F3x3(12, 13, 14, 39, 40, 41, 66, 67, 68))
        private val blueBrickWallFaces = BrickFaceArrays(up = F3x3(84, 85, 86, 111, 112, 113, 138, 139, 140))
        private val carpetBlueFaces = BrickFaceArrays(up = F3x3(93, 94, 95, 120, 121, 122, 147, 148, 149))
        private val carpetBrownFaces = BrickFaceArrays(up = F3x3(96, 97, 98, 123, 124, 125, 150, 151, 152))
        private val carpetRedFaces = BrickFaceArrays(up = F3x3(90, 91, 92, 117, 118, 119, 144, 145, 146))
        private val darkRedBrickWallFaces = BrickFaceArrays(up = F3x3(333, 334, 335, 360, 361, 362, 387, 388, 389))
        private val glassFaces = BrickFaceArrays(up = F1x1(27))
        private val grassyPathFaces = BrickFaceArrays(up = F3x3(252, 253, 254, 279, 280, 281, 306, 307, 308))
        private val greyBrickWallFaces = BrickFaceArrays(up = F3x3(330, 331, 332, 357, 358, 359, 384, 385, 386))
        private val metalRedFaces = BrickFaceArrays(up = F3x3(261, 262, 263, 288, 289, 290, 315, 316, 317))
        private val redBrickWallFaces = BrickFaceArrays(up = F3x3(87, 88, 89, 114, 115, 116, 141, 142, 143))
        private val roadPavedFaces = BrickFaceArrays(up = F3x3(81, 82, 83, 108, 109, 110, 135, 136, 137))
        private val roadPebblesFaces = BrickFaceArrays(up = F3x3(18, 19, 20, 45, 46, 47, 72, 73, 74))
        private val tiledStreetFaces = BrickFaceArrays(up = F3x3(249, 250, 251, 276, 277, 278, 303, 304, 305))
        private val windowDetailsFaces = BrickFaceArrays(up = F3x3(21, 22, 23, 48, 49, 50, 75, 76, 77))
        private val windowInteriorFaces = BrickFaceArrays(up = F2x2(28, 29, 55, 56))
        private val woodDarkFaces = BrickFaceArrays(up = F3x3(183, 184, 185, 210, 211, 212, 237, 238, 239))
        private val woodRedFaces = BrickFaceArrays(up = F3x3(186, 187, 188, 213, 214, 215, 240, 241, 242))
        private val woodenPlanksHFaces = BrickFaceArrays(up = F3x3(162, 163, 164, 189, 190, 191, 216, 217, 218))
        private val woodenPlanksVBriteFaces = BrickFaceArrays(up = F3x3(168, 169, 170, 195, 196, 197, 222, 223, 224))
        private val woodenPlanksVDarkFaces = BrickFaceArrays(up = F3x3(165, 166, 167, 192, 193, 194, 219, 220, 221))
        private val zzTestFaces = BrickFaceArrays(up = F1x1(0), side = F1x1(1), down = F1x1(2))

        private val greyConcreteFaces = BrickFaceArrays(
            up = F3x3(99, 100, 101, 126, 127, 128, 153, 154, 155),
            side = F3x3(102, 103, 104, 129, 130, 131, 156, 157, 158),
            down = F3x3(105, 106, 107, 132, 133, 134, 159, 160, 161),
        )

        private val darkGreyConcreteFaces = BrickFaceArrays(
            up = F3x3(264, 265, 266, 291, 292, 293, 318, 319, 320),
            side = F3x3(267, 268, 269, 294, 295, 296, 321, 322, 323),
        )

        private val concreteCellarWindowFaces = BrickFaceArrays(
            up = greyConcreteFaces.up,
            side = F1x1(71),
            down = greyConcreteFaces.down
        )

        private val concreteFakeDoorFaces = BrickFaceArrays(
            up = greyConcreteFaces.up,
            side = F1x3(16, 43, 70),
            down = greyConcreteFaces.down
        )

        private val concreteLargeVentilationFaces = BrickFaceArrays(
            up = greyConcreteFaces.up,
            side = F1x1(15),
            down = greyConcreteFaces.down
        )

        private val concreteSmallVentilationFaces = BrickFaceArrays(
            up = greyConcreteFaces.up,
            side = F1x1(17),
            down = greyConcreteFaces.down
        )

        private val concreteSquareWindowFaces = BrickFaceArrays(
            up = greyConcreteFaces.up,
            side = F1x1(44),
            down = greyConcreteFaces.down
        )

        private val concreteTallWindowFaces = BrickFaceArrays(
            up = greyConcreteFaces.up,
            side = F1x2(42, 69),
            down = greyConcreteFaces.down
        )

        private val concreteGrafittiFaces = BrickFaceArrays(
            up = greyConcreteFaces.up,
            side = F3x3(24, 25, 26, 51, 52, 53, 78, 79, 80),
            down = greyConcreteFaces.down
        )

        private val forestGroundFaces = BrickFaceArrays(
            up = F3x3(171, 172, 173, 198, 199, 200, 225, 226, 227),
            side = F3x3(174, 175, 176, 201, 202, 203, 228, 229, 230),
        )

        private val grassyGroundFaces = BrickFaceArrays(
            up = F3x3(243, 244, 245, 270, 271, 272, 297, 298, 299),
            side = F3x3(246, 247, 248, 273, 274, 275, 300, 301, 302),
        )

        private val greenConcreteFaces = BrickFaceArrays(
            up = F3x3(177, 178, 179, 204, 205, 206, 231, 232, 233),
            side = F3x3(180, 181, 182, 207, 208, 209, 234, 235, 236),
        )

        private val orangeConcreteFaces = BrickFaceArrays(
            up = F3x3(3, 4, 5, 30, 31, 32, 57, 58, 59),
            side = F3x3(6, 7, 8, 33, 34, 35, 60, 61, 62),
        )

        private val stoneWallYellowFaces = BrickFaceArrays(
            up = F3x3(324, 325, 326, 351, 352, 353, 378, 379, 380),
            side = F3x3(327, 328, 329, 354, 355, 356, 381, 382, 383),
        )

        private val whiteConcreteFaces = BrickFaceArrays(
            up = F3x3(255, 256, 257, 282, 283, 284, 309, 310, 311),
            side = F3x3(258, 259, 260, 285, 286, 287, 312, 313, 314),
        )
    }
}
