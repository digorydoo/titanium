package ch.digorydoo.titanium.main.shader

import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.shader.ShaderManager.ShaderFlags
import ch.digorydoo.titanium.engine.shader.ShaderProgram.ProgramType
import ch.digorydoo.titanium.main.shader.ShaderAttributes.Attribute

class Shader(type: ProgramType, flags: Set<ShaderFlags>? = null) {
    val uniforms = ShaderUniforms()
    private val attributes = ShaderAttributes()
    val program = App.shaders.getNewProgram(type, flags) as ShaderProgramImpl

    // The ShaderVBO is not part of Shader, because sometimes we want the same model data to use with different shaders.
    // Is there a similar use case for the VAO? I think not, so I keep it here.
    private val vao = ShaderVAO()
    private var valid = false

    fun create() {
        vao.create()
        program.findLocations(uniforms, attributes)
        valid = true
    }

    fun free() {
        if (valid) {
            valid = false
            vao.free()
            // program.free() -- doesn't exist, and program should be shared anyway FIXME use getSharedProgram
        }
    }

    @Suppress("removal")
    protected fun finalize() {
        // Check that unload has been called. We can't throw from finalize, so log only.
        if (valid) Log.error(TAG, "Wrapper of ${program.type} still valid at finalize")
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
