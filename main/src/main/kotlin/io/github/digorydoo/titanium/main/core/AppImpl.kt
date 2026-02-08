package io.github.digorydoo.titanium.main.core

import io.github.digorydoo.titanium.engine.camera.Camera
import io.github.digorydoo.titanium.engine.core.AbstrApp
import io.github.digorydoo.titanium.engine.core.ActionManager
import io.github.digorydoo.titanium.engine.core.CrashLockManager
import io.github.digorydoo.titanium.engine.core.GameTime
import io.github.digorydoo.titanium.engine.core.LampManager
import io.github.digorydoo.titanium.engine.editor.Editor
import io.github.digorydoo.titanium.engine.intermission.IntermissionManager
import io.github.digorydoo.titanium.engine.mesh.MeshManager
import io.github.digorydoo.titanium.engine.physics.CollisionManager
import io.github.digorydoo.titanium.engine.prefs.PrefsManager
import io.github.digorydoo.titanium.engine.scene.ActiveSceneContent
import io.github.digorydoo.titanium.engine.scene.SceneLoader
import io.github.digorydoo.titanium.engine.sky.Sky
import io.github.digorydoo.titanium.engine.ui.dialogue.DlgManager
import io.github.digorydoo.titanium.engine.ui.game_hud.GameHUD
import io.github.digorydoo.titanium.game.core.StateManagerImpl
import io.github.digorydoo.titanium.game.i18n.I18nManagerImpl
import io.github.digorydoo.titanium.game.ui.GameMenuImpl
import io.github.digorydoo.titanium.main.font.FontManagerImpl
import io.github.digorydoo.titanium.main.input.GamepadImpl
import io.github.digorydoo.titanium.main.input.InputManagerImpl
import io.github.digorydoo.titanium.main.input.KeyboardImpl
import io.github.digorydoo.titanium.main.shader.ShaderManagerImpl
import io.github.digorydoo.titanium.main.sound.SoundManagerImpl
import io.github.digorydoo.titanium.main.texture.TextureManagerImpl

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
