package io.github.digorydoo.titanium.engine.prefs

import io.github.digorydoo.titanium.engine.camera.CameraControlsSpeed
import io.github.digorydoo.titanium.engine.core.App
import io.github.digorydoo.titanium.engine.core.ResolutionManager.Monitor
import io.github.digorydoo.titanium.engine.core.ResolutionManager.Resolution
import io.github.digorydoo.titanium.engine.i18n.EngineTextId.*
import io.github.digorydoo.titanium.engine.i18n.TextLanguage
import io.github.digorydoo.titanium.engine.intermission.Intermission

// Note: There are two instances of this menu: One is in game/StartScene, and one is in game/OptionsPage.
class PrefsMenu {
    suspend fun Intermission.show() {
        do {
            val selected = showDlg {
                item {
                    textId = PREFS_GAMEPAD_AND_KEYBOARD
                    onSelect = { showGamepadAndKeyboardMenu() }
                }
                item {
                    textId = PREFS_MONITOR_AND_RESOLUTION
                    onSelect = { showMonitorAndResolutionMenu() }
                }
                item {
                    text = App.i18n.format(PREFS_TEXT_LANGUAGE, App.prefs.textLanguage.displayText)
                    onSelect = { showTextLanguageMenu() }
                }
                dismiss = item { textId = BACK }
            }
        } while (selected.textId != BACK)

        App.prefs.saveIfNeeded()
    }

    private suspend fun Intermission.showGamepadAndKeyboardMenu() {
        val prefs = App.prefs

        do {
            val selected = showDlg {
                itemWithBooleanValue {
                    textId = PREFS_SWAP_GAMEPAD_BTNS_ABXY
                    initialValue = prefs.swapGamepadBtnsABXY
                    onChange = { prefs.swapGamepadBtnsABXY = it }
                }
                itemWithBooleanValue {
                    textId = PREFS_SWAP_CAMERA_X
                    initialValue = prefs.swapCameraX
                    onChange = { prefs.swapCameraX = it }
                }
                itemWithBooleanValue {
                    textId = PREFS_SWAP_CAMERA_Y
                    initialValue = prefs.swapCameraY
                    onChange = { prefs.swapCameraY = it }
                }
                item {
                    text = App.i18n.format(PREFS_CAMERA_SPEED, prefs.cameraControlsSpeed.displayText)
                    onSelect = { showCameraSpeedMenu() }
                }
                dismiss = item { textId = BACK }
            }
        } while (selected.textId != BACK)
    }

    private suspend fun Intermission.showMonitorAndResolutionMenu() {
        val prefs = App.prefs
        val resolutionMgr = App.resolutionMgr

        do {
            val selected = showDlg {
                itemWithBooleanValue {
                    textId = PREFS_WINDOW_MODE
                    initialValue = !prefs.fullscreen
                    onChange = {
                        if (it) {
                            resolutionMgr.setWindowModeAndUpdatePrefs()
                        } else {
                            resolutionMgr.setFullscreenAndUpdatePrefs()
                        }
                    }
                }
                itemWithBooleanValue {
                    textId = PREFS_STRETCH_VIEWPORT
                    initialValue = prefs.stretchViewport
                    onChange = { resolutionMgr.setStretchViewportAndUpdatePrefs(!prefs.stretchViewport) }
                }
                itemWithBooleanValue {
                    textId = PREFS_SCALE_UI
                    initialValue = prefs.scaleUI
                    onChange = { resolutionMgr.setScaleUIAndUpdatePrefs(!prefs.scaleUI) }
                }
                itemWithBooleanValue {
                    textId = PREFS_AUTO_PICK_MONITOR_AND_RESOLUTION
                    initialValue = prefs.autoPickMonitorAndRes
                    onChange = { resolutionMgr.setAutoPickMonitorAndResAndUpdatePrefs(!prefs.autoPickMonitorAndRes) }
                }

                resolutionMgr.getAvailableMonitors().forEach { monitor ->
                    item {
                        text = monitor.name
                        onSelect = { showResolutionsMenu(monitor, recommendedOnly = true) }
                    }
                }

                dismiss = item { textId = BACK }
            }
        } while (selected.textId != BACK)
    }

    private suspend fun Intermission.showResolutionsMenu(monitor: Monitor, recommendedOnly: Boolean) {
        val prefs = App.prefs
        val resolutionMgr = App.resolutionMgr

        val resolutions = when (recommendedOnly) {
            true -> resolutionMgr.getRecommendedResolutions(monitor)
            false -> resolutionMgr.getAvailableResolutions(monitor)
        }

        var selectedRes: Resolution? = null

        do {
            var reopen = false
            showDlg {
                resolutions
                    .sortedBy { -it.numPixelsX * it.numPixelsY } // sort by negative area: largest first
                    .forEach { res ->
                        item {
                            text = "${res.numPixelsX}x${res.numPixelsY}"

                            if (res == selectedRes) {
                                this@showDlg.focus = this // focus item when reopening dlg
                            }

                            onSelect = {
                                prefs.autoPickMonitorAndRes = false
                                resolutionMgr.setFullscreenAndUpdatePrefs(monitor, res)
                                selectedRes = res
                                reopen = true
                            }
                        }
                    }

                if (recommendedOnly) {
                    item {
                        textId = MORE
                        onSelect = {
                            showResolutionsMenu(monitor, recommendedOnly = false)
                            // won't reopen the recommended-only variant of the menu when we come back
                        }
                    }
                }

                dismiss = item { textId = BACK }
            }
        } while (reopen)
    }

    private suspend fun Intermission.showCameraSpeedMenu() {
        val prefs = App.prefs

        showDlg {
            CameraControlsSpeed.entries.forEach { speed ->
                item {
                    text = speed.displayText
                    if (speed == prefs.cameraControlsSpeed) this@showDlg.focus = this
                    onSelect = { prefs.cameraControlsSpeed = speed }
                }
            }

            dismiss = item { textId = BACK }
        }
    }

    private suspend fun Intermission.showTextLanguageMenu() {
        val prefs = App.prefs

        showDlg {
            TextLanguage.entries.forEach { lang ->
                item {
                    text = lang.displayText
                    if (lang == prefs.textLanguage) this@showDlg.focus = this
                    onSelect = {
                        prefs.textLanguage = lang
                        App.i18n.setLocale(lang.locale)
                    }
                }
            }

            dismiss = item { textId = BACK }
        }
    }
}
