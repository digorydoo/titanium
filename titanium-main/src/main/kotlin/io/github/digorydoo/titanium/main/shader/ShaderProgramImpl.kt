package io.github.digorydoo.titanium.main.shader

import ch.digorydoo.kutils.logging.Log
import io.github.digorydoo.titanium.engine.shader.ShaderManager.ShaderFlags
import io.github.digorydoo.titanium.engine.shader.ShaderProgram
import io.github.digorydoo.titanium.engine.shader.ShaderProgram.ProgramType
import io.github.digorydoo.titanium.main.core.LeakDetector
import io.github.digorydoo.titanium.main.opengl.checkGLError
import org.lwjgl.opengl.GL20.*

class ShaderProgramImpl(
    override val type: ProgramType,
    override val flags: Set<ShaderFlags>,
): ShaderProgram {
    private var id = -1
    private val leakDetector = LeakDetector(TAG.name, initiallyValid = false)

    fun create(vertexShaderId: Int, fragmentShaderId: Int) {
        require(id < 0) { "Program $type already created" }

        id = glCreateProgram()
        checkGLError()

        require(id >= 0) { "Failed to create id for program $type" }
        leakDetector.resourceValid = true

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

    // FIXME call this on shutdown from ShaderManager
    // fun unload() {
    //     Log.info(TAG, "Unloading program $type ($id)")
    //     require(id >= 0) { "Program $type not loaded" }
    //
    //     glDeleteProgram(id)
    //     id = -1
    //     checkGLError()
    //     leakDetector.resourceValid = false
    // }

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
