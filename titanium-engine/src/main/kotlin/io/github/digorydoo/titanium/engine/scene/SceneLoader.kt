package io.github.digorydoo.titanium.engine.scene

import ch.digorydoo.kutils.logging.Log
import io.github.digorydoo.titanium.engine.brick.BrickVolume
import io.github.digorydoo.titanium.engine.camera.CameraDirectingMode
import io.github.digorydoo.titanium.engine.camera.CameraInertia
import io.github.digorydoo.titanium.engine.camera.CameraInputMode
import io.github.digorydoo.titanium.engine.core.App
import io.github.digorydoo.titanium.engine.file.BrickVolumeFileReader
import io.github.digorydoo.titanium.engine.gel.GelLayer
import io.github.digorydoo.titanium.engine.gel.SwishFadeGel
import io.github.digorydoo.titanium.engine.sound.EngineSampleId
import io.github.digorydoo.titanium.engine.state.StateManager.RestoredState
import kotlinx.coroutines.delay

class SceneLoader {
    private class Artifacts {
        var scene: Scene? = null
        var bricks: BrickVolume? = null
    }

    private var loaded = Artifacts()

    private enum class Stage {
        BEGIN,
        BEFORE_READ_BRICKS,
        BEFORE_UPDATE_BRICKS,
        BEFORE_LOAD_SKY,
        BEFORE_LOAD_GEL_LIST,
        FINISHING_UP,
        FINISHED
    }

    private var stage = Stage.FINISHED
    private var playSoundOnFinish = true
    private var restoredState: RestoredState? = null

    val sceneBeingLoaded get() = if (stage == Stage.FINISHED) null else loaded.scene

    fun load(scene: Scene, playSound: Boolean = true, restore: RestoredState? = null) {
        Log.info(TAG, "Loading $scene")

        require(stage == Stage.FINISHED) { "load called while a scene is being loaded" }
        stage = Stage.BEGIN
        playSoundOnFinish = playSound
        restoredState = restore

        loaded = Artifacts()
        loaded.scene = scene

        App.content.beginLoading()

        App.spawnMgr.clear()
        App.sky.unload()

        // Set the camera mode for the loading screen.
        val camera = App.camera
        camera.directingMode = CameraDirectingMode.FIXED_SOURCE
        camera.inputMode = CameraInputMode.OFF
        camera.setSource(20.0f, 20.0f, 20.0f, jump = true)
        camera.setTarget(10.0f, 10.0f, 0.0f, jump = true)

        App.hud.onBeforeLoadScene()
        nextStage(Stage.BEFORE_READ_BRICKS)
    }

    private fun nextStage(s: Stage) {
        Log.info(TAG, "stage is $stage")
        App.process.requireMainThread()
        App.process.runAsync {
            updateProgress()
            stage = s
            // Ideally, we should do most of the task here. At the moment, everything is synchronous, so we immediately
            // jump back to the main thread. Add a short delay to give the progress bar some frames for its animation.
            delay(100L)
            App.process.runAtEndOfFrame { finishStage() }
        }
    }

    private fun finishStage() {
        App.process.requireMainThread()

        when (stage) {
            Stage.BEFORE_READ_BRICKS -> {
                // BrickVolumeFileReader.readFile calls GL functions, so it needs to happen from main thread!
                val scene = loaded.scene!!
                loaded.bricks = BrickVolumeFileReader.readFile(scene.brickVolumeFileName, scene.brickTexFileName)
                nextStage(Stage.BEFORE_UPDATE_BRICKS)
            }
            Stage.BEFORE_UPDATE_BRICKS -> {
                loaded.bricks?.updateAll() // calls GL functions
                nextStage(Stage.BEFORE_LOAD_SKY)
            }
            Stage.BEFORE_LOAD_SKY -> {
                val scene = loaded.scene!!

                if (scene.hasSky) {
                    App.sky.load() // calls GL functions and adds gels
                }

                nextStage(Stage.BEFORE_LOAD_GEL_LIST)
            }
            Stage.BEFORE_LOAD_GEL_LIST -> {
                val scene = loaded.scene!!
                App.spawnMgr.load(scene.gelListFileName)
                nextStage(Stage.FINISHING_UP)
            }
            Stage.FINISHING_UP -> {
                val scene = loaded.scene!!
                val content = App.content
                content.scene = scene
                content.bricks = loaded.bricks
                content.finishLoading()

                App.camera.apply {
                    directingMode = CameraDirectingMode.SMART
                    inertia = CameraInertia.NORMAL
                    inputMode = CameraInputMode.FULLY_CONTROLLABLE
                }

                restoreFromSavegameIfNeeded()
                App.content.scene.enter(restoredState)

                if (scene.hasSky) {
                    App.sky.enable()
                }

                // Needs to happen after enter(), because enter() sets the storyTime!
                if (scene.lightingFollowsStoryTime) {
                    scene.lighting.adaptToStoryTime() // because ActiveSceneContent does not do this on every frame
                }

                App.hud.onAfterLoadScene()

                if (playSoundOnFinish) {
                    App.sound.play(EngineSampleId.SCENE_LOADED)
                }

                System.gc() // to prevent initial hiccup
                SwishFadeGel().onCreate(GelLayer.LayerKind.UI_BELOW_DLG)
                stage = Stage.FINISHED
            }
            Stage.BEGIN, Stage.FINISHED -> {
                throw Exception("Callback not expected for stage $stage")
            }
        }
    }

    private fun restoreFromSavegameIfNeeded() {
        val state = restoredState ?: return

        val camera = App.camera
        state.cameraDirectingMode?.let { camera.directingMode = it }
        state.cameraInertia?.let { camera.inertia = it }
        state.cameraInputMode?.let { camera.inputMode }

        val cameraTarget = state.cameraTarget
        val cameraSource = state.cameraSource
        val cameraPhi = state.cameraPhi
        val cameraRho = state.cameraRho

        if (cameraTarget != null) {
            camera.setTarget(cameraTarget, jump = true)

            if (cameraSource != null && cameraPhi != null && cameraRho != null) {
                camera.setSourceRelativeToTarget(
                    phi = cameraPhi,
                    rho = cameraRho,
                    distance = cameraSource.distanceTo(cameraTarget).toFloat(),
                    jump = true,
                )
            }
        }

        App.time.setStoryTime(
            state.storyClockHoursHand ?: 0,
            state.storyClockMinutesHand ?: 0,
            state.storyClockSecondsHand ?: 0,
        )
    }

    private fun updateProgress() {
        // We set the progress value to what should be the start of the *next* stage, in order that the progress bar
        // can animate towards that value while the stage is being loaded.

        val progress = when (stage) {
            Stage.BEGIN -> 0.2f
            Stage.BEFORE_READ_BRICKS -> 0.4f
            Stage.BEFORE_UPDATE_BRICKS -> 0.5f
            Stage.BEFORE_LOAD_SKY -> 0.7f
            Stage.BEFORE_LOAD_GEL_LIST -> 0.9f
            Stage.FINISHING_UP -> 1.0f
            Stage.FINISHED -> 1.0f
        }

        // Assuming unguarded read operations of Floats are safe in Kotlin, and assuming the GameStatusBar only reads
        // the progress value, we simply update the value without Mutex or similar mechanism.

        App.hud.setLoadingProgress(progress)
    }

    companion object {
        private val TAG = Log.Tag("SceneLoader")
    }
}
