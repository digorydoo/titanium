package io.github.digorydoo.titanium.engine.ui.dialogue

import ch.digorydoo.kutils.vector.MutableVector2f
import ch.digorydoo.kutils.vector.MutableVector3f
import io.github.digorydoo.titanium.engine.core.App
import io.github.digorydoo.titanium.engine.gel.GraphicElement
import io.github.digorydoo.titanium.engine.shader.Renderer
import io.github.digorydoo.titanium.engine.sprite.UISpriteRenderer
import io.github.digorydoo.titanium.engine.texture.Texture
import io.github.digorydoo.titanium.engine.ui.DLG_TEXT_MARGIN_X
import io.github.digorydoo.titanium.engine.ui.DLG_TEXT_MARGIN_Y

class DlgTextGel(private val textTex: Texture, private val bgTex: Texture): GraphicElement() {
    init {
        inDialog = Visibility.ACTIVE
        inMenu = Visibility.ACTIVE
        inEditor = Visibility.ACTIVE
    }

    override val renderer = makeRenderer()

    private fun makeRenderer(): Renderer {
        val bgProps = object: UISpriteRenderer.Delegate() {
            override val renderPos get() = this@DlgTextGel.pos
            override val tex = bgTex
            override val frameSize = MutableVector2f(bgTex.width, bgTex.height)
        }

        val bgRenderer = App.factory.createUISpriteRenderer(bgProps)
        val textPos = MutableVector3f()

        val textProps = object: UISpriteRenderer.Delegate() {
            override val renderPos
                get() = textPos
                    .set(this@DlgTextGel.pos)
                    .add(DLG_TEXT_MARGIN_X, DLG_TEXT_MARGIN_Y, 0)

            override val tex = textTex
            override val frameSize = MutableVector2f(textTex.width, textTex.height)
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
