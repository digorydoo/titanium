package ch.digorydoo.titanium.main.shader

import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.engine.shader.ShaderManager.ShaderFlags
import ch.digorydoo.titanium.engine.shader.ShaderProgram
import ch.digorydoo.titanium.engine.shader.ShaderProgram.ProgramType
import ch.digorydoo.titanium.main.opengl.checkGLError
import org.lwjgl.opengl.GL20.*

class ShaderProgramImpl(
    override val type: ProgramType,
    override val flags: Set<ShaderFlags>,
): ShaderProgram {
    private var id = -1

    fun create(vertexShaderId: Int, fragmentShaderId: Int) {
        require(id < 0) { "Program $type already created" }

        id = glCreateProgram()
        checkGLError()

        require(id >= 0) { "Failed to create id for program $type" }

        glAttachShader(id, vertexShaderId)
        checkGLError()

        glAttachShader(id, fragmentShaderId)
        checkGLError()

        glLinkProgram(id)
        checkGLError()

        val arr = intArrayOf(0)
        glGetProgramiv(id, GL_LINK_STATUS, arr)
        val status = arr[0]

        if (status != GL_TRUE) {
            Log.error(TAG, glGetProgramInfoLog(id))
            throw Exception("Failed to link program $type")
        }

        glUseProgram(id)
        checkGLError()
    }

    fun unload() {
        Log.info(TAG, "Unloading program $type ($id)")
        require(id >= 0) { "Program $type not loaded" }

        glDeleteProgram(id)
        id = -1
        checkGLError()
    }

    @Suppress("removal")
    protected fun finalize() {
        // Check that unload has been called. We can't throw from finalize, so log only.
        if (id >= 0) Log.error(TAG, "still valid at finalize")
    }

    fun use() {
        require(id >= 0) { "Program $type not loaded" }
        glUseProgram(id)
        checkGLError()
    }

    fun findLocations(uniforms: ShaderUniforms, attributes: ShaderAttributes) {
        uniforms.findLocations(id)
        attributes.findLocations(id)
    }

    companion object {
        private val TAG = Log.Tag("ShaderProgramImpl")
    }
}
