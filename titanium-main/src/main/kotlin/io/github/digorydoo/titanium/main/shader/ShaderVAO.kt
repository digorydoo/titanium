package io.github.digorydoo.titanium.main.shader

import ch.digorydoo.kutils.logging.Log
import io.github.digorydoo.titanium.main.core.LeakDetector
import io.github.digorydoo.titanium.main.opengl.checkGLError
import org.lwjgl.opengl.ARBVertexArrayObject.glBindVertexArray
import org.lwjgl.opengl.GL30.glDeleteVertexArrays
import org.lwjgl.opengl.GL30.glGenVertexArrays

// Wrapper around a GL vertex array object
class ShaderVAO {
    private var id = -1
    private val leakDetector = LeakDetector(TAG.name, initiallyValid = false)

    fun create() {
        require(id == -1) { "VAO already created" }

        val arr = intArrayOf(0)
        glGenVertexArrays(arr)
        checkGLError()

        id = arr[0]
        require(id >= 0) { "Failed to create VAO" }
        leakDetector.resourceValid = true
    }

    fun free() {
        require(id >= 0) { "VAO is invalid" }
        glDeleteVertexArrays(id)
        checkGLError()
        id = -1
        leakDetector.resourceValid = false
    }

    fun bind() {
        require(id >= 0) { "VAO is invalid" }
        glBindVertexArray(id)
        checkGLError()
    }

    companion object {
        private val TAG = Log.Tag("ShaderVAO")
    }
}
