package ch.digorydoo.titanium.main.shader

import ch.digorydoo.titanium.engine.shader.ShaderManager.ShaderFlags
import ch.digorydoo.titanium.engine.shader.ShaderProgram
import ch.digorydoo.titanium.engine.shader.ShaderProgram.ProgramType

class ShaderProgramCache {
    private val cache = mutableMapOf<String, ShaderProgram>()

    operator fun get(type: ProgramType, flags: Set<ShaderFlags>?): ShaderProgram? {
        val key = getKey(type, flags)
        return cache[key]
    }

    operator fun set(type: ProgramType, flags: Set<ShaderFlags>?, program: ShaderProgram) {
        val key = getKey(type, flags)
        cache[key] = program
    }

    private fun getKey(type: ProgramType, flags: Set<ShaderFlags>?): String {
        val f = flags?.sorted()?.joinToString(",") ?: ""
        return "${type}[$f]"
    }
}
