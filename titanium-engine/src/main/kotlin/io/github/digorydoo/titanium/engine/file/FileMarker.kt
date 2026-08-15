package io.github.digorydoo.titanium.engine.file

import ch.digorydoo.kutils.file.KDataInputStream

enum class FileMarker(override val value: UShort): KDataInputStream.FileMarker {
    // Brick volume files
    BEGIN_BRICK_VOLUME(0x1BE6u),
    END_BRICK_VOLUME(0x1E0Fu),
    BEGIN_HEADER(0xBEADu),
    END_HEADER(0x4EADu),

    // Mesh files
    BEGIN_MESH_FILE(0x7001u),
    END_MESH_FILE(0x7E02u),
    BEGIN_GEOMETRY(0x7003u),
    END_GEOMETRY(0x7004u),
    POSITIONS(0x7005u),
    NORMALS(0x7006u),
    TEXCOORDS(0x7007u),
    MATERIAL(0x7008u),
    BEGIN_NODE(0x7009u),
    END_NODE(0x700Au),
    NODE_TRANSFORM(0x700Bu),
    GEOMETRY_REF(0x700Cu),
    BEGIN_SKELETON(0x700Du),
    END_SKELETON(0x700Eu),
    COLLECTED_VEC3F(0x700Fu),
    COLLECTED_VEC2F(0x7010u),
    SKELETON_REF(0x7011u),
    BEGIN_JOINT(0x7012u),
    END_JOINT(0x7013u),
    INV_BIND_MATRIX(0x7014u),
    BIND_SHAPE_MATRIX(0x7015u),

    // User preferences
    BEGIN_PREFS_FILE(0x2101u),
    END_PREFS_FILE(0x2102u),
    FULLSCREEN(0x2103u),
    SWAP_CAMERA_X(0x2104u),
    SWAP_CAMERA_Y(0x2105u),
    TEXT_LANGUAGE(0x2106u),
    CAMERA_CONTROLS_SPEED(0x2107u),
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

    // Height map
    BEGIN_HEIGHT_MAP(0x2301u),
    END_HEIGHT_MAP(0x2302u),
    NUM_SAMPLES_X(0x2303u),
    NUM_SAMPLES_Y(0x2304u),
    X_SIZE(0x2305u),
    Y_SIZE(0x2306u),
    Z_VALUES(0x2307u),
    ;

    companion object {
        fun fromUShort(value: UShort) =
            entries.find { shape -> shape.value == value }
                ?: throw Exception("There is no FileMarker with value == $value")
    }
}
