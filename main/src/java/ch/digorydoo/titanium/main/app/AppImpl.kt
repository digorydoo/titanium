package ch.digorydoo.titanium.main.app

import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.kutils.utils.Log.Severity
import ch.digorydoo.kutils.utils.Log.TtyOutput
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.scene.ActiveSceneContent
import ch.digorydoo.titanium.game.core.StateManagerImpl
import ch.digorydoo.titanium.game.i18n.I18nManagerImpl
import ch.digorydoo.titanium.game.s000_start.StartScene
import ch.digorydoo.titanium.game.ui.GameMenuImpl
import ch.digorydoo.titanium.main.font.FontManagerImpl
import ch.digorydoo.titanium.main.input.InputManagerImpl
import ch.digorydoo.titanium.main.input.InputManagerImpl.KeyAction
import ch.digorydoo.titanium.main.opengl.checkGLError
import ch.digorydoo.titanium.main.shader.ShaderManagerImpl
import ch.digorydoo.titanium.main.sound.SoundManagerImpl
import ch.digorydoo.titanium.main.texture.TextureManagerImpl
import org.lwjgl.Version
import org.lwjgl.glfw.Callbacks.glfwFreeCallbacks
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL20.GL_SHADING_LANGUAGE_VERSION
import org.lwjgl.opengl.GL30.*
import kotlin.system.exitProcess

class AppImpl: App() {
    init {
        require(singleton == null) { "_singleton already set" }
        singleton = this
    }

    override val assets = AssetsImpl()
    override lateinit var content: ActiveSceneContent; private set
    override val factory = FactoryImpl()
    override val fontMgr = FontManagerImpl()
    override val gameMenu = GameMenuImpl()
    override val inputMgr = InputManagerImpl()
    override val i18n = I18nManagerImpl()
    override val resolutionMgr = ResolutionManagerImpl()
    override val screenshot = ScreenshotManagerImpl()
    override val shaderMgr = ShaderManagerImpl()
    override val shadowBuffer = ShadowBufferImpl()
    override val soundMgr = SoundManagerImpl()
    override val spawnMgr = SpawnManagerImpl()
    override val state = StateManagerImpl()
    override val textureMgr = TextureManagerImpl()

    override val screenSizeDp get() = resolutionMgr.screenSizeDp
    override val dpToGlFactor get() = resolutionMgr.dpToGlFactor

    var window = 0L; private set

    private var needsClear = 0

    fun run() {
        try {
            initialize()
            startGame()
            loop()
        } catch (e: Exception) {
            Log.error(TAG, "Uncaught exception: ${e.message}\n${e.stackTraceToString()}")
            tearDown(removeLock = false)
            exitProcess(1)
        }

        tearDown(removeLock = true)
        singleton = null
    }

