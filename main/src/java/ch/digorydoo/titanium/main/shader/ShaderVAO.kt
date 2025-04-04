package ch.digorydoo.titanium.main.shader

import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.main.opengl.checkGLError
import org.lwjgl.opengl.ARBVertexArrayObject.glBindVertexArray
import org.lwjgl.opengl.GL30.glDeleteVertexArrays
import org.lwjgl.opengl.GL30.glGenVertexArrays

// Wrapper around a GL vertex array object
class ShaderVAO {
    private var id = -1

    fun create() {
        require(id == -1) { "VAO already created" }

        val arr = intArrayOf(0)
        glGenVertexArrays(arr)
        checkGLError()

        id = arr[0]
        require(id >= 0) { "Failed to create VAO" }
    }

    fun free() {
        require(id >= 0) { "VAO is invalid" }
        glDeleteVertexArrays(id)
        id = -1
        checkGLError()
    }

    @Suppress("removal")
    protected fun finalize() {
        // Check that unload has been called. We can't throw from finalize, so log only.
        if (id >= 0) Log.error(TAG, "ShaderVAO still valid at finalize")
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
