package ch.digorydoo.titanium.engine.mesh

// The Collada file needs to refer to the materials by name. Our .msh file will refer to them by value.
enum class MeshMaterial(val value: Int) {
    DEFAULT(0), // material used when mesh did not specifiy any material
    RED_CLOTH(1),
    GREY_STONE(2),
    WOOD(3),
    SILVER_METAL(4),
    RED_METAL(5),
    GOLD(6),
    BLACK_CLOTH(7), // used by CursorGel
    WHITE_CLOTH(8), // used by CursorGel
    BLUE_METAL(9),
    GLOSSY_WHITE(10), // e.g. eyes
    MILITARY_DKGREEN_METAL(11),
    MILITARY_GREEN_METAL(12),
    ;

    companion object {
        fun fromInt(value: Int) =
            entries.find { mat -> mat.value == value }
                ?: throw Exception("There is no MeshMaterial with value == $value")

        fun fromString(s: String): MeshMaterial? =
            entries.find { mat -> mat.name == s }
    }
}
