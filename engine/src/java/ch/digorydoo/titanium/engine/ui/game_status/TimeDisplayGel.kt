package ch.digorydoo.titanium.engine.ui.game_status

import ch.digorydoo.kutils.point.MutablePoint2f
import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.titanium.engine.behaviours.Align
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.gel.GraphicElement
import ch.digorydoo.titanium.engine.shader.Renderer
import ch.digorydoo.titanium.engine.texture.Texture
import ch.digorydoo.titanium.engine.ui.UISpriteRenderer

class TimeDisplayGel: GraphicElement() {
    init {
        inDialog = Visibility.INVISIBLE
        inMenu = Visibility.INVISIBLE
        inEditor = Visibility.ACTIVE
        visibleOnScreenshots = false
    }

    private val digitOrColonTextures = Array(11) { idx ->
        when (idx) {
            10 -> makeTexture(":")
            else -> makeTexture("$idx")
        }
    }

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
                var xOffset = 0

                renderNumber(App.time.storyClockHoursHand, xOffset)
                xOffset += DIGIT_PAIR_WIDTH

                renderColon(xOffset)
                xOffset += COLON_WIDTH

                renderNumber(App.time.storyClockMinutesHand, xOffset)
                xOffset += DIGIT_PAIR_WIDTH

                renderColon(xOffset)
                xOffset += COLON_WIDTH

                renderNumber(App.time.storyClockSecondsHand, xOffset)
            }

            private fun renderNumber(numberValue: Int, xOffset: Int) {
                var d = numberValue

                for (i in 0 ..< DIGITS_PER_NUMBER) {
                    val gelPos = this@TimeDisplayGel.pos
                    val x = gelPos.x + xOffset + (DIGITS_PER_NUMBER - 1 - i) * (DIGIT_WIDTH + DIGIT_SPACING)
                    digitRendererProps.renderPos.set(x, gelPos.y, gelPos.z)
                    digitRendererProps.tex = digitOrColonTextures[d % 10]
                    digitRenderer.renderTransparent()
                    d /= 10
                }
            }

            private fun renderColon(xOffset: Int) {
                val gelPos = this@TimeDisplayGel.pos
                val x = gelPos.x + xOffset
                digitRendererProps.renderPos.set(x, gelPos.y, gelPos.z)
                digitRendererProps.tex = digitOrColonTextures[10]
                digitRenderer.renderTransparent()
            }
        }
    }

    private val align = Align(
        object: Align.Delegate() {
            override val anchor = Align.Anchor.TOP_RIGHT
            override val marginTop = TIME_DISPLAY_MARGIN_TOP
            override val marginRight = TIME_DISPLAY_MARGIN_RIGHT
            override val width = TIME_DISPLAY_WIDTH

            override fun setPos(x: Int, y: Int) {
                this@TimeDisplayGel.pos.set(x, y, 0)
            }
        }
    )

    override fun onAnimateActive() {
        align.animate()
    }

    override fun onRemoveZombie() {
        renderer.free()
        digitOrColonTextures.forEach { it.freeRequireUnshared() }
    }

    companion object {
        private const val TIME_DISPLAY_WIDTH = 80 // dp
        private const val TIME_DISPLAY_MARGIN_RIGHT = 42 // dp
        private const val TIME_DISPLAY_MARGIN_TOP = 24 // dp
        private const val DIGITS_PER_NUMBER = 2
        private const val DIGIT_PAIR_WIDTH = 21
        private const val COLON_WIDTH = 7
        private const val DIGIT_WIDTH = 16
        private const val DIGIT_HEIGHT = 32
        private const val DIGIT_SPACING = -6 // digit width is larger than the expected digit size

        private fun makeTexture(digitOrColon: String) =
            App.textures.createTexture(digitOrColon, width = DIGIT_WIDTH, height = DIGIT_HEIGHT)
    }
}
