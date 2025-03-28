package ch.digorydoo.titanium.engine.file

import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.file.FileMarker.*
import ch.digorydoo.titanium.engine.prefs.Preferences
import java.io.File

class PrefsFileWriter private constructor(
    private val stream: MyDataOutputStream,
    private val prefs: Preferences,
) {
    private fun write() {
        stream.write(BEGIN_PREFS_FILE)
        stream.write(FULLSCREEN, prefs.fullscreen)
        stream.write(NAME_OF_MONITOR, prefs.nameOfMonitor)
        stream.writeInt16(FULLSCREEN_RES_X, prefs.fullscreenResX)
        stream.writeInt16(FULLSCREEN_RES_Y, prefs.fullscreenResY)
        stream.write(AUTO_PICK_MONITOR_AND_RES, prefs.autoPickMonitorAndRes)
        stream.write(STRETCH_VIEWPORT, prefs.stretchViewport)
        stream.write(SCALE_UI, prefs.scaleUI)
        stream.write(SWAP_CAMERA_X, prefs.swapCameraX)
        stream.write(SWAP_CAMERA_Y, prefs.swapCameraY)
        stream.write(SWAP_GAMEPAD_BTNS_ABXY, prefs.swapGamepadBtnsABXY)
        stream.writeInt16(SPEED_OF_CAMERA_CONTROLS, prefs.speedOfCameraControls.id)
        stream.writeInt16(TEXT_LANGUAGE, prefs.textLanguage.id)
        stream.write(END_PREFS_FILE)
    }

    companion object {
        const val FILENAME = "user-prefs.dat"

        fun write(prefs: Preferences) {
            val path = App.assets.pathToPrefs(FILENAME)
            Log.info("PrefsFileWriter: Writing $path")

            val file = File(path)
            MyDataOutputStream.use(file) {
                PrefsFileWriter(it, prefs).write()
            }
        }
    }
}
