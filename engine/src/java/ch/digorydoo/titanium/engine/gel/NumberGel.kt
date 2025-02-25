package ch.digorydoo.titanium.engine.gel

import ch.digorydoo.kutils.colour.Colour
import ch.digorydoo.kutils.point.MutablePoint2f
import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.titanium.engine.behaviours.Align
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.font.FontManager.FontName
import ch.digorydoo.titanium.engine.image.GreyscaleImageBuffer
import ch.digorydoo.titanium.engine.shader.Renderer
import ch.digorydoo.titanium.engine.texture.Texture
import ch.digorydoo.titanium.engine.ui.UISpriteRenderer

class NumberGel(alignment: Align.Alignment? = null): GraphicElement() {
    var numberValue = 0

    private val digitTextures = Array(10) { makeDigitTexture(it) }

    private val align = if (alignment == null) null else Align(
        object: Align.Delegate() {
            override val anchor = alignment.anchor
            override val xOffset = alignment.xOffset
            override val yOffset = alignment.yOffset
            override val marginLeft = alignment.marginLeft
            override val marginTop = alignment.marginTop
            override val marginRight = alignment.marginRight
            override val marginBottom = alignment.marginBottom
            override val width = MAX_NUM_DIGITS * DIGIT_WIDTH
            override val height = DIGIT_HEIGHT

            override fun setPos(x: Int, y: Int) {
                this@NumberGel.pos.set(x, y, 0)
            }
        }
    )

    override val renderer = makeRenderer()

    private fun makeRenderer(): Renderer {
        val digitRendererProps = object: UISpriteRenderer.Delegate() {
            override val renderPos = MutablePoint3f()
            override val frameSize = MutablePoint2f(DIGIT_WIDTH, DIGIT_HEIGHT)
            override var tex: Texture? = null
        }

        val digitRenderer = App.factory.createUISpriteRenderer(digitRendererProps)

        return object: Renderer {
            override fun free() {
                digitRenderer.free()
            }

            override fun renderShadows() {}
            override fun renderSolid() {}

            override fun renderTransparent() {
                var firstDigitAt = 0
                var f = numberValue

                for (i in 0 ..< MAX_NUM_DIGITS) {
                    if (f <= 0) break
                    firstDigitAt = i
                    f /= 10
                }

                f = numberValue

                for (i in 0 .. firstDigitAt) {
                    val gelPos = this@NumberGel.pos
                    val x = gelPos.x + (firstDigitAt - i) * (DIGIT_WIDTH + DIGIT_SPACING)
                    digitRendererProps.renderPos.set(x, gelPos.y, gelPos.z)
                    digitRendererProps.tex = digitTextures[f % 10]
                    digitRenderer.renderTransparent()
                    f /= 10
                }
            }
        }
    }

    override fun onAnimateActive() {
        align?.animate()
    }

    override val inDialog = Visibility.ACTIVE
    override val inMenu = Visibility.ACTIVE
    override val inEditor = Visibility.ACTIVE

    override fun onRemoveZombie() {
        renderer.free()
        digitTextures.forEach { it.freeRequireUnshared() }
    }

    companion object {
        private const val MAX_NUM_DIGITS = 10
        private const val DIGIT_WIDTH = 16
        private const val DIGIT_HEIGHT = 32
        private const val DIGIT_PADDING = GreyscaleImageBuffer.OUTLINE_RANGE
        private const val DIGIT_SPACING = -6 // digit width is larger than the expected digit size
        private val otlColour = Colour.grey900

        fun makeDigitTexture(digit: Int) =
            App.textures.createTexture(DIGIT_WIDTH, DIGIT_HEIGHT).apply {
                drawInto {
                    clear(Colour.transparent)
                    drawText("$digit", DIGIT_PADDING, DIGIT_PADDING, Colour.white, FontName.SMALL_UI_FONT, otlColour)
                }
            }
    }
}
