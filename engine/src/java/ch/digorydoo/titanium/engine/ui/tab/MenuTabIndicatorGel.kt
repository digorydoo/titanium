package ch.digorydoo.titanium.engine.ui.tab

import ch.digorydoo.kutils.colour.Colour
import ch.digorydoo.kutils.point.MutablePoint2f
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.gel.GraphicElement
import ch.digorydoo.titanium.engine.texture.Texture
import ch.digorydoo.titanium.engine.ui.UISpriteRenderer

class MenuTabIndicatorGel(private val tabs: List<MenuTabDescriptor>): GraphicElement() {
    private val texture = makeTexture()
    private val frameSize = MutablePoint2f(0, HEIGHT)
    override val inDialog = Visibility.ACTIVE
    override val inMenu = Visibility.ACTIVE
    override val inEditor = Visibility.ACTIVE

    var selectedIdx = -1
        set(t) {
            field = t
            updatePos()
        }

    private var opacity = 0.0f

    override val renderer = App.factory.createUISpriteRenderer(
        object: UISpriteRenderer.Delegate() {
            override val tex = this@MenuTabIndicatorGel.texture
            override val frameSize = this@MenuTabIndicatorGel.frameSize
            override val renderPos = this@MenuTabIndicatorGel.pos
            override val opacity get() = this@MenuTabIndicatorGel.opacity
        }
    )

    private fun updatePos() {
        val tab = tabs.getOrNull(selectedIdx)

        if (tab == null) {
            opacity = 0.0f
        } else {
            opacity = 1.0f
            val bounds = tab.bounds
            pos.set(bounds.left, bounds.bottom + MARGIN_TOP, 0)
            frameSize.x = (bounds.right - bounds.left).toFloat()
        }
    }

    override fun onRemoveZombie() {
        renderer.free()
        texture.freeRequireUnshared()
    }

    companion object {
        private const val HEIGHT = 3
        private const val MARGIN_TOP = 2

        fun makeTexture(): Texture {
            return App.textures.createTexture(HEIGHT, HEIGHT).apply {
                drawInto { clear(Colour.white) }
            }
        }
    }
}
