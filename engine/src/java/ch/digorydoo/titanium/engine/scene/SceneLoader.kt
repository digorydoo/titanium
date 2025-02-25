package ch.digorydoo.titanium.engine.scene

import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.engine.brick.BrickVolume
import ch.digorydoo.titanium.engine.camera.CameraProps.Mode
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.file.BrickVolumeFileReader
import ch.digorydoo.titanium.engine.i18n.EngineTextId
import ch.digorydoo.titanium.engine.sound.EngineSampleId
import ch.digorydoo.titanium.engine.state.StateManager.RestoredState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

    private val loadingScreen = object: Scene(
        id = null,
        EngineTextId.LOADING,
        fileNameStem = "",
        Lighting.fineDay1200,
        lightingFollowsStoryTime = false,
        hasSky = false,
        hasShadows = false,
    ) {}

    private val scope = CoroutineScope(Dispatchers.Default) // the Default pool contains at least two threads we can use
    var needCallback = false; private set // when true, the main loop needs to call us back (from ActiveSceneContent)
    val sceneBeingLoaded get() = if (stage == Stage.FINISHED) null else loaded.scene

    fun load(scene: Scene, playSound: Boolean = true, restore: RestoredState? = null) {
        Log.info("SceneLoader: Loading $scene")

        require(stage == Stage.FINISHED) { "load called while a scene is being loaded" }
        stage = Stage.BEGIN
        playSoundOnFinish = playSound
        restoredState = restore

        loaded = Artifacts()
        loaded.scene = scene

        App.content.setAllGelsToZombie() // zombies will free their resources before the first callback
        App.content.player = null
        App.content.scene = loadingScreen

        App.content.bricks?.free()
        App.content.bricks = null

        App.spawnMgr.clear()
        App.sky.unload()
        App.shaders.unloadAllNonSharedPrograms()

        // Set the camera mode for the loading screen.
        App.camera.mode = Mode.FIXED_SOURCE
        App.camera.setSource(20.0f, 20.0f, 20.0f, jump = true)
        App.camera.setTarget(10.0f, 10.0f, 0.0f, jump = true)

        App.status.onBeforeLoadScene()

        scope.launch {
            // Load as much as possible here from this thread.
            // Not much to do at the moment, since we must not call any GL functions from here, though.
            nextStage(Stage.BEFORE_READ_BRICKS)
        }
    }

    fun callback() {
        Log.info("SceneLoader: stage is now $stage")

        // This is called from the main thread.
        require(needCallback) { "callback called when needCallback was false" }
        needCallback = false

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
                App.content.scene = scene
                App.content.bricks = loaded.bricks

                App.camera.mode = Mode.SMART
                restoreFromSavegameIfNeeded()

                App.content.scene.enter(restoredState)

                if (scene.hasSky) {
                    App.sky.enable()
                }

                // Needs to happen after enter(), because enter() sets the storyTime!
                if (scene.lightingFollowsStoryTime) {
                    scene.lighting.adaptToStoryTime() // because ActiveSceneContent does not do this on every frame
                }

                App.status.onAfterLoadScene()

                if (playSoundOnFinish) {
                    App.sound.play(EngineSampleId.SCENE_LOADED)
                }

                System.gc() // to prevent initial hiccup
                stage = Stage.FINISHED
            }
            Stage.BEGIN, Stage.FINISHED -> {
                throw Exception("Callback not expected for stage $stage")
            }
        }
    }

    private fun restoreFromSavegameIfNeeded() {
        val state = restoredState ?: return
        App.camera.mode = state.cameraMode ?: Mode.SMART

        val cameraTarget = state.cameraTarget
        val cameraSource = state.cameraSource
        val cameraPhi = state.cameraPhi
        val cameraRho = state.cameraRho

        if (cameraTarget != null) {
            App.camera.setTarget(cameraTarget, jump = true)

            if (cameraSource != null && cameraPhi != null && cameraRho != null) {
                App.camera.setSourceRelativeToTarget(
                    phi = cameraPhi,
                    rho = cameraRho,
                    dist = cameraSource.distanceTo(cameraTarget).toFloat(),
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

    private fun nextStage(s: Stage) {
        scope.launch {
            updateProgress()
            delay(100L)
            stage = s
            needCallback = true
        }
    }

    private fun updateProgress() {
        // We set the progress value to what should be the start of the *next* stage, in order that the progress bar can
        // animate towards that value while the stage is being loaded.

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

        App.status.setLoadingProgress(progress)
    }
}
