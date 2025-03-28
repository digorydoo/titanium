package ch.digorydoo.titanium.engine.core

import ch.digorydoo.kutils.point.Point2f
import ch.digorydoo.kutils.point.Point2i
import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.kutils.utils.Moment
import ch.digorydoo.titanium.engine.camera.Camera
import ch.digorydoo.titanium.engine.editor.Editor
import ch.digorydoo.titanium.engine.font.FontManager
import ch.digorydoo.titanium.engine.gel.SpawnManager
import ch.digorydoo.titanium.engine.i18n.I18nManager
import ch.digorydoo.titanium.engine.input.InputManager
import ch.digorydoo.titanium.engine.physics.CollisionManager
import ch.digorydoo.titanium.engine.prefs.PrefsManager
import ch.digorydoo.titanium.engine.scene.ActiveSceneContent
import ch.digorydoo.titanium.engine.scene.Scene
import ch.digorydoo.titanium.engine.scene.SceneLoader
import ch.digorydoo.titanium.engine.shader.ShaderManager
import ch.digorydoo.titanium.engine.sky.Sky
import ch.digorydoo.titanium.engine.sound.SoundManager
import ch.digorydoo.titanium.engine.state.StateManager
import ch.digorydoo.titanium.engine.state.StateManager.RestoredState
import ch.digorydoo.titanium.engine.texture.TextureManager
import ch.digorydoo.titanium.engine.ui.dialogue.DlgManager
import ch.digorydoo.titanium.engine.ui.game_menu.GameMenu
import ch.digorydoo.titanium.engine.ui.game_status.GameStatusBar
import java.io.File

/**
 * This is the abstract part of the App singleton. The implementation of it is in the main module.
 */
abstract class App {
    abstract val assets: Assets
    abstract val content: ActiveSceneContent
    abstract val factory: Factory
    abstract val fontMgr: FontManager
    abstract val gameMenu: GameMenu
    abstract val i18n: I18nManager
    abstract val inputMgr: InputManager
    abstract val resolutionMgr: ResolutionManager
    abstract val screenshot: ScreenshotManager
    abstract val shaderMgr: ShaderManager
    abstract val shadowBuffer: ShadowBuffer
    abstract val soundMgr: SoundManager
    abstract val spawnMgr: SpawnManager
    abstract val state: StateManager
    abstract val textureMgr: TextureManager

    val camera = Camera()
    val collisions = CollisionManager()
    val dlgMgr = DlgManager()
    val editor = Editor()
    val lamps = LampManager()
    val prefs = PrefsManager()
    val sceneLoader = SceneLoader()
    val sky = Sky()
    val status = GameStatusBar()
    val time = GameTime()

    protected abstract val screenSizeDp: Point2i
    protected abstract val dpToGlFactor: Point2f

    class EndOfFrameInfo(val lambda: () -> Unit, var skip: Int)

    private val endOfFrameLambdas = mutableListOf<EndOfFrameInfo>()
    var isAboutToTakeScreenshot = false

    protected fun runEndOfFrameLambdas() {
        if (endOfFrameLambdas.isNotEmpty()) {
            endOfFrameLambdas.removeAll {
                if (it.skip > 0) {
                    it.skip--
                    false // don't remove yet
                } else {
                    it.lambda()
                    true // remove
                }
            }
        }
    }

