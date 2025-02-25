package ch.digorydoo.titanium.engine.shader

import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.shader.ShaderProgram.ProgramType
import java.io.File

abstract class ShaderManager {
    @Suppress("unused")
    enum class ShaderFlags {
        ALWAYS_FAR_Z,
        CONCRETE,
        CONTOUR,
        CONTRAST,
        DEBUG,
        GENTLE_FLOW_NORTH,
        LAMP0,
        LAMP1,
        LAMP2,
        LAMP3,
        LAMP4,
        METAL,
        NO_FLOW,
        PSEUDO_ENV_MAP,
        SHADOWS,
        SHININESS,
        SPECULAR_LIGHT,
        WINDOW_INTERIOR,
    }

    private val precompiler = ShaderPrecompiler()

    // FIXME can I make all programs shared now that VBO and VAO are external????

    abstract fun getSharedProgram(type: ProgramType, flags: Set<ShaderFlags>? = null): ShaderProgram
    abstract fun getNewProgram(type: ProgramType, flags: Set<ShaderFlags>? = null): ShaderProgram
    abstract fun unloadAllNonSharedPrograms()

    protected fun getPrecompiledShader(shaderFileName: String, flags: Set<ShaderFlags>?): String? {
        val path = App.assets.pathToShader(shaderFileName)
        val file = File(path)

        return file.bufferedReader()
            .readText()
            .let { precompiler.precompile(it, flags) }
            .takeIf { it.isNotEmpty() }
    }
}
