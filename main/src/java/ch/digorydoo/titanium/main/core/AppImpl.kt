package ch.digorydoo.titanium.main.core

import ch.digorydoo.titanium.engine.camera.Camera
import ch.digorydoo.titanium.engine.core.AbstrApp
import ch.digorydoo.titanium.engine.core.ActionManager
import ch.digorydoo.titanium.engine.core.CrashLockManager
import ch.digorydoo.titanium.engine.core.GameTime
import ch.digorydoo.titanium.engine.core.LampManager
import ch.digorydoo.titanium.engine.editor.Editor
import ch.digorydoo.titanium.engine.intermission.IntermissionManager
import ch.digorydoo.titanium.engine.mesh.MeshManager
import ch.digorydoo.titanium.engine.physics.CollisionManager
import ch.digorydoo.titanium.engine.prefs.PrefsManager
import ch.digorydoo.titanium.engine.scene.ActiveSceneContent
import ch.digorydoo.titanium.engine.scene.SceneLoader
import ch.digorydoo.titanium.engine.sky.Sky
import ch.digorydoo.titanium.engine.ui.dialogue.DlgManager
import ch.digorydoo.titanium.engine.ui.game_hud.GameHUD
import ch.digorydoo.titanium.game.core.StateManagerImpl
import ch.digorydoo.titanium.game.i18n.I18nManagerImpl
import ch.digorydoo.titanium.game.ui.GameMenuImpl
import ch.digorydoo.titanium.main.font.FontManagerImpl
import ch.digorydoo.titanium.main.input.GamepadImpl
import ch.digorydoo.titanium.main.input.InputManagerImpl
import ch.digorydoo.titanium.main.input.KeyboardImpl
import ch.digorydoo.titanium.main.shader.ShaderManagerImpl
import ch.digorydoo.titanium.main.sound.SoundManagerImpl
import ch.digorydoo.titanium.main.texture.TextureManagerImpl

class AppImpl: AbstrApp() {
    override val actions = ActionManager()
    override val assets = AssetsImpl()
    override val camera = Camera()
    override val collisions = CollisionManager()
    override val content = ActiveSceneContent()
    override val crashLock = CrashLockManager()
    override val dlg = DlgManager()
    override val editor = Editor()
    override val factory = FactoryImpl()
    override val fonts = FontManagerImpl()
    override val gameMenu = GameMenuImpl()
    override val hud = GameHUD()
    override val i18n = I18nManagerImpl()
    override val inputMgr = InputManagerImpl(GamepadImpl(), KeyboardImpl())
    override val intermissions = IntermissionManager()
    override val lamps = LampManager()
    override val meshes = MeshManager()
    override val prefs = PrefsManager()
    override val process = ProcessManagerImpl()
    override val resolutionMgr = ResolutionManagerImpl()
    override val sceneLoader = SceneLoader()
    override val screenshots = ScreenshotManagerImpl()
    override val shaders = ShaderManagerImpl()
    override val shadowBuffer = ShadowBufferImpl()
    override val sky = Sky()
    override val sound = SoundManagerImpl()
    override val spawnMgr = SpawnManagerImpl()
    override val state = StateManagerImpl()
    override val textures = TextureManagerImpl()
    override val time = GameTime()
}
