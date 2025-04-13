package ch.digorydoo.titanium.engine.gel

import ch.digorydoo.kutils.colour.Colour
import ch.digorydoo.kutils.point.MutablePoint2f
import ch.digorydoo.titanium.engine.behaviours.Align
import ch.digorydoo.titanium.engine.behaviours.Rotate
import ch.digorydoo.titanium.engine.behaviours.Shake
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.font.FontManager.FontName
import ch.digorydoo.titanium.engine.texture.GreyscaleImageBuffer
import ch.digorydoo.titanium.engine.texture.Texture
import ch.digorydoo.titanium.engine.ui.UISpriteRenderer

class TextGel(text: String, alignment: Align.Alignment? = null): GraphicElement() {
    init {
        inDialog = Visibility.ACTIVE
        inMenu = Visibility.ACTIVE
        inEditor = Visibility.ACTIVE
    }

    var text = text
        set(newText) {
            if (field != newText) {
                field = newText
                texture.freeRequireUnshared()
                texture = makeTexture(text)
                frameSize.set(texture.width, texture.height)
            }
        }

    private var texture = makeTexture(text)

    private val frameSize = MutablePoint2f(texture.width, texture.height)

    private val renderProps = object: UISpriteRenderer.Delegate() {
        override val renderPos get() = this@TextGel.pos
        override val frameSize get() = this@TextGel.frameSize
        override val tex get() = this@TextGel.texture
        override val rotation get() = rotateProps.rotation
    }

    override val renderer = App.factory.createUISpriteRenderer(renderProps)

    private val rotateProps = object: Rotate.Delegate {
        override var rotation = 0.0f
    }

    private val rotate = Rotate(rotateProps, duration = 0.8f, isRotating = false)

    private val shake = Shake(
        object: Shake.Delegate {
            override var shakeValue
                get() = this@TextGel.pos.x
                set(x) {
                    this@TextGel.moveTo(x, this@TextGel.pos.y, 0.0f)
                }
        }
    )

    private val align = if (alignment == null) null else Align(
        object: Align.Delegate() {
            override val anchor = alignment.anchor
            override val xOffset = alignment.xOffset
            override val yOffset = alignment.yOffset
            override val marginLeft = alignment.marginLeft
            override val marginTop = alignment.marginTop
            override val marginRight = alignment.marginRight
            override val marginBottom = alignment.marginBottom
            override val width = texture.width
            override val height = texture.height

            override fun setPos(x: Int, y: Int) {
                this@TextGel.moveTo(x, y, 0)
            }
        }
    )

    override fun onAnimateActive() {
        align?.animate()
        rotate.animate()
        shake.animate()
    }

    fun rotate() = rotate.rotate()
    fun shake() = shake.shake()

    override fun onRemoveZombie() {
        renderer.free()
        texture.freeRequireUnshared()
    }

    override fun toString() = "TextGel($text)"

    companion object {
        private const val CAPTION_PADDING = GreyscaleImageBuffer.OUTLINE_RANGE
        private val otlColour = Colour.grey900

        fun makeTexture(text: String): Texture {
            val size = App.fonts.measureText(text, FontName.SMALL_UI_FONT)
            size.x += 2 * CAPTION_PADDING
            size.y += 2 * CAPTION_PADDING
            return App.textures.createTexture(size.x.toInt(), size.y.toInt()).apply {
                drawInto {
                    clear(Colour.transparent)
                    drawText(text, CAPTION_PADDING, CAPTION_PADDING, Colour.white, FontName.SMALL_UI_FONT, otlColour)
                }
            }
        }
    }
}
