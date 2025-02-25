package ch.digorydoo.titanium.engine.prefs

import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.engine.camera.CameraSpeed
import ch.digorydoo.titanium.engine.file.PrefsFileReader
import ch.digorydoo.titanium.engine.file.PrefsFileWriter
import ch.digorydoo.titanium.engine.i18n.TextLanguage
import kotlin.properties.Delegates

class PrefsManager: Preferences {
    private var changed = false
    private fun <T> watch(initial: T) = Delegates.observable<T>(initial) { _, _, _ -> changed = true }

    override var fullscreen: Boolean by watch(initial = true)
    override var nameOfMonitor: String by watch(initial = "")
    override var fullscreenResX: Int by watch(initial = 0)
    override var fullscreenResY: Int by watch(initial = 0)
    override var autoPickMonitorAndRes: Boolean by watch(initial = true)
    override var stretchViewport: Boolean by watch(initial = false)
    override var scaleUI: Boolean by watch(initial = true)
    override var swapCameraX: Boolean by watch(initial = false)
    override var swapCameraY: Boolean by watch(initial = true)
    override var cameraSpeed: CameraSpeed by watch(initial = CameraSpeed.NORMAL)
    override var textLanguage: TextLanguage by watch(initial = TextLanguage.ENGLISH)

    fun loadFromFile() {
        try {
            PrefsFileReader.read(this)
        } catch (e: Exception) {
            Log.error("Failed to read prefs file: ${e.message}")
        }

        changed = false
    }

    fun saveIfNeeded() {
        if (!changed) return

        try {
            PrefsFileWriter.write(this)
        } catch (e: Exception) {
            Log.error("Failed to write prefs file: ${e.message}")
        }

        changed = false
    }
}
