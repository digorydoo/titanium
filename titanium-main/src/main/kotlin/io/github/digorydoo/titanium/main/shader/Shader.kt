package io.github.digorydoo.titanium.main.shader

import ch.digorydoo.kutils.logging.Log
import io.github.digorydoo.titanium.engine.core.App
import io.github.digorydoo.titanium.engine.shader.ShaderManager.ShaderFlags
import io.github.digorydoo.titanium.engine.shader.ShaderProgram.ProgramType
import io.github.digorydoo.titanium.main.core.LeakDetector
import io.github.digorydoo.titanium.main.shader.ShaderAttributes.Attribute

class Shader(type: ProgramType, flags: Set<ShaderFlags>? = null) {
    val uniforms = ShaderUniforms()
    private val attributes = ShaderAttributes()
    val program = App.shaders.getProgram(type, flags) as ShaderProgramImpl

    // The ShaderVBO is not part of Shader, because sometimes we want the same model data to use with different shaders.
    // Is there a similar use case for the VAO? I think not, so I keep it here.
    private val vao = ShaderVAO()

    private val leakDetector = LeakDetector(TAG.name, initiallyValid = false)

    fun create() {
        program.use()
        vao.create()
        program.findLocations(uniforms, attributes)
        leakDetector.resourceValid = true
    }

    fun free() {
        if (leakDetector.resourceValid) {
            vao.free()
            leakDetector.resourceValid = false
        }
    }

    fun bindVAO() {
        vao.bind()
    }

    fun connectToVBO(attr: Attribute) {
        vao.bind()
        attributes.connectVAOToVBO(attr)
    }

    companion object {
        private val TAG = Log.Tag("Shader")
    }
}
