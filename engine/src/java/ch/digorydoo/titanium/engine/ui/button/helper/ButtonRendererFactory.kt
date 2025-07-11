package ch.digorydoo.titanium.engine.ui.button.helper

import ch.digorydoo.kutils.point.MutablePoint2f
import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.shader.Renderer
import ch.digorydoo.titanium.engine.sprite.UISpriteRenderer
import ch.digorydoo.titanium.engine.texture.Texture
import ch.digorydoo.titanium.engine.ui.ITEM_TEXT_OUTER_PADDING
import ch.digorydoo.titanium.engine.ui.button.IButtonGel
import kotlin.math.max

object ButtonRendererFactory {
    interface Delegate {
        val pos: Point3f
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
                override val frameSize = MutablePoint2f(tex.width, tex.height)
                override val opacity get() = delegate.opacity

                override val brightness: Float
                    get() = when {
                        delegate.selected -> (0.8f + 0.8f * max(
                            0.0f,
                            1.0f - (App.time.sessionTime - delegate.selectTime) / IButtonGel.SELECT_DELAY_SECONDS
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
                override val frameSize = MutablePoint2f(tex.width, tex.height)
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
        val textPos = MutablePoint3f() // bound variable

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
                override val frameSize = MutablePoint2f(tex.width, tex.height)
                override val opacity get() = delegate.opacity
                override val brightness get() = if (delegate.hilited) 1.0f else 0.8f
            }
        )
    }
}
