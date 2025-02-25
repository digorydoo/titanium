package ch.digorydoo.titanium.game.core

enum class SpawnObjType {
    BENCH_1,
    GNARLED_TREE_LARGE,
    GNARLED_TREE_MEDIUM,
    GNARLED_TREE_SMALL,
    RAILING_1,
    RAILING_2,
    ROBOT_POLICEMAN,
    ROUND_TREE,
    SIGN_1,
    STONE_1,
    STREET_LAMP_TRADITIONAL,
    TEST_GEL,
    VASE;

    val id = toString().lowercase().replace("_", "-")

    companion object {
        fun fromString(s: String): SpawnObjType =
            entries.find { it.id == s } ?: throw Exception("Unknown SpawnObjType: $s")
    }
}
