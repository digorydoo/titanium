package io.github.digorydoo.titanium.engine.ui.icon

import ch.digorydoo.kutils.colour.Colour
import ch.digorydoo.kutils.vector.MutableVector2f
import ch.digorydoo.kutils.vector.MutableVector3f
import ch.digorydoo.kutils.vector.Vector2f
import io.github.digorydoo.titanium.engine.core.App
import io.github.digorydoo.titanium.engine.font.FontManager.FontName
import io.github.digorydoo.titanium.engine.i18n.ITextId
import io.github.digorydoo.titanium.engine.shader.Renderer
import io.github.digorydoo.titanium.engine.sprite.UICircularProgressRenderer
import io.github.digorydoo.titanium.engine.sprite.UISpriteRenderer
import io.github.digorydoo.titanium.engine.ui.icon.InputIconGel.Companion.ICON_SCALE_FACTOR

class InputIconRenderer(
    private val gel: InputIconGel,
    private val iconFrames: InputIconFrames,
    label: ITextId?,
): Renderer {
    private val iconRenderer = run {
        val p = MutableVector3f()
        return@run App.factory.createUISpriteRenderer(
            object: UISpriteRenderer.Delegate() {
                override val renderPos get() = p.set(gel.pos).add(0.0f, gel.scrollOffset, 0f)
                override val frameSize = iconFrames.frameSize // shared mutable object
                override val tex get() = iconFrames.tex
                override val texOffset = iconFrames.texOffset // shared mutable object
                override val brightness get() = gel.brightness
                override val scaleFactor = Vector2f(ICON_SCALE_FACTOR, ICON_SCALE_FACTOR)
                override val opacity get() = gel.opacity
            }
        )
    }

    private val labelTex = if (label == null) null else run {
        val slabel = App.i18n.getString(label)
        val font = FontName.MEDIUM_HUD_FONT
        val sz = App.fonts.measureText(slabel, font)
        val padding = 8 // to make room for the text's outline
        val texWidth = sz.x.toInt() + 2 * padding
        val texHeight = sz.y.toInt() + 2 * padding
        val tex = App.textures.createTexture(texWidth, texHeight)
        tex.drawInto {
            clear(Colour.transparent)
            drawTextCentred(slabel, tex.width / 2, padding, Colour.white, font, Colour.black)
        }
        return@run tex
    }

    private val labelFrameSize = Vector2f(labelTex?.width ?: 0, labelTex?.height ?: 0)
    val labelWidth get() = labelFrameSize.x + LABEL_X_OFFSET

    private val labelRenderer = labelTex?.let { tex ->
        val p = MutableVector3f()
        return@let App.factory.createUISpriteRenderer(
            object: UISpriteRenderer.Delegate() {
                override val renderPos
                    get() = p.set(gel.pos)
                        .add(
                            iconFrames.frameSize.x * ICON_SCALE_FACTOR + LABEL_X_OFFSET,
                            gel.scrollOffset + LABEL_Y_OFFSET,
                            0f
                        )
                override val frameSize = labelFrameSize
                override val tex = tex
                override val opacity get() = gel.opacity
            }
        )
    }

    private val progressRenderer = if (gel.secondsToHoldForSelect <= 0f) null else run {
        val p = MutableVector3f()
        val sz = MutableVector2f()
        return@run App.factory.createUICircularProgressRenderer(
            object: UICircularProgressRenderer.Delegate() {
                override val renderPos
                    get() = p.set(
                        gel.pos.x - PROGRESS_PEN_SIZE,
                        gel.pos.y - PROGRESS_PEN_SIZE + gel.scrollOffset,
                        0f
                    )
                override val frameSize
                    get() = sz.set(
                        iconFrames.frameSize.x * ICON_SCALE_FACTOR + 2 * PROGRESS_PEN_SIZE,
                        iconFrames.frameSize.y * ICON_SCALE_FACTOR + 2 * PROGRESS_PEN_SIZE,
                    )
                override val progress get() = gel.circularProgress
                override val penSize = PROGRESS_PEN_SIZE
            }
        )
    }

    override fun renderShadows() {}
    override fun renderSolid() {}

    override fun renderTransparent() {
        iconRenderer.renderTransparent()
        labelRenderer?.renderTransparent()
        progressRenderer?.renderTransparent()
    }

    override fun free() {
        iconRenderer.free()
        labelRenderer?.free()
        progressRenderer?.free()
        labelTex?.freeRequireUnshared()
    }

    companion object {
        private const val LABEL_X_OFFSET = 8f // dp
        private const val LABEL_Y_OFFSET = -3f
        private const val PROGRESS_PEN_SIZE = 8.0f // dp
    }
}
