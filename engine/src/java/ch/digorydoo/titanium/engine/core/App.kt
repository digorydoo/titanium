package ch.digorydoo.titanium.engine.core

import ch.digorydoo.titanium.engine.camera.Camera
import ch.digorydoo.titanium.engine.editor.Editor
import ch.digorydoo.titanium.engine.font.FontManager
import ch.digorydoo.titanium.engine.gel.SpawnManager
import ch.digorydoo.titanium.engine.i18n.I18nManager
import ch.digorydoo.titanium.engine.input.InputManager
import ch.digorydoo.titanium.engine.intermission.IntermissionManager
import ch.digorydoo.titanium.engine.mesh.MeshManager
import ch.digorydoo.titanium.engine.physics.CollisionManager
import ch.digorydoo.titanium.engine.prefs.PrefsManager
import ch.digorydoo.titanium.engine.scene.ActiveSceneContent
import ch.digorydoo.titanium.engine.scene.SceneLoader
import ch.digorydoo.titanium.engine.shader.ShaderManager
import ch.digorydoo.titanium.engine.sky.Sky
import ch.digorydoo.titanium.engine.sound.SoundManager
import ch.digorydoo.titanium.engine.state.StateManager
import ch.digorydoo.titanium.engine.texture.TextureManager
import ch.digorydoo.titanium.engine.ui.dialogue.DlgManager
import ch.digorydoo.titanium.engine.ui.game_hud.GameHUD
import ch.digorydoo.titanium.engine.ui.game_menu.GameMenu

const val FIXED_ASPECT_RATIO = 16.0f / 9.0f
const val WORLD_TO_GL_FACTOR = 0.1125f // zoom factor for world coords; does not affect dialogues or menus
const val MILLIMETRES_PER_INCH = 25.4f // don't change this

class BricksNotLoadedException: Exception()

/**
 * This abstract class provides access to all global instances and is implemented by Main or unit tests.
 */
abstract class AbstrApp {
    abstract val actions: ActionManager
    abstract val assets: Assets
    abstract val camera: Camera
    abstract val collisions: CollisionManager
    abstract val content: ActiveSceneContent
    abstract val crashLock: CrashLockManager
    abstract val dlg: DlgManager
    abstract val editor: Editor
    abstract val factory: Factory
    abstract val fonts: FontManager
    abstract val gameMenu: GameMenu
    abstract val hud: GameHUD
    abstract val i18n: I18nManager
    abstract val inputMgr: InputManager
    abstract val intermissions: IntermissionManager
    abstract val lamps: LampManager
    abstract val meshes: MeshManager
    abstract val prefs: PrefsManager
    abstract val process: ProcessManager
    abstract val resolutionMgr: ResolutionManager
    abstract val sceneLoader: SceneLoader
    abstract val screenshots: ScreenshotManager
    abstract val shaders: ShaderManager
    abstract val shadowBuffer: ShadowBuffer
    abstract val sky: Sky
    abstract val sound: SoundManager
    abstract val spawnMgr: SpawnManager
    abstract val state: StateManager
    abstract val textures: TextureManager
    abstract val time: GameTime

    // Provide some getters for ease of access of some very common values
    val bricks get() = content.bricks ?: throw BricksNotLoadedException()
    val input get() = inputMgr.accessor
    val isAboutToTakeScreenshot get() = screenshots.isAboutToTakeScreenshot
    val player get() = content.player
    val scene get() = content.scene
}

/**
 * Classes access the instance of AbstrApp through this global. Since it is lateinit, unit tests can't set it to null
 * unfortunately, but they can set it back to a common default MockApp instance as long as it remains stateless.
 * (Using a getter here accessing a nullable private variable would also be possible, but this variant may be slightly
 * better for performance.)
 */
lateinit var App: AbstrApp
