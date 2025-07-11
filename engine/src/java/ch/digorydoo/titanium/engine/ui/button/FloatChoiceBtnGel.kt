package ch.digorydoo.titanium.engine.ui.button

import ch.digorydoo.kutils.colour.Colour
import ch.digorydoo.kutils.math.clamp
import ch.digorydoo.kutils.point.MutablePoint2f
import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.kutils.point.Point2f
import ch.digorydoo.kutils.string.toPrecision
import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.engine.behaviours.Align
import ch.digorydoo.titanium.engine.behaviours.Glow
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.font.FontManager.FontName.DIALOG_FONT
import ch.digorydoo.titanium.engine.gel.GraphicElement
import ch.digorydoo.titanium.engine.shader.Renderer
import ch.digorydoo.titanium.engine.sound.EngineSampleId
import ch.digorydoo.titanium.engine.sprite.FrameCollection
import ch.digorydoo.titanium.engine.sprite.UISpriteRenderer
import ch.digorydoo.titanium.engine.texture.Texture
import ch.digorydoo.titanium.engine.ui.ITEM_INCDEC_MARGIN_TOP
import ch.digorydoo.titanium.engine.ui.ITEM_TEXT_INNER_PADDING
import ch.digorydoo.titanium.engine.ui.ITEM_TEXT_OUTER_PADDING
import ch.digorydoo.titanium.engine.ui.ITEM_VALUE_MAX_WIDTH
import ch.digorydoo.titanium.engine.ui.button.IButtonGel.Companion.FADE_DELAY_SECONDS
import ch.digorydoo.titanium.engine.ui.button.helper.BtnAlignDelegate
import ch.digorydoo.titanium.engine.ui.button.helper.ButtonRendererFactory
import ch.digorydoo.titanium.engine.ui.button.helper.ButtonTextureFactory
import ch.digorydoo.titanium.engine.ui.choice.FloatChoice
import ch.digorydoo.titanium.engine.ui.dlgTextColour
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.round

