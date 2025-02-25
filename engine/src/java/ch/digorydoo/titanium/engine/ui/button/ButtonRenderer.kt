package ch.digorydoo.titanium.engine.ui.button

import ch.digorydoo.kutils.point.MutablePoint2f
import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.shader.Renderer
import ch.digorydoo.titanium.engine.ui.ITEM_INCDEC_MARGIN_TOP
import ch.digorydoo.titanium.engine.ui.ITEM_TEXT_OUTER_PADDING
import ch.digorydoo.titanium.engine.ui.ITEM_VALUE_MAX_WIDTH
import ch.digorydoo.titanium.engine.ui.UISpriteRenderer
import ch.digorydoo.titanium.engine.ui.button.ButtonGel.Companion.SELECT_DELAY_SECONDS
import kotlin.math.floor
import kotlin.math.max

class ButtonRenderer private constructor(
    private val bg: Renderer,
    private val otl: Renderer,
    private val text: Renderer,
    private val image: Renderer?,
    private val value: Renderer?,
    private val inc: Renderer?,
    private val dec: Renderer?,
): Renderer {
    override fun free() {
        bg.free()
        otl.free()
        text.free()
        image?.free()
        value?.free()
        inc?.free()
        dec?.free()
    }

    override fun renderShadows() {}
    override fun renderSolid() {}

    override fun renderTransparent() {
        bg.renderTransparent()
        otl.renderTransparent()
        text.renderTransparent()
        image?.renderTransparent()
        value?.renderTransparent()
        inc?.renderTransparent()
        dec?.renderTransparent()
    }

    companion object {
        fun create(gel: ButtonGel): ButtonRenderer {
            val bg = makeBgRenderer(gel)
            val otl = makeOtlRenderer(gel)
            val text = makeTextRenderer(gel)
            val image = makeImageRenderer(gel)
            val value = makeValueRenderer(gel)
            val inc = makeIncDecRenderer(gel, true)
            val dec = makeIncDecRenderer(gel, false)
            return ButtonRenderer(bg, otl, text, image, value, inc, dec)
        }

        private fun makeBgRenderer(gel: ButtonGel) =
            App.factory.createUISpriteRenderer(
                object: UISpriteRenderer.Delegate() {
                    override val renderPos get() = gel.pos
                    override val tex = gel.bgTex
                    override val frameSize = MutablePoint2f(tex.width, tex.height)
                    override val opacity get() = gel.opacity

                    override val brightness: Float
                        get() = when {
                            gel.selected -> (0.8f + 0.8f * max(
                                0.0f,
                                1.0f - (App.time.sessionTime - gel.selectTime) / SELECT_DELAY_SECONDS
                            ))
                            gel.hilited -> 1.15f + 0.05f * gel.brightness
                            else -> 1.0f
                        }
                }
            )

        private fun makeOtlRenderer(gel: ButtonGel) =
            App.factory.createUISpriteRenderer(
                object: UISpriteRenderer.Delegate() {
                    override val renderPos get() = gel.pos
                    override val tex = gel.otlTex
                    override val frameSize = MutablePoint2f(tex.width, tex.height)
                    override val opacity get() = gel.opacity

                    override val brightness
                        get() = when {
                            !gel.hilited -> 0.0f
                            !gel.selected -> 1.4f + 0.5f * gel.brightness
                            else -> 0.8f
                        }
                }
            )

        private fun makeTextRenderer(gel: ButtonGel): Renderer {
            val textPos = MutablePoint3f() // bound variable
            var xoffset = ITEM_TEXT_OUTER_PADDING

            gel.imageTex?.let {
                xoffset += it.width + ITEM_TEXT_OUTER_PADDING
            }

            return App.factory.createUISpriteRenderer(
                object: UISpriteRenderer.Delegate() {
                    override val renderPos
                        get() = textPos.set(
                            gel.pos.x + xoffset,
                            gel.pos.y + ITEM_TEXT_OUTER_PADDING,
                            gel.pos.z
                        )
                    override val tex = gel.textTex
                    override val frameSize = MutablePoint2f(tex.width, tex.height)
                    override val opacity get() = gel.opacity
                    override val brightness get() = if (gel.hilited) 1.0f else 0.8f
                }
            )
        }

        private fun makeImageRenderer(gel: ButtonGel): Renderer? {
            val imageTex = gel.imageTex ?: return null
            val imagePos = MutablePoint3f() // bound variable
            return App.factory.createUISpriteRenderer(
                object: UISpriteRenderer.Delegate() {
                    override val renderPos
                        get() = imagePos.set(
                            gel.pos.x + ITEM_TEXT_OUTER_PADDING,
                            gel.pos.y + ITEM_TEXT_OUTER_PADDING,
                            gel.pos.z
                        )
                    override val tex = imageTex
                    override val frameSize = MutablePoint2f(imageTex.width, imageTex.height)
                    override val opacity get() = gel.opacity
                }
            )
        }

        private fun makeValueRenderer(gel: ButtonGel): Renderer? {
            val value = gel.value ?: return null
            val incDecWidth = gel.incDec?.frameSize?.x ?: 0.0f
            val bgTex = gel.bgTex
            val valueTex = value.tex
            val valuePos = MutablePoint3f() // bound variable
            val left = bgTex.width - ITEM_TEXT_OUTER_PADDING - incDecWidth - ITEM_VALUE_MAX_WIDTH

            // Using floor is important here, because a sub-pixel offset would lead to artifacts
            val xOffset = floor(
                when (value.centreHorizontally) {
                    true -> left + ITEM_VALUE_MAX_WIDTH / 2 - value.frameSize.x / 2
                    false -> left
                }
            )
            val yOffset = floor(
                when (value.centreVertically) {
                    true -> bgTex.height / 2 - value.frameSize.y / 2
                    false -> ITEM_TEXT_OUTER_PADDING.toFloat()
                }
            )

            return App.factory.createUISpriteRenderer(
                object: UISpriteRenderer.Delegate() {
                    override val renderPos get() = valuePos.set(gel.pos.x + xOffset, gel.pos.y + yOffset, gel.pos.z)
                    override val tex = valueTex
                    override val frameSize = value.frameSize
                    override val texOffset = value.texOffset // since texOffset is mutable, we don't need a getter
                    override val opacity get() = gel.opacity
                    override val brightness get() = if (gel.hilited) 1.0f else 0.8f
                }
            )
        }

        private fun makeIncDecRenderer(gel: ButtonGel, isInc: Boolean): Renderer? {
            val incDec = gel.incDec ?: return null
            val bgTex = gel.bgTex
            val incDecTex = incDec.tex
            val incDecPos = MutablePoint3f() // bound variable
            val incDecWidth = incDec.frameSize.x

            val xOffset = floor(
                bgTex.width - ITEM_TEXT_OUTER_PADDING - incDecWidth - when (isInc) {
                    true -> 0f
                    false -> ITEM_VALUE_MAX_WIDTH + incDecWidth
                }
            )

            return App.factory.createUISpriteRenderer(
                object: UISpriteRenderer.Delegate() {
                    override val renderPos
                        get() = incDecPos.set(gel.pos.x + xOffset, gel.pos.y + ITEM_INCDEC_MARGIN_TOP, gel.pos.z)
                    override val tex = incDecTex
                    override val frameSize = incDec.frameSize
                    override val texOffset = if (isInc) incDec.incTexOffset else incDec.decTexOffset

                    override val opacity
                        get() = when (gel.hilited) {
                            true -> gel.opacity * when {
                                isInc && gel.canIncrement -> 1.0f
                                !isInc && gel.canDecrement -> 1.0f
                                else -> 0.42f
                            }
                            false -> 0.0f
                        }
                }
            )
        }
    }
}
