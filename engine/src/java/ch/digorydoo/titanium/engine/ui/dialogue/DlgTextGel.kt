package ch.digorydoo.titanium.engine.ui.dialogue

import ch.digorydoo.kutils.point.MutablePoint2f
import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.gel.GraphicElement
import ch.digorydoo.titanium.engine.shader.Renderer
import ch.digorydoo.titanium.engine.texture.Texture
import ch.digorydoo.titanium.engine.ui.DLG_TEXT_MARGIN_X
import ch.digorydoo.titanium.engine.ui.DLG_TEXT_MARGIN_Y
import ch.digorydoo.titanium.engine.ui.UISpriteRenderer

class DlgTextGel(private val textTex: Texture, private val bgTex: Texture): GraphicElement() {
    override val inDialog = Visibility.ACTIVE
    override val inMenu = Visibility.ACTIVE
    override val inEditor = Visibility.ACTIVE
    override val renderer = makeRenderer()

    private fun makeRenderer(): Renderer {
        val bgProps = object: UISpriteRenderer.Delegate() {
            override val renderPos get() = this@DlgTextGel.pos
            override val tex = bgTex
            override val frameSize = MutablePoint2f(bgTex.width, bgTex.height)
        }

        val bgRenderer = App.factory.createUISpriteRenderer(bgProps)
        val textPos = MutablePoint3f()

        val textProps = object: UISpriteRenderer.Delegate() {
            override val renderPos
                get() = textPos
                    .set(this@DlgTextGel.pos)
                    .add(DLG_TEXT_MARGIN_X, DLG_TEXT_MARGIN_Y, 0)

            override val tex = textTex
            override val frameSize = MutablePoint2f(textTex.width, textTex.height)
        }

        val textRenderer = App.factory.createUISpriteRenderer(textProps)

        return object: Renderer {
            override fun free() {
                bgRenderer.free()
                textRenderer.free()
            }

            override fun renderShadows() {}
            override fun renderSolid() {}

            override fun renderTransparent() {
                bgRenderer.renderTransparent()
                textRenderer.renderTransparent()
            }
        }
    }

    override fun onRemoveZombie() {
        renderer.free()
        textTex.freeRequireUnshared()
        bgTex.freeRequireUnshared()
    }
}
