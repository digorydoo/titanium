package ch.digorydoo.titanium.engine.gel

import ch.digorydoo.titanium.engine.shader.Renderer

/**
 * A gel that does nothing. This instance can be used for empty slots in Arrays, etc. to avoid nullability.
 */
internal class DummyGel private constructor(): GraphicElement() {
    override val renderer = object: Renderer {
        override fun renderShadows() {}
        override fun renderSolid() {}
        override fun renderTransparent() {}
        override fun free() {}
    }

    override fun onRemoveZombie() {}

    init {
        setZombie()
    }

    override fun toString() = "DummyGel"

    companion object {
        val instance = DummyGel()
    }
}
