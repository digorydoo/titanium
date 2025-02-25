package ch.digorydoo.titanium.engine.shader

interface Renderer {
    enum class BlendMode {
        NONE, // transparent pixels may still be 'discard'ed by shader
        ADD,
    }

    fun renderShadows()
    fun renderSolid()
    fun renderTransparent()

    fun free()
}
