package ch.digorydoo.titanium.main.shader

import ch.digorydoo.titanium.main.opengl.checkGLError
import org.lwjgl.opengl.GL20.*

class ShaderAttributes {
    enum class Attribute {
        ModelPos,   // vec3
        Normal,     // vec3
        TexCoord,   // vec2
    }

    private val locations = mutableMapOf<Attribute, Int>()

    fun findLocations(programId: Int) {
        Attribute.entries.forEach { a ->
            locations[a] = glGetAttribLocation(programId, a.name)
                .takeIf { glGetError() == GL_NO_ERROR }
                ?: -1
        }
    }

    // Make sure to bind both a VAO and a VBO before calling this function.
    fun connectVAOToVBO(attr: Attribute) {
        val numComponents = when (attr) {
            Attribute.ModelPos -> 3
            Attribute.Normal -> 3
            Attribute.TexCoord -> 2
        }
        connect(attr, numComponents)
    }

    private fun connect(attr: Attribute, numComponents: Int) {
        val loc = locations[attr] ?: -1
        require(loc >= 0) { "Attribute location unknown: $attr" }

        val stride = numComponents * Float.SIZE_BYTES

        glVertexAttribPointer(loc, numComponents, GL_FLOAT, false, stride, 0L)
        checkGLError()

        glEnableVertexAttribArray(loc)
        checkGLError()
    }

    override fun toString() =
        locations.map { (key, loc) -> "   $key: $loc" }
            .joinToString("\n")
            .let { "ShaderAttributes(\n$it\n)" }
}
