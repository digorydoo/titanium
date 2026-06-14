package io.github.digorydoo.titanium.engine.gel

import ch.digorydoo.kutils.colour.Colour
import ch.digorydoo.kutils.vector.MutableVector2f
import ch.digorydoo.kutils.vector.MutableVector3f
import io.github.digorydoo.titanium.engine.behaviours.Align
import io.github.digorydoo.titanium.engine.core.App
import io.github.digorydoo.titanium.engine.font.FontManager.FontName
import io.github.digorydoo.titanium.engine.shader.Renderer
import io.github.digorydoo.titanium.engine.sprite.UISpriteRenderer
import io.github.digorydoo.titanium.engine.texture.GreyscaleImageBuffer
import io.github.digorydoo.titanium.engine.texture.Texture

class NumberGel(alignment: Align.Alignment? = null): GraphicElement() {
    init {
        inDialog = Visibility.ACTIVE
        inMenu = Visibility.ACTIVE
        inEditor = Visibility.ACTIVE
        visibleOnScreenshots = false
    }

    var numberValue = 0

    private val digitTextures = Array(10) { makeDigitTexture(it) }

    private val align = if (alignment == null) null else Align(
        this,
        alignment,
        object: Align.Delegate {
            override val width = MAX_NUM_DIGITS * DIGIT_WIDTH
            override val height = DIGIT_HEIGHT
        }
    )

    override val renderer = makeRenderer()

    private fun makeRenderer(): Renderer {
        val digitRendererProps = object: UISpriteRenderer.Delegate() {
            override val renderPos = MutableVector3f()
            override val frameSize = MutableVector2f(DIGIT_WIDTH, DIGIT_HEIGHT)
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
                    drawText("$digit", DIGIT_PADDING, DIGIT_PADDING, Colour.white, FontName.SMALL_HUD_FONT, otlColour)
                }
            }
    }
}
