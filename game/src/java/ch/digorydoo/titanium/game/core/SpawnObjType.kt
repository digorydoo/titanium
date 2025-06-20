package ch.digorydoo.titanium.game.core

enum class SpawnObjType {
    BALL_R25CM,
    BALL_R33CM,
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
    VASE_H1M;

    val id = toString().lowercase().replace("_", "-")

    companion object {
        fun fromString(s: String): SpawnObjType =
            entries.find { it.id == s } ?: throw Exception("Unknown SpawnObjType: $s")
    }
}
