package ch.digorydoo.titanium.main.shader

import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.engine.shader.ShaderManager
import ch.digorydoo.titanium.engine.shader.ShaderProgram
import ch.digorydoo.titanium.engine.shader.ShaderProgram.ProgramType
import ch.digorydoo.titanium.main.opengl.checkGLError
import org.lwjgl.opengl.GL20.*

class ShaderManagerImpl: ShaderManager() {
    private val cachedPrograms = ShaderProgramCache()
    private val cachedShaders = ShaderCache()
    private val allNonSharedPrograms = mutableListOf<ShaderProgramImpl>()

    override fun getSharedProgram(type: ProgramType, flags: Set<ShaderFlags>?): ShaderProgram {
        val entry = cachedPrograms[type, flags]

        if (entry != null) {
            return entry // already in cache
        }

        val p = makeProgram(type, flags)
        cachedPrograms[type, flags] = p
        return p
    }

    override fun getNewProgram(type: ProgramType, flags: Set<ShaderFlags>?): ShaderProgram {
        val p = makeProgram(type, flags)
        allNonSharedPrograms.add(p as ShaderProgramImpl)
        return p
    }

    private fun makeProgram(type: ProgramType, flags: Set<ShaderFlags>?): ShaderProgram {
        checkGLError()
        val vsh = getOrLoadShader(type, GL_VERTEX_SHADER, flags)
        val fsh = getOrLoadShader(type, GL_FRAGMENT_SHADER, flags)
        return ShaderProgramImpl(type, flags ?: emptySet()).also { it.create(vsh, fsh) }
    }

    private fun getOrLoadShader(programType: ProgramType, shaderType: Int, flags: Set<ShaderFlags>?): Int {
        val name = when (programType) {
            ProgramType.BRICK_GLASS -> "brick-glass"
            ProgramType.BRICK_VOLUME -> "brick-volume"
            ProgramType.MESH -> "mesh"
            ProgramType.PAPER -> "paper"
            ProgramType.SHADOWS -> "shadows"
            ProgramType.SKYDOME -> "skydome"
            ProgramType.UI_SPRITES -> "ui-sprite"
            ProgramType.WATER -> "water"
        }

        val suffix = when (shaderType) {
            GL_VERTEX_SHADER -> "vsh"
            GL_FRAGMENT_SHADER -> "fsh"
            else -> throw Exception("Unknown shader type: $shaderType")
        }

        val fname = "$name.$suffix"

        return cachedShaders[fname, flags]
            ?: loadShader(fname, shaderType, flags)?.also { cachedShaders[fname, flags] = it }
            ?: throw Exception("Error: Failed to load shader: $fname")
    }

    private fun loadShader(fname: String, type: Int, flags: Set<ShaderFlags>?): Int? {
        Log.info(TAG, "Loading shader $fname")
        checkGLError()

        val contents = getPrecompiledShader(fname, flags) ?: return null
        val shader = glCreateShader(type)
        checkGLError()

        if (shader <= 0) {
            Log.error(TAG, "Couldn't create shader object: $fname")
            return null
        }

        glShaderSource(shader, contents)
        checkGLError()

        glCompileShader(shader)
        checkGLError()

        val status = intArrayOf(0)
            .let { arr ->
                glGetShaderiv(shader, GL_COMPILE_STATUS, arr)
                arr[0]
            }

        if (status != GL_TRUE) {
            Log.error(TAG, "Compiling shader failed with status $status\n" + glGetShaderInfoLog(shader).trim())
            glDeleteShader(shader)
            return null
        }

        return shader
    }

    override fun unloadAllNonSharedPrograms() {
        Log.info(TAG, "Unloading all ${allNonSharedPrograms.size} of non-shared programmes")
        allNonSharedPrograms.forEach { it.unload() }
        allNonSharedPrograms.clear()
    }

    companion object {
        private val TAG = Log.Tag("ShaderManagerImpl")
    }
}
