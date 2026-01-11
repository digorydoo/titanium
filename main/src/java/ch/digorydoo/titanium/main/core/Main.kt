package ch.digorydoo.titanium.main.core

import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.kutils.utils.Log.Severity
import ch.digorydoo.kutils.utils.Log.TtyOutput
import ch.digorydoo.titanium.BuildConfig
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.game.s000_start.StartScene
import ch.digorydoo.titanium.main.input.InputManagerImpl.KeyAction
import ch.digorydoo.titanium.main.opengl.checkGLError
import org.lwjgl.Version
import org.lwjgl.glfw.Callbacks.glfwFreeCallbacks
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL20.GL_SHADING_LANGUAGE_VERSION
import kotlin.system.exitProcess

class Main(private val app: AppImpl) {
    init {
        require(singleton == null) { "singleton already set" }
        singleton = this
    }

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
    }

    private var window = 0L
    private var needsClear = 0

    private fun initialize() {
        val assets = app.assets
        val crashLock = app.crashLock
        val i18n = app.i18n
        val inputMgr = app.inputMgr
        val prefs = app.prefs
        val resolutionMgr = app.resolutionMgr
        val shadowBuffer = app.shadowBuffer

        assets.initialize() // needs to happen before setting Log options
        val noCrashes = crashLock.detectCrashesAndPutLockFile() // needs to happen before setting Log options
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

        Log.info(TAG, "BuildConfig: isWindows=${BuildConfig.isWindows}, isProduction=${BuildConfig.isProduction}")
        Log.info(TAG, "LWJGL version: ${Version.getVersion()}")
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

        // The maximise callback is only called when Alt-clicking the zoom button, but not when going full-screen.
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
        // glfwSwapInterval(1) // enable v-sync; needs restart when changed; unclear if this is useful nowadays
        GL.createCapabilities() // attach LWJGL to GLFW's OpenGL context
        glfwShowWindow(window) // make the window visible

        Log.info(TAG, "OpenGL version: " + glGetString(GL_VERSION))
        Log.info(TAG, "GLSL version: " + glGetString(GL_SHADING_LANGUAGE_VERSION))
        checkGLError()

        shadowBuffer.initialize()

        // When we detected an earlier crash, we enfore window mode.
        resolutionMgr.setFullscreenMode(noCrashes && prefs.fullscreen)

        App.sound.initialize()
        i18n.setLocale(prefs.textLanguage.locale)
    }

    private fun onEnterWindowMode() {
        needsClear = NEEDS_CLEAR_NUM_FRAMES
        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL) // show cursor
    }

    private fun onEnterFullscreen() {
        needsClear = NEEDS_CLEAR_NUM_FRAMES
        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_HIDDEN) // hide cursor
    }

    private fun onViewportUpdated() {
        needsClear = NEEDS_CLEAR_NUM_FRAMES
    }

    private fun onJoystick(joyId: Int, event: Int) {
        val inputMgr = app.inputMgr

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

    private fun onKey(
        @Suppress("unused") window: Long,
        key: Int,
        @Suppress("unused") scanCode: Int,
        glfwAction: Int,
        modifiers: Int,
    ) {
        val inputMgr = app.inputMgr
        val prefs = app.prefs
        val resolutionMgr = app.resolutionMgr

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

                // Kotlin 2.3.0 complains about inaccessible @Nullable with glfwGetKeyName here, but storing the result
                // in a temporary nullable variable makes the warning go away.
                val keyName: String? = glfwGetKeyName(key, glfwGetKeyScancode(key))

                val charCode = keyName
                    ?.takeIf { it.isNotEmpty() }
                    ?.let { it[0].code }
                    ?: 0

                if (charCode != 0) {
                    inputMgr.onGLFWCharEvent(charCode)
                }
            }
        }
    }

    private fun onChar(@Suppress("unused") window: Long, charCode: Int) {
        app.inputMgr.onGLFWCharEvent(charCode)
    }

    private fun tearDown(removeLock: Boolean) {
        val content = app.content
        val crashLock = app.crashLock
        val prefs = app.prefs
        val shadowBuffer = app.shadowBuffer

        content.forEachGel { _, gel -> gel.setZombie() }
        content.animate() // zombies will free their resources here

        prefs.saveIfNeeded()

        // Some objects implement finalize() to check whether resources have been properly freed.
        // Invoke garbage collector to (hopefully) see them here.
        System.gc()

        app.sound.tearDown()

        glfwFreeCallbacks(window)
        glfwDestroyWindow(window)

        shadowBuffer.tearDown()

        glfwTerminate()
        glfwSetErrorCallback(null)?.free()

        if (removeLock) {
            crashLock.removeCrashLockFile() // must be last so we can detect all crashes
        } else {
            Log.warn(TAG, "Not removing lock, because previous errors were treated as fatal")
        }
    }

    private fun startGame() {
        val scene = StartScene()
        app.sceneLoader.load(scene, playSound = false)
    }

    private fun loop() {
        val content = app.content
        val inputMgr = app.inputMgr
        val intermissions = app.intermissions
        val lamps = app.lamps
        val process = app.process
        val screenshots = app.screenshots
        val shadowBuffer = app.shadowBuffer
        val time = app.time

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
            } else if (!content.scene.hasSky) {
                // When the sky is disabled, we need to clear the current framebuffer before starting to draw.
                glClear(GL_COLOR_BUFFER_BIT)
            }

            time.maintain()
            content.animate()
            intermissions.handle()
            lamps.maintain()

            // Rendering shadows
            if (content.scene.hasShadows) {
                shadowBuffer.startDrawingInto()
                content.renderShadows()
                shadowBuffer.stopDrawingInto()
            }

            // Rendering regular objects
            content.renderRegular()

            // Finishing frame
            process.runEndOfFrameLambdas()
            screenshots.takeIfNecessary(window)

            glfwSwapBuffers(window)
            glfwPollEvents()
            inputMgr.update()
        }
    }

    companion object {
        private val TAG = Log.Tag("Main")
        private const val NEEDS_CLEAR_NUM_FRAMES = 3

        // Provide accessors for classes that need access to the concrete implementation of objects
        private var singleton: Main? = null
        val app get() = singleton!!.app
        val resolutionMgr get() = singleton!!.app.resolutionMgr
        val window get() = singleton!!.window
        fun onEnterWindowMode() = singleton!!.onEnterWindowMode()
        fun onEnterFullscreen() = singleton!!.onEnterFullscreen()
        fun onViewportUpdated() = singleton!!.onViewportUpdated()
    }
}

fun main() {
    val app = AppImpl()
    App = app
    Main(app).run()
}
