package ch.digorydoo.titanium.engine.prefs

import ch.digorydoo.titanium.engine.camera.CameraSpeed
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.core.ResolutionManager.Monitor
import ch.digorydoo.titanium.engine.i18n.EngineTextId.*
import ch.digorydoo.titanium.engine.i18n.TextLanguage
import ch.digorydoo.titanium.engine.ui.choice.BoolChoice
import ch.digorydoo.titanium.engine.ui.choice.Choice
import ch.digorydoo.titanium.engine.ui.choice.TextChoice

// Note: There are two instances of this menu: One is in game/StartScene, and one is in game/OptionsPage.
class PrefsMenu {
    fun show(onDone: () -> Unit) {
        val reopen = { show(onDone) }

        val choices = listOf(
            TextChoice(PREFS_GAMEPAD_AND_KEYBOARD) {
                showGamepadAndKeyboardMenu(reopen)
            },
            TextChoice(PREFS_MONITOR_AND_RESOLUTION) {
                showMonitorAndResolutionMenu(reopen)
            },
            TextChoice(App.i18n.format(PREFS_TEXT_LANGUAGE, App.prefs.textLanguage.displayText)) {
                showTextLanguageMenu(reopen)
            },
            TextChoice(DONE) {
                App.prefs.saveIfNeeded()
                onDone()
            }
        )

        App.dlg.showChoices(
            choices,
            0,
            lastItemIsDismiss = true,
            playSoundOnOpen = false,
            playSoundOnDismiss = false,
        )
    }

    private fun showGamepadAndKeyboardMenu(onDone: () -> Unit) {
        val reopen = { show(onDone) }

        val choices = listOf(
            BoolChoice(PREFS_SWAP_GAMEPAD_BTNS_ABXY, App.prefs.swapGamepadBtnsABXY) {
                App.prefs.swapGamepadBtnsABXY = it
            },
            BoolChoice(PREFS_SWAP_CAMERA_X, App.prefs.swapCameraX) { App.prefs.swapCameraX = it },
            BoolChoice(PREFS_SWAP_CAMERA_Y, App.prefs.swapCameraY) { App.prefs.swapCameraY = it },
            TextChoice(App.i18n.format(PREFS_CAMERA_SPEED, App.prefs.speedOfCameraControls.displayText)) {
                showCameraSpeedMenu(reopen)
            },
            TextChoice(DONE, onDone),
        )

        App.dlg.showChoices(
            choices,
            0,
            lastItemIsDismiss = true,
            playSoundOnOpen = false,
            playSoundOnDismiss = false,
        )
    }

    private fun showMonitorAndResolutionMenu(onDone: () -> Unit) {
        val reopen = { showMonitorAndResolutionMenu(onDone) }

        val choices: MutableList<Choice> = mutableListOf(
            BoolChoice(PREFS_WINDOW_MODE, !App.prefs.fullscreen) {
                if (it) {
                    App.resolutionMgr.setWindowModeAndUpdatePrefs()
                } else {
                    App.resolutionMgr.setFullscreenAndUpdatePrefs()
                }
            },
            BoolChoice(PREFS_STRETCH_VIEWPORT, App.prefs.stretchViewport) {
                App.resolutionMgr.setStretchViewportAndUpdatePrefs(!App.prefs.stretchViewport)
            },
            BoolChoice(PREFS_SCALE_UI, App.prefs.scaleUI) {
                App.resolutionMgr.setScaleUIAndUpdatePrefs(!App.prefs.scaleUI)
            },
            BoolChoice(PREFS_AUTO_PICK_MONITOR_AND_RESOLUTION, App.prefs.autoPickMonitorAndRes) {
                App.resolutionMgr.setAutoPickMonitorAndResAndUpdatePrefs(!App.prefs.autoPickMonitorAndRes)
            }
        )

        App.resolutionMgr.getAvailableMonitors().forEach { monitor ->
            choices.add(
                TextChoice(monitor.name) {
                    showResolutionsMenu(monitor, recommendedOnly = true, reopen)
                }
            )
        }

        choices.add(TextChoice(DONE, onDone))

        App.dlg.showChoices(
            choices,
            0,
            lastItemIsDismiss = true,
            playSoundOnOpen = false,
            playSoundOnDismiss = false,
        )
    }

    private fun showResolutionsMenu(monitor: Monitor, recommendedOnly: Boolean, onDone: () -> Unit) {
        val resolutions = when (recommendedOnly) {
            true -> App.resolutionMgr.getRecommendedResolutions(monitor)
            false -> App.resolutionMgr.getAvailableResolutions(monitor)
        }

        val choices = resolutions
            .sortedBy { -it.numPixelsX * it.numPixelsY } // sort by negative area: largest first
            .map { res ->
                TextChoice("${res.numPixelsX}x${res.numPixelsY}") {
                    App.prefs.autoPickMonitorAndRes = false
                    App.resolutionMgr.setFullscreenAndUpdatePrefs(monitor, res)
                    onDone()
                }
            }.toMutableList()

        if (recommendedOnly) {
            choices.add(
                TextChoice(MORE) {
                    showResolutionsMenu(monitor, recommendedOnly = false, onDone)
                }
            )
        }

        choices.add(TextChoice(CANCEL, onDone))

        App.dlg.showChoices(
            choices,
            0,
            lastItemIsDismiss = true,
            playSoundOnOpen = false,
            playSoundOnDismiss = false,
        )
    }

    private fun showCameraSpeedMenu(onDone: () -> Unit) {
        val choices = CameraSpeed.entries.map {
            TextChoice(it.displayText) {
                App.prefs.speedOfCameraControls = it
                onDone()
            }
        }.toMutableList()

        choices.add(TextChoice(CANCEL, onDone))

        App.dlg.showChoices(
            choices,
            CameraSpeed.entries.indexOf(App.prefs.speedOfCameraControls),
            lastItemIsDismiss = true,
            playSoundOnOpen = false,
            playSoundOnDismiss = false,
        )
    }

    private fun showTextLanguageMenu(onDone: () -> Unit) {
        val choices = TextLanguage.entries.map {
            TextChoice(it.displayText) {
                App.prefs.textLanguage = it
                App.i18n.setLocale(it.locale)
                onDone()
            }
        }.toMutableList()

        choices.add(TextChoice(CANCEL, onDone))

        App.dlg.showChoices(
            choices,
            TextLanguage.entries.indexOf(App.prefs.textLanguage),
            lastItemIsDismiss = true,
            playSoundOnOpen = false,
            playSoundOnDismiss = false,
        )
    }
}
