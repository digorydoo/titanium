package io.github.digorydoo.titanium.main.core

import ch.digorydoo.kutils.logging.Log
import io.github.digorydoo.titanium.engine.core.GameLoop
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL11.*

class GameLoopImpl(private val app: AppImpl): GameLoop() {
    private var needsClear = 0

    fun needsClear() {
        needsClear = NEEDS_CLEAR_NUM_FRAMES
    }

    fun loop(window: Long) {
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

            time.tick(token)
            content.tick(token)
            intermissions.tick(token)
            lamps.tick(token)

            // Rendering shadows
            if (content.scene.hasShadows) {
                shadowBuffer.startDrawingInto()
                content.renderShadows(token)
                shadowBuffer.stopDrawingInto()
            }

            // Rendering regular objects
            content.renderRegular(token)

            // Finishing frame
            process.tick(token) // runs the end-of-frame lambdas
            screenshots.takeIfNecessary(window)

            glfwSwapBuffers(window)
            glfwPollEvents()
            inputMgr.tick(token) // updates controller state
        }
    }

    fun tearDown() {
        app.content.apply {
            forEachGel { _, gel -> gel.setZombie() }
            tick(token) // zombies will free their resources here
        }
    }

    companion object {
        private val TAG = Log.Tag("GameLoopImpl")
        private const val NEEDS_CLEAR_NUM_FRAMES = 3
    }
}
