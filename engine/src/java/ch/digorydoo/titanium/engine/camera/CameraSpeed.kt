package ch.digorydoo.titanium.engine.camera

import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.i18n.EngineTextId.*
import ch.digorydoo.titanium.engine.i18n.ITextId

enum class CameraSpeed(val id: Int, val speed: Float, private val textId: ITextId) {
    SLOW(1, 0.75f, PREFS_CAMERA_SLOW_SPEED),
    NORMAL(2, 1.0f, PREFS_CAMERA_NORMAL_SPEED),
    FAST(3, 1.5f, PREFS_CAMERA_FAST_SPEED);

    val displayText get() = App.i18n.getString(textId)

    companion object {
        fun fromIntOrNull(id: Int) =
            entries.find { it.id == id }
    }
}
