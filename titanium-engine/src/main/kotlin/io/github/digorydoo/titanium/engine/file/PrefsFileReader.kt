package io.github.digorydoo.titanium.engine.file

import ch.digorydoo.kutils.file.KDataInputStream
import ch.digorydoo.kutils.logging.Log
import io.github.digorydoo.titanium.engine.camera.CameraControlsSpeed
import io.github.digorydoo.titanium.engine.core.App
import io.github.digorydoo.titanium.engine.file.FileMarker.*
import io.github.digorydoo.titanium.engine.file.PrefsFileWriter.Companion.FILENAME
import io.github.digorydoo.titanium.engine.i18n.TextLanguage
import io.github.digorydoo.titanium.engine.prefs.Preferences
import java.io.BufferedInputStream
import java.io.DataInputStream
import java.io.File

class PrefsFileReader private constructor(
    private val stream: KDataInputStream<FileMarker>,
    private val prefs: Preferences,
) {
    private fun read() {
        stream.readExpected(BEGIN_PREFS_FILE)
        var finished = false

        while (!finished) {
            val marker = stream.readMarker()

            when (marker) {
                FULLSCREEN -> prefs.fullscreen = stream.readBoolean()
                NAME_OF_MONITOR -> prefs.nameOfMonitor = stream.readUTF8()
                FULLSCREEN_RES_X -> prefs.fullscreenResX = stream.readInt16()
                FULLSCREEN_RES_Y -> prefs.fullscreenResY = stream.readInt16()
                AUTO_PICK_MONITOR_AND_RES -> prefs.autoPickMonitorAndRes = stream.readBoolean()
                STRETCH_VIEWPORT -> prefs.stretchViewport = stream.readBoolean()
                SCALE_UI -> prefs.scaleUI = stream.readBoolean()
                SWAP_CAMERA_X -> prefs.swapCameraX = stream.readBoolean()
                SWAP_CAMERA_Y -> prefs.swapCameraY = stream.readBoolean()
                SWAP_GAMEPAD_BTNS_ABXY -> prefs.swapGamepadBtnsABXY = stream.readBoolean()
                CAMERA_CONTROLS_SPEED -> prefs.cameraControlsSpeed =
                    CameraControlsSpeed.fromIntOrNull(stream.readInt16()) ?: CameraControlsSpeed.NORMAL
                TEXT_LANGUAGE -> prefs.textLanguage = TextLanguage.fromId(stream.readInt16()) ?: TextLanguage.ENGLISH
                END_PREFS_FILE -> finished = true
                else -> throw Exception("Unexpected marker in prefs file: $marker")
            }
        }
    }

    companion object {
        private val TAG = Log.Tag("PrefsFileReader")

        fun read(prefs: Preferences) {
            val path = App.assets.pathToPrefs(FILENAME)
            val file = File(path)

            if (!file.exists()) {
                Log.info(TAG, "Not reading any prefs, because file does not exist: $path")
            } else {
                Log.info(TAG, "Reading $path")

                file.inputStream()
                    .let { BufferedInputStream(it) }
                    .let { DataInputStream(it) }
                    .use { PrefsFileReader(KDataInputStream(it, FileMarker::fromUShort), prefs).read() }
            }
        }
    }
}