class FloatChoiceBtnGel(
    private val choice: FloatChoice,
    alignment: Align.Alignment,
    override val btnWidth: Int,
    override val btnHeight: Int,
    precomputedTextTex: Texture?,
): GraphicElement(), IButtonGel {
    init {
        inDialog = Visibility.ACTIVE
        inMenu = Visibility.ACTIVE
        inEditor = Visibility.ACTIVE
    }

    private val bgTex = ButtonTextureFactory.makeBgTexture(btnWidth, btnHeight)
    private val otlTex = ButtonTextureFactory.makeOtlTexture(btnWidth, btnHeight)
    private val textTex = precomputedTextTex ?: ButtonTextureFactory.makeTextTexture(choice)

    private val valueTex = makeStringValueTexture(choice.curValue.toPrecision(2))
    val valueFrameSize = MutablePoint2f(valueTex.width, valueTex.height)

    val incDecFrames = FrameCollection()
    val incTexOffset: Point2f
    val decTexOffset: Point2f

    init {
        val img = App.textures.getOrLoadImageDataSync("ui-incdec.png")
        incDecFrames.setTexture(img, 2, 1)

        incDecFrames.setFrame(0)
        decTexOffset = Point2f(incDecFrames.texOffset)

        incDecFrames.setFrame(1)
        incTexOffset = Point2f(incDecFrames.texOffset)
    }

    private var _hilited = false

    override var hilited: Boolean
        get() = _hilited
        set(b) {
            _hilited = b
            if (b) glow.reset(0.25f)
        }

    override val canSelect = false

    private val canIncrement get() = choice.curValue < choice.maxValue
    private val canDecrement get() = choice.curValue > choice.minValue

    override fun increment(smallStep: Boolean) {
        if (canIncrement) {
            val oldValue = choice.curValue
            choice.curValue += if (smallStep) choice.smallStep else choice.step
            keepConstraints()

            if (oldValue < choice.initialValue && choice.curValue > choice.initialValue) {
                choice.curValue = choice.initialValue // startValue may be outside step grid or even outside range
            }

            App.sound.play(EngineSampleId.MENU_INC_DEC)
            valueChanged()
        } else {
            App.sound.play(EngineSampleId.NO_ACTION)
        }
    }

    override fun decrement(smallStep: Boolean) {
        if (canDecrement) {
            val oldValue = choice.curValue
            choice.curValue -= if (smallStep) choice.smallStep else choice.step
            keepConstraints()

            if (oldValue > choice.initialValue && choice.curValue < choice.initialValue) {
                choice.curValue = choice.initialValue
            }

            App.sound.play(EngineSampleId.MENU_INC_DEC)
            valueChanged()
        } else {
            App.sound.play(EngineSampleId.NO_ACTION)
        }
    }

    private fun keepConstraints() {
        if (choice.modulo != null) {
            choice.curValue = (choice.curValue + choice.modulo) % choice.modulo
        }

        choice.curValue = round(choice.curValue / choice.smallStep) * choice.smallStep
        choice.curValue = clamp(choice.curValue, choice.minValue, choice.maxValue)
    }

    private fun valueChanged() {
        redrawStringValue(valueTex, choice.curValue.toPrecision(2))
        choice.onChange?.invoke(choice.curValue)
    }

    private var fading = false
    private var fadeTime = 0.0f

    override fun fadeOut() {
        if (!fading) {
            fadeTime = App.time.sessionTime
            fading = true
        }
    }

    override fun show() {
        setHiddenOnNextFrameTo = false
    }

    override fun hide() {
        setHiddenOnNextFrameTo = true
    }

    private val opacity: Float
        get() = when {
            !fading -> 1.0f
            else -> max(0.0f, 1.0f - (App.time.sessionTime - fadeTime) / FADE_DELAY_SECONDS)
        }

    private val glowProps = object: Glow.Delegate {
        override var brightness = 1.0f
    }

    private val glow = Glow(glowProps).apply {
        minBrite = 0.0f
        maxBrite = 1.0f
    }

    override var scrollOffset = 0.0f
    override val height get() = bgTex.height

    private val align = Align(BtnAlignDelegate(this, alignment))

    override fun onAnimateActive() {
        align.animate()
        glow.animate()
    }

    override val renderer = makeCombinedRenderer(this)

    override fun onRemoveZombie() {
        renderer.free()

        textTex.freeRequireUnshared()
        bgTex.freeRequireUnshared()
        otlTex.freeRequireUnshared()
        valueTex.freeRequireUnshared()
        // incDecFrames.tex is a shared texture
    }

    override fun toString() = "FloatChoiceBtnGel($choice)"

    companion object {
        private val TAG = Log.Tag("FloatChoiceBtnGel")

        private fun makeCombinedRenderer(gel: FloatChoiceBtnGel): Renderer {
            val delegate = object: ButtonRendererFactory.Delegate {
                override val pos = gel.pos // shared mutable object
                override val opacity get() = gel.opacity
                override val brightness get() = gel.glowProps.brightness
                override val hilited get() = gel._hilited
                override val selected = false
                override val selectTime = 0.0f
            }

            val bg = ButtonRendererFactory.makeBgRenderer(delegate, gel.bgTex)
            val otl = ButtonRendererFactory.makeOtlRenderer(delegate, gel.otlTex)
            val text = ButtonRendererFactory.makeTextRenderer(delegate, gel.textTex)
            val value = makeValueRenderer(gel)
            val inc = makeIncDecRenderer(gel, isInc = true)
            val dec = makeIncDecRenderer(gel, isInc = false)

            return object: Renderer {
                override fun renderShadows() {}
                override fun renderSolid() {}

                override fun renderTransparent() {
                    bg.renderTransparent()
                    otl.renderTransparent()
                    text.renderTransparent()
                    value.renderTransparent()
                    inc.renderTransparent()
                    dec.renderTransparent()
                }

                override fun free() {
                    bg.free()
                    otl.free()
                    text.free()
                    value.free()
                    inc.free()
                    dec.free()
                }
            }
        }

        private fun makeValueRenderer(gel: FloatChoiceBtnGel): Renderer {
            val incDecFrames = gel.incDecFrames
            val incDecWidth = incDecFrames.frameSize.x
            val bgTex = gel.bgTex
            val valueTex = gel.valueTex
            val valuePos = MutablePoint3f() // bound variable
            val left = bgTex.width - ITEM_TEXT_OUTER_PADDING - incDecWidth - ITEM_VALUE_MAX_WIDTH

            // Using floor is important here, because a sub-pixel offset would lead to artifacts
            val xOffset = floor(left + ITEM_VALUE_MAX_WIDTH / 2 - gel.valueFrameSize.x / 2)
            val yOffset = floor(ITEM_TEXT_OUTER_PADDING.toFloat())

            return App.factory.createUISpriteRenderer(
                object: UISpriteRenderer.Delegate() {
                    override val renderPos get() = valuePos.set(gel.pos.x + xOffset, gel.pos.y + yOffset, gel.pos.z)
                    override val tex = valueTex
                    override val frameSize = gel.valueFrameSize
                    override val opacity get() = gel.opacity
                    override val brightness get() = if (gel.hilited) 1.0f else 0.8f
                }
            )
        }

        private fun makeIncDecRenderer(gel: FloatChoiceBtnGel, isInc: Boolean): Renderer {
            val bgTex = gel.bgTex
            val incDecFrames = gel.incDecFrames
            val incDecTex = incDecFrames.tex
            val incDecPos = MutablePoint3f() // bound variable
            val incDecWidth = gel.incDecFrames.frameSize.x

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
                    override val frameSize = incDecFrames.frameSize // shared mutable object
                    override val texOffset = if (isInc) gel.incTexOffset else gel.decTexOffset

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

        private fun makeStringValueTexture(value: String): Texture {
            val sz = App.fonts.measureText("0", DIALOG_FONT)
            val textWidth = ITEM_VALUE_MAX_WIDTH + 2 * ITEM_TEXT_INNER_PADDING
            val textHeight = ceil(sz.y).toInt() + 2 * ITEM_TEXT_INNER_PADDING
            return App.textures.createTexture(textWidth, textHeight).also {
                Log.info(TAG, "Item value texture created: w=${it.width}, h=${it.height}, value=$value")
                redrawStringValue(it, value)
            }
        }

        private fun redrawStringValue(tex: Texture, value: String) {
            tex.drawInto {
                clear(Colour.transparent)
                drawTextCentred(value, tex.width / 2, ITEM_TEXT_INNER_PADDING, dlgTextColour, DIALOG_FONT)
            }
        }
    }
}
