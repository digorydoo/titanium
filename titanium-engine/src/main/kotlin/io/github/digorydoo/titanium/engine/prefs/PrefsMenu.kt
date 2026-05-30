package io.github.digorydoo.titanium.engine.prefs

import io.github.digorydoo.titanium.engine.camera.CameraControlsSpeed
import io.github.digorydoo.titanium.engine.core.App
import io.github.digorydoo.titanium.engine.core.ResolutionManager.Monitor
import io.github.digorydoo.titanium.engine.i18n.EngineTextId.*
import io.github.digorydoo.titanium.engine.i18n.TextLanguage

// Note: There are two instances of this menu: One is in game/StartScene, and one is in game/OptionsPage.
class PrefsMenu {
    fun show(onDone: () -> Unit) {
        val reopen = { show(onDone) }
        val prefs = App.prefs

        App.dlg.showDlg<Unit> {
            item {
                textId = PREFS_GAMEPAD_AND_KEYBOARD
                onSelect = { showGamepadAndKeyboardMenu(reopen) }
            }
            item {
                textId = PREFS_MONITOR_AND_RESOLUTION
                onSelect = { showMonitorAndResolutionMenu(reopen) }
            }
            item {
                text = App.i18n.format(PREFS_TEXT_LANGUAGE, prefs.textLanguage.displayText)
                onSelect = { showTextLanguageMenu(reopen) }
            }
            dismiss = item {
                textId = DONE
                onSelect = {
                    prefs.saveIfNeeded()
                    onDone()
                }
            }
        }
    }

    private fun showGamepadAndKeyboardMenu(onDone: () -> Unit) {
        val reopen = { show(onDone) }
        val prefs = App.prefs

        App.dlg.showDlg<Unit> {
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
                onSelect = { showCameraSpeedMenu(reopen) }
            }
            dismiss = item {
                textId = DONE
                onSelect = onDone
            }
        }
    }

    private fun showMonitorAndResolutionMenu(onDone: () -> Unit) {
        val reopen = { showMonitorAndResolutionMenu(onDone) }
        val prefs = App.prefs
        val resolutionMgr = App.resolutionMgr

        App.dlg.showDlg<Unit> {
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
                    onSelect = { showResolutionsMenu(monitor, recommendedOnly = true, reopen) }
                }
            }

            dismiss = item {
                textId = DONE
                onSelect = onDone
            }
        }
    }

    private fun showResolutionsMenu(monitor: Monitor, recommendedOnly: Boolean, onDone: () -> Unit) {
        val prefs = App.prefs
        val resolutionMgr = App.resolutionMgr

        val resolutions = when (recommendedOnly) {
            true -> resolutionMgr.getRecommendedResolutions(monitor)
            false -> resolutionMgr.getAvailableResolutions(monitor)
        }

        App.dlg.showDlg<Unit> {
            resolutions
                .sortedBy { -it.numPixelsX * it.numPixelsY } // sort by negative area: largest first
                .forEach { res ->
                    item {
                        text = "${res.numPixelsX}x${res.numPixelsY}"
                        onSelect = {
                            prefs.autoPickMonitorAndRes = false
                            resolutionMgr.setFullscreenAndUpdatePrefs(monitor, res)
                            onDone()
                        }
                    }
                }

            if (recommendedOnly) {
                item {
                    textId = MORE
                    onSelect = { showResolutionsMenu(monitor, recommendedOnly = false, onDone) }
                }
            }

            dismiss = item {
                textId = DONE
                onSelect = onDone
            }
        }
    }

    private fun showCameraSpeedMenu(onDone: () -> Unit) {
        val prefs = App.prefs

        App.dlg.showDlg<Unit> {
            val dlgDef = this

            CameraControlsSpeed.entries.forEach { speed ->
                item {
                    text = speed.displayText
                    onSelect = {
                        prefs.cameraControlsSpeed = speed
                        onDone()
                    }
                    if (speed == prefs.cameraControlsSpeed) dlgDef.focus = this
                }
            }

            dismiss = item {
                textId = DONE
                onSelect = onDone
            }
        }
    }

    private fun showTextLanguageMenu(onDone: () -> Unit) {
        val prefs = App.prefs

        App.dlg.showDlg<Unit> {
            val dlgDef = this

            TextLanguage.entries.forEach { lang ->
                item {
                    text = lang.displayText
                    onSelect = {
                        prefs.textLanguage = lang
                        App.i18n.setLocale(lang.locale)
                        onDone()
                    }
                    if (lang == prefs.textLanguage) dlgDef.focus = this
                }
            }

            dismiss = item {
                textId = DONE
                onSelect = onDone
            }
        }
    }
}