    private fun initialize() {
        assets.initialize() // needs to happen before setting Log options
        val noCrashes = detectCrashesAndPutLockFile() // needs to happen before setting Log options
        val logFile = assets.pathToLogFile

        Log.setOptions(
            ttyLogLevel = Severity.INFO,
            ttyOutput = TtyOutput.EVERYTHING_STDOUT,
            ttyColours = true,
            fileLogLevel = Severity.INFO,
            logFile = logFile,
        )

        Log.truncateLogFile()
        Log.info(TAG, "Logging to $logFile")

        prefs.loadFromFile()

        Log.info(TAG, "LWJGL version: " + Version.getVersion().toString())
        GLFWErrorCallback.createThrow().set()

        // Do not include the hat switch in the list of Joystick buttons.
        // The hat switch has a separate API.
        glfwInitHint(GLFW_JOYSTICK_HAT_BUTTONS, GLFW_FALSE)

        // The first time a window is created the menu bar is created. If GLFW finds a `MainMenu.nib` it is loaded and
        // assumed to contain a menu bar. Disabling the menu bar also breaks creating the window...
        // glfwInitHint(GLFW_COCOA_MENUBAR, GLFW_FALSE)

        if (!glfwInit()) {
            throw IllegalStateException("Unable to initialize GLFW")
        }

        glfwDefaultWindowHints()
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4) // enable OpenGL 4.1
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 1)
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE)
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE)
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE) // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE) // the window will be resizable

        // Don't use full retina resolution. If we wanted to support Retina, we should also set the
        // NSHighResolutionCapable bit in the bundle Info.plist.
        glfwWindowHint(GLFW_COCOA_RETINA_FRAMEBUFFER, GLFW_FALSE)

        // If the following call hangs, make sure that build.gradle sets the jvm option -XstartOnFirstThread.
        // The size we specify here is irrelevant, we're going to change it later.
        window = glfwCreateWindow(320, 200, "Titanium", 0L, 0L)

        if (window == 0L) {
            throw RuntimeException("Failed to create the GLFW window")
        }

        glfwSetWindowAttrib(window, GLFW_FOCUS_ON_SHOW, GLFW_TRUE)

        // Disables resizing, but unfortunately it does not disable the macOS full-screen button.
        // glfwSetWindowAttrib(window, GLFW_RESIZABLE, GLFW_FALSE)

        // Removes the border and title bar.
        // glfwSetWindowAttrib(window, GLFW_DECORATED, GLFW_FALSE)

        // The maximize callback is only called when Alt-clicking the zoom button, but not when going full-screen.
        // glfwSetWindowMaximizeCallback(window) { _, flag ->
        //    Log.info(TAG, "Maximize set to $flag")
        // }

        glfwSetFramebufferSizeCallback(window) { window, fbWidth, fbHeight ->
            if (window != this.window) {
                Log.error(TAG, "onFramebufferSize called for window=$window, but our window is ${this.window}")
            } else {
                needsClear = NEEDS_CLEAR_NUM_FRAMES
                resolutionMgr.onFramebufferSize(fbWidth, fbHeight)
            }
        }

        glfwSetJoystickCallback(::onJoystick)
        glfwSetKeyCallback(window, ::onKey)
        glfwSetCharCallback(window, ::onChar)

        val gamepadId = inputMgr.findAvailableGamepad()

        if (gamepadId >= 0) {
            inputMgr.bindGamepad(gamepadId)
        } else {
            Log.info(TAG, "No game controller was found")
        }

        glfwMakeContextCurrent(window)
        glfwSwapInterval(1) // enable v-sync
        GL.createCapabilities() // attach LWJGL to GLFW's OpenGL context
        glfwShowWindow(window) // make the window visible

        Log.info(TAG, "OpenGL version: " + glGetString(GL_VERSION))
        Log.info(TAG, "GLSL version: " + glGetString(GL_SHADING_LANGUAGE_VERSION))
        checkGLError()

        shadowBuffer.initialize()

        // When we detected an earlier crash, we enfore window mode.
        resolutionMgr.setFullscreenMode(noCrashes && prefs.fullscreen)

        soundMgr.initialize()
        i18n.setLocale(prefs.textLanguage.locale)
    }

    fun onEnterWindowMode() {
        needsClear = NEEDS_CLEAR_NUM_FRAMES
        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL) // show cursor
    }

    fun onEnterFullscreen() {
        needsClear = NEEDS_CLEAR_NUM_FRAMES
        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_HIDDEN) // hide cursor
    }

    fun onViewportUpdated() {
        needsClear = NEEDS_CLEAR_NUM_FRAMES
    }

    private fun onJoystick(joyId: Int, event: Int) {
        if (event == GLFW_CONNECTED) {
            inputMgr.bindGamepad(joyId)
        } else {
            inputMgr.unbindGamepad()
            val otherId = inputMgr.findAvailableGamepad() // maybe another one is still attached

            if (otherId >= 0) {
                inputMgr.bindGamepad(otherId)
            }
        }
    }

    @Suppress("unused")
    private fun onKey(window: Long, key: Int, scanCode: Int, glfwAction: Int, modifiers: Int) {
        if (key == GLFW_KEY_ENTER && glfwAction == GLFW_PRESS && (modifiers and GLFW_MOD_ALT) != 0) {
            // ALT+RETURN: Toggle between fullscreen and window mode.
            resolutionMgr.setFullscreenMode(!prefs.fullscreen)
        } else {
            // We don't pass modifiers down since they are sent as separate keys anyway. GameController needs to
            // detect when a modifier is released while the primary key is still down, so the combined modifiers is
            // quite useless.
            val action = when (glfwAction) {
                GLFW_PRESS -> KeyAction.PRESS
                GLFW_RELEASE -> KeyAction.RELEASE
                else -> return
            }
            inputMgr.onGLFWKeyEvent(key, action)

            if (glfwAction == GLFW_PRESS && (modifiers and GLFW_MOD_CONTROL) != 0) {
                // GLFW does not call onChar() when the control key is down, so we simulate it. This is needed so we
                // can base the editor shortcuts on the char code rather than the raw key codes.

                val charCode = glfwGetKeyName(key, glfwGetKeyScancode(key))
                    ?.takeIf { it.isNotEmpty() }
                    ?.let { it[0].code }
                    ?: 0

                if (charCode != 0) {
                    inputMgr.onGLFWCharEvent(charCode)
                }
            }
        }
    }

    @Suppress("unused")
    private fun onChar(window: Long, charCode: Int) {
        inputMgr.onGLFWCharEvent(charCode)
    }

    private fun tearDown(removeLock: Boolean) {
        content.setAllGelsToZombie()
        content.animate() // zombies will free their resources here

        prefs.saveIfNeeded()

        // Some objects implement finalize() to check whether resources have been properly freed.
        // Invoke garbage collector to (hopefully) see them here.
        System.gc()

        soundMgr.tearDown()

        glfwFreeCallbacks(window)
        glfwDestroyWindow(window)

        shadowBuffer.tearDown()

        glfwTerminate()
        glfwSetErrorCallback(null)?.free()

        if (removeLock) {
            removeCrashLockFile() // must be last so we can detect all crashes
        } else {
            Log.warn(TAG, "Not removing lock, because previous errors were treated as fatal")
        }
    }

    override fun exit() {
        glfwSetWindowShouldClose(window, true)
    }

    private fun startGame() {
        val scene = StartScene()
        content = ActiveSceneContent(scene)
        sceneLoader.load(scene, playSound = false)
    }

    private fun loop() {
        // Since the clear colour also affects the area outside the viewport, it should always be black.
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f)

        while (!glfwWindowShouldClose(window)) {
            glClear(GL_DEPTH_BUFFER_BIT)

            if (needsClear > 0) {
                // We need to clear both framebuffers to make sure the area outside the viewport is black.
                Log.info(TAG, "Clearing frame buffers")
                needsClear-- // we do this multiple times, because it sometimes doesn't immediately work (why?!)
                glClear(GL_COLOR_BUFFER_BIT)
                glfwSwapBuffers(window)
                glClear(GL_COLOR_BUFFER_BIT)
            } else if (!scene.hasSky) {
                // When the sky is disabled, we need to clear the current framebuffer before starting to draw.
                glClear(GL_COLOR_BUFFER_BIT)
            }

            time.maintain()
            content.animate()

            // Rendering shadows
            if (content.scene.hasShadows) {
                shadowBuffer.startDrawingInto()
                content.renderShadows()
                shadowBuffer.stopDrawingInto()
            }

            // Rendering regular objects
            content.renderRegular()

            // Finishing frame
            handleSceneLoading()
            lamps.maintain()
            runEndOfFrameLambdas()
            screenshot.takeIfNecessary(window)

            glfwSwapBuffers(window)
            glfwPollEvents()
            inputMgr.update()
        }
    }

    companion object {
        private val TAG = Log.Tag("AppImpl")
        private const val NEEDS_CLEAR_NUM_FRAMES = 3
    }
}
