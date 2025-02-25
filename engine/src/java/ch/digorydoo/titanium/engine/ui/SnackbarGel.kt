package ch.digorydoo.titanium.engine.ui

import ch.digorydoo.kutils.colour.Colour
import ch.digorydoo.kutils.point.MutablePoint2f
import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.kutils.rect.MutableRecti
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.font.FontManager.FontName.SNACKBAR_FONT
import ch.digorydoo.titanium.engine.gel.GraphicElement
import ch.digorydoo.titanium.engine.shader.Renderer
import ch.digorydoo.titanium.engine.texture.Texture
import kotlin.math.ceil

class SnackbarGel private constructor(val textTex: Texture, val bgTex: Texture): GraphicElement() {
    override val renderer = makeRenderer()
    override val inDialog = Visibility.ACTIVE
    override val inMenu = Visibility.ACTIVE
    override val inEditor = Visibility.ACTIVE

    private val creationTime = App.time.sessionTime
    private var opacity = 0.0f

    override fun onAnimateActive() {
        val sx = App.screenWidthDp.toFloat()
        pos.x = sx / 2 - bgTex.width / 2
        pos.y = SNACKBAR_MARGIN_TOP.toFloat()

        var t = App.time.sessionTime - creationTime

        if (t < FADEIN_TIME) {
            opacity = 1.0f - (FADEIN_TIME - t) / FADEIN_TIME
        } else {
            t -= FADEIN_TIME

            if (t < STAY_TIME) {
                opacity = 1.0f
            } else {
                t -= STAY_TIME

                if (t < FADEOUT_TIME) {
                    opacity = (FADEOUT_TIME - t) / FADEOUT_TIME
                } else {
                    setZombie()
                }
            }
        }
    }

    private fun makeRenderer(): Renderer {
        val bgProps: UISpriteRenderer.Delegate = object: UISpriteRenderer.Delegate() {
            override val renderPos get() = this@SnackbarGel.pos
            override val tex get() = this@SnackbarGel.bgTex
            override val frameSize = MutablePoint2f(tex.width, tex.height)
            override val opacity get() = this@SnackbarGel.opacity
        }

        val bgRenderer = App.factory.createUISpriteRenderer(bgProps)

        val textPos = MutablePoint3f() // cached to avoid creating new object all the time

        val textProps: UISpriteRenderer.Delegate = object: UISpriteRenderer.Delegate() {
            override val renderPos
                get() = textPos
                    .set(this@SnackbarGel.pos)
                    .add(SNACKBAR_TEXT_MARGIN, SNACKBAR_TEXT_MARGIN, 0)

            override val tex get() = this@SnackbarGel.textTex
            override val frameSize = MutablePoint2f(tex.width, tex.height)
            override val opacity get() = this@SnackbarGel.opacity
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

    companion object {
        private const val FADEIN_TIME = 0.1f
        private const val STAY_TIME = 1.5f
        private const val FADEOUT_TIME = 0.3f

        fun create(text: String): SnackbarGel {
            val textTex = makeSnackbarTextTexture(text)

            val snackWidth = textTex.width + 2 * SNACKBAR_TEXT_MARGIN
            val snackHeight = textTex.height + 2 * SNACKBAR_TEXT_MARGIN
            val bgTex = makeSnackbarBgTexture(snackWidth, snackHeight)

            return SnackbarGel(textTex, bgTex)
        }

        private fun makeSnackbarTextTexture(snackbarText: String): Texture {
            var texWidth = SNACKBAR_DEFAULT_WIDTH - 2 * SNACKBAR_TEXT_MARGIN
            val texHeight: Int

            val ftxt = App.fonts.formatText(snackbarText, SNACKBAR_FONT, texWidth - 2 * SNACKBAR_TEXT_PADDING)
            val shrink = ftxt.lines.size <= 1

            if (shrink) {
                val sz = App.fonts.measureText(snackbarText, SNACKBAR_FONT)
                texWidth = ceil(sz.x).toInt() + 2 * SNACKBAR_TEXT_PADDING
                texHeight = ceil(sz.y).toInt() + 2 * SNACKBAR_TEXT_PADDING
            } else {
                texHeight = ftxt.maxHeight + 2 * SNACKBAR_TEXT_PADDING
            }

            return App.textures.createTexture(texWidth, texHeight).also {
                it.drawInto {
                    clear(Colour.transparent)

                    if (shrink) {
                        drawText(
                            snackbarText,
                            SNACKBAR_TEXT_PADDING,
                            SNACKBAR_TEXT_PADDING,
                            dlgTextColour,
                            SNACKBAR_FONT
                        )
                    } else {
                        drawText(ftxt, SNACKBAR_TEXT_PADDING, SNACKBAR_TEXT_PADDING, dlgTextColour)
                    }
                }
            }
        }

        private fun makeSnackbarBgTexture(width: Int, height: Int) =
            App.textures.createTexture(width, height).also {
                it.drawInto {
                    clear(Colour.transparent)
                    fillRoundRect(
                        MutableRecti(0, 0, width, height),
                        SNACKBAR_CORNER_SIZE,
                        SNACKBAR_CORNER_SIZE,
                        dlgBgColour
                    )
                }
            }
    }
}
