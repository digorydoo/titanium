package ch.digorydoo.titanium.engine.prefs

import ch.digorydoo.titanium.engine.camera.CameraSpeed
import ch.digorydoo.titanium.engine.i18n.TextLanguage

// Make sure none of these values are important; the user may throw away the prefs file at any time!
interface Preferences {
    var fullscreen: Boolean
    var nameOfMonitor: String
    var fullscreenResX: Int
    var fullscreenResY: Int
    var autoPickMonitorAndRes: Boolean
    var stretchViewport: Boolean
    var scaleUI: Boolean
    var swapGamepadBtnsABXY: Boolean
    var swapCameraX: Boolean
    var swapCameraY: Boolean
    var speedOfCameraControls: CameraSpeed
    var textLanguage: TextLanguage
}
