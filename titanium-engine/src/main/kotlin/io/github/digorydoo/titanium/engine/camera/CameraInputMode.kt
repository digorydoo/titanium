package io.github.digorydoo.titanium.engine.camera

enum class CameraInputMode(val value: Int) {
    OFF(0), FULLY_CONTROLLABLE(1), CONSTRAINED(2);

    companion object {
        fun fromIntOrNull(value: Int) =
            entries.find { it.value == value }
    }
}
