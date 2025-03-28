package ch.digorydoo.titanium.engine.file

enum class FileMarker(val value: UShort) {
    // Brick volume files
    BEGIN_BRICK_VOLUME(0x1BE6u),
    END_BRICK_VOLUME(0x1E0Fu),
    BEGIN_HEADER(0xBEADu),
    END_HEADER(0x4EADu),

    // Mesh files
    BEGIN_MESH_FILE(0x70BEu),
    END_MESH_FILE(0x7E0Fu),
    BEGIN_GEOMETRY(0x7001u),
    END_GEOMETRY(0x7002u),
    POSITIONS(0x7003u),
    NORMALS(0x7004u),
    TEXCOORDS(0x7005u),
    MATERIAL(0x7006u),
    BEGIN_NODE(0x7007u),
    END_NODE(0x7008u),
    MATRIX(0x7009u),
    COLLECTED_POINT3F(0x700Au),
    COLLECTED_POINT2F(0x700Bu),
    GEOMETRY_REF(0x700Cu),
    BEGIN_DIVISION(0x700Du),
    END_DIVISION(0x700Eu),

    // User preferences
    BEGIN_PREFS_FILE(0x2101u),
    END_PREFS_FILE(0x2102u),
    FULLSCREEN(0x2103u),
    SWAP_CAMERA_X(0x2104u),
    SWAP_CAMERA_Y(0x2105u),
    TEXT_LANGUAGE(0x2106u),
    SPEED_OF_CAMERA_CONTROLS(0x2107u),
    NAME_OF_MONITOR(0x2108u),
    FULLSCREEN_RES_X(0x2109u),
    FULLSCREEN_RES_Y(0x210au),
    AUTO_PICK_MONITOR_AND_RES(0x210bu),
    STRETCH_VIEWPORT(0x210cu),
    SCALE_UI(0x210du),
    SWAP_GAMEPAD_BTNS_ABXY(0x210eu),

    // Save game
    BEGIN_SAVE_GAME(0x2201u),
    END_SAVE_GAME(0x2202u),
    SCENE_TITLE(0x2203u),
    SAVE_DATE(0x2204u),
    SCREENSHOT(0x2205u),
    END_OF_SUMMARY(0x2210u),
    INT_VALUES(0x2211u),
    POINT3F_VALUES(0x2212u),
    FLOAT_VALUES(0x2213u),
    ;

    companion object {
        fun fromUShort(value: UShort) =
            entries.find { shape -> shape.value == value }
                ?: throw Exception("There is no FileMarker with value == $value")
    }
}
