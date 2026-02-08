package io.github.digorydoo.titanium.engine.ui.button

import ch.digorydoo.kutils.vector.MutableVector2f
import ch.digorydoo.kutils.vector.MutableVector3f
import ch.digorydoo.kutils.vector.Vector3f
import io.github.digorydoo.titanium.engine.core.App
import io.github.digorydoo.titanium.engine.shader.Renderer
import io.github.digorydoo.titanium.engine.sprite.UISpriteRenderer
import io.github.digorydoo.titanium.engine.texture.Texture
import io.github.digorydoo.titanium.engine.ui.ITEM_TEXT_OUTER_PADDING
import io.github.digorydoo.titanium.engine.ui.dlg_item.DlgItemGel
import kotlin.math.max

object ButtonRendererFactory {
    interface Delegate {
        val pos: Vector3f
        val opacity: Float
        val brightness: Float
        val hilited: Boolean
        val selected: Boolean
        val selectTime: Float
    }

    fun makeBgRenderer(delegate: Delegate, bgTex: Texture) =
        App.factory.createUISpriteRenderer(
            object: UISpriteRenderer.Delegate() {
                override val renderPos = delegate.pos // shared mutable object
                override val tex = bgTex
                override val frameSize = MutableVector2f(tex.width, tex.height)
                override val opacity get() = delegate.opacity

                override val brightness: Float
                    get() = when {
                        delegate.selected -> (0.8f + 0.8f * max(
                            0.0f,
                            1.0f - (App.time.sessionTime - delegate.selectTime) / DlgItemGel.SELECT_DELAY_SECONDS
                        ))
                        delegate.hilited -> 1.15f + 0.05f * delegate.brightness
                        else -> 1.0f
                    }
            }
        )

    fun makeOtlRenderer(delegate: Delegate, otlTex: Texture) =
        App.factory.createUISpriteRenderer(
            object: UISpriteRenderer.Delegate() {
                override val renderPos = delegate.pos // shared mutable object
                override val tex = otlTex
                override val frameSize = MutableVector2f(tex.width, tex.height)
                override val opacity get() = delegate.opacity

                override val brightness
                    get() = when {
                        !delegate.hilited -> 0.1f
                        !delegate.selected -> 0.7f + 0.5f * delegate.brightness
                        else -> 1.0f
                    }
            }
        )

    fun makeTextRenderer(delegate: Delegate, textTex: Texture, xoffset: Int = ITEM_TEXT_OUTER_PADDING): Renderer {
        val textPos = MutableVector3f() // bound variable

        return App.factory.createUISpriteRenderer(
            object: UISpriteRenderer.Delegate() {
                override val renderPos
                    get() = delegate.pos.let {
                        textPos.set(
                            it.x + xoffset,
                            it.y + ITEM_TEXT_OUTER_PADDING,
                            it.z
                        )
                    }
                override val tex = textTex
                override val frameSize = MutableVector2f(tex.width, tex.height)
                override val opacity get() = delegate.opacity
                override val brightness get() = if (delegate.hilited) 1.0f else 0.8f
            }
        )
    }
}
