package ch.digorydoo.titanium.main.shader

import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.main.opengl.checkGLError
import org.lwjgl.opengl.GL20.*
import java.nio.FloatBuffer

// Wrapper around a GL vertex buffer object
class ShaderVBO {
    /**
     * DYNAMIC: The data will be modified repeatedly and used many times
     * STATIC: The data will be modified once and used many times
     * STREAM: The data will be modified once and used at most a few times
     *
     * COPY: The data is modified by reading data from GL, and used as the source for GL drawing
     * DRAW: The data is modified by the application, and used as the source for GL drawing
     * READ: The data is modified by reading data from GL, and used to return that data when queried by the application
     */
    enum class Type(val glValue: Int) {
        DYNAMIC_DRAW(GL_DYNAMIC_DRAW),
        STATIC_DRAW(GL_STATIC_DRAW),
    }

    private var id = -1
    private var type = Type.STATIC_DRAW

    fun create(type: Type) {
        require(id < 0) { "VBO already created" }
        this.type = type

        val arr = intArrayOf(0)
        glGenBuffers(arr)
        checkGLError()

        id = arr[0]
        require(id >= 0) { "Failed to create VBO" }
    }

    fun free() {
        require(id >= 0) { "VBO is invalid" }
        glDeleteBuffers(id)
        id = -1
        checkGLError()
    }

    @Suppress("removal")
    protected fun finalize() {
        // Check that unload has been called. We can't throw from finalize, so log only.
        if (id >= 0) Log.error(TAG, "ShaderVBO still valid at finalize")
    }

    fun bind() {
        require(id >= 0) { "VBO is invalid" }
        glBindBuffer(GL_ARRAY_BUFFER, id)
        checkGLError()
    }

    /**
     * Make sure to call bind() before calling this function. Typically, a call to this function should be followed by
     * a call to glVertexAttribPointer (see class ShaderAttributes) to assign the data to a shader attribute.
     */
    fun setData(data: FloatBuffer) {
        require(id >= 0) { "VBO is invalid" }
        data.position(0)
        glBufferData(GL_ARRAY_BUFFER, data, type.glValue)
        checkGLError()
    }

    companion object {
        private val TAG = Log.Tag("ShaderVBO")
    }
}
