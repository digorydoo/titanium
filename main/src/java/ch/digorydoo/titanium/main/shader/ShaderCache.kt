package ch.digorydoo.titanium.main.shader

import ch.digorydoo.titanium.engine.shader.ShaderManager.ShaderFlags

class ShaderCache {
    private val cache = mutableMapOf<String, Int>()

    operator fun get(fname: String, flags: Set<ShaderFlags>?): Int? {
        val key = getKey(fname, flags)
        return cache[key]
    }

    operator fun set(fname: String, flags: Set<ShaderFlags>?, shader: Int) {
        val key = getKey(fname, flags)
        cache[key] = shader
    }

    private fun getKey(fname: String, flags: Set<ShaderFlags>?): String {
        val f = flags?.sorted()?.joinToString(",") ?: ""
        return "${fname}[$f]"
    }
}