    protected fun detectCrashesAndPutLockFile(): Boolean {
        // The log file should not be set up yet. Anything we log from here should just go to the tty.
        require(Log.logFile == null) { "Log file was set up too early" }
        val lockFile = File(assets.pathToCrashLockFile)
        var allGood = false

        try {
            if (lockFile.exists()) {
                lockFile.setLastModified(System.currentTimeMillis()) // touch the lock file
                val logFile = File(assets.pathToLogFile)

                if (logFile.exists()) {
                    // The name of the log file will have the date/time when we found the crash (now).
                    // This is the easiest way to ensure that it's very unlikely that the file already exists.
                    val newExt = "-crash-${Moment().formatRevDateTimeForFileName()}.log"
                    val pathWithoutExt = logFile.path.slice(0 ..< logFile.path.length - logFile.extension.length - 1)
                    val movedLogFile = File("$pathWithoutExt$newExt")
                    logFile.renameTo(movedLogFile)
                    Log.warn("A recent crash was detected. The old log was found and moved to: ${movedLogFile.path}")
                } else {
                    Log.warn("A recent crash was detected, but no log was found!")
                }
            } else {
                lockFile.writeText("0") // we could write the process id here, but it doesn't matter
                Log.info("Created new lock file: ${lockFile.path}")
                allGood = true
            }
        } catch (e: Exception) {
            Log.error("Exception while detecting recent crash: ${e.message}")
        }

        return allGood
    }

    protected fun removeCrashLockFile() {
        File(assets.pathToCrashLockFile).delete()
    }

    protected abstract fun exit()

    companion object {
        const val FIXED_ASPECT_RATIO = 16.0f / 9.0f
        const val WORLD_TO_GL_FACTOR = 0.1125f // zoom factor for world coords; does not affect dialogues or menus
        const val MILLIMETRES_PER_INCH = 25.4f // don't change this

        @JvmStatic
        var singleton: App? = null; protected set

        val screenWidthDp get() = singleton!!.screenSizeDp.x
        val screenHeightDp get() = singleton!!.screenSizeDp.y
        val isAboutToTakeScreenshot get() = singleton!!.isAboutToTakeScreenshot

        val assets get() = singleton!!.assets
        val bricks get() = singleton!!.content.bricks!!
        val camera get() = singleton!!.camera
        val collisions get() = singleton!!.collisions
        val content get() = singleton!!.content
        val dlg get() = singleton!!.dlgMgr
        val editor get() = singleton!!.editor
        val factory get() = singleton!!.factory
        val fonts get() = singleton!!.fontMgr
        val gameMenu get() = singleton!!.gameMenu
        val i18n get() = singleton!!.i18n
        val input get() = singleton!!.inputMgr.accessor
        val inputMgr get() = singleton!!.inputMgr
        val lamps get() = singleton!!.lamps
        val player get() = singleton!!.content.player
        val prefs get() = singleton!!.prefs
        val resolutionMgr get() = singleton!!.resolutionMgr
        val scene get() = singleton!!.content.scene
        val sceneLoader get() = singleton!!.sceneLoader
        val screenshot get() = singleton!!.screenshot
        val shaders get() = singleton!!.shaderMgr
        val shadowBuffer get() = singleton!!.shadowBuffer
        val sky get() = singleton!!.sky
        val sound get() = singleton!!.soundMgr
        val spawnMgr get() = singleton!!.spawnMgr
        val state get() = singleton!!.state
        val status get() = singleton!!.status
        val textures get() = singleton!!.textureMgr
        val time get() = singleton!!.time

        fun exit() {
            val singleton = singleton ?: return
            singleton.exit()
        }

        fun load(scene: Scene, playSound: Boolean = true, restore: RestoredState? = null) {
            val singleton = singleton ?: return
            singleton.sceneLoader.load(scene, playSound, restore)
        }

        fun handleSceneLoading() {
            val loader = singleton?.sceneLoader ?: return

            if (loader.needCallback) {
                loader.callback()
            }
        }

        fun runAtEndOfFrame(lambda: () -> Unit) {
            val singleton = singleton ?: return
            // We set skip = 2 so that two frames will be skipped. After the scene has been loaded, spawn points will
            // spawn new gels in the first frame, and we want to wait until all of them have been animated once.
            singleton.endOfFrameLambdas.add(EndOfFrameInfo(lambda, skip = 2))
        }

        fun dpToGlX(dp: Float) = dp * singleton!!.dpToGlFactor.x
        fun dpToGlY(dp: Float) = dp * singleton!!.dpToGlFactor.y
    }
}
