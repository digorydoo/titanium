package io.github.digorydoo.titanium.engine.camera

import ch.digorydoo.kutils.string.initCap

enum class CameraDirectingMode(val value: Int) {
    FIXED_SOURCE(1), FIXED_DISTANCE(2), SMART(3), MAP(4);

    val displayText = initCap(toString().replace("_", " ").lowercase()) // for Editor

    companion object {
        fun fromIntOrNull(value: Int) =
            entries.find { it.value == value }
    }
}
