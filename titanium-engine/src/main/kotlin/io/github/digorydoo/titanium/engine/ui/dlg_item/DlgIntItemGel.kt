package io.github.digorydoo.titanium.engine.ui.dlg_item

import ch.digorydoo.kutils.colour.Colour
import ch.digorydoo.kutils.logging.Log
import ch.digorydoo.kutils.math.clamp
import ch.digorydoo.kutils.vector.MutableVector2f
import ch.digorydoo.kutils.vector.MutableVector3f
import ch.digorydoo.kutils.vector.Vector2f
import io.github.digorydoo.titanium.engine.behaviours.Align
import io.github.digorydoo.titanium.engine.behaviours.Glow
import io.github.digorydoo.titanium.engine.core.App
import io.github.digorydoo.titanium.engine.font.FontManager.FontName.DIALOG_FONT
import io.github.digorydoo.titanium.engine.gel.GraphicElement
import io.github.digorydoo.titanium.engine.shader.Renderer
import io.github.digorydoo.titanium.engine.sound.EngineSampleId
import io.github.digorydoo.titanium.engine.sprite.FrameCollection
import io.github.digorydoo.titanium.engine.sprite.UISpriteRenderer
import io.github.digorydoo.titanium.engine.texture.Texture
import io.github.digorydoo.titanium.engine.ui.ITEM_INCDEC_MARGIN_TOP
import io.github.digorydoo.titanium.engine.ui.ITEM_TEXT_INNER_PADDING
import io.github.digorydoo.titanium.engine.ui.ITEM_TEXT_OUTER_PADDING
import io.github.digorydoo.titanium.engine.ui.ITEM_VALUE_MAX_WIDTH
import io.github.digorydoo.titanium.engine.ui.button.BtnAlignDelegate
import io.github.digorydoo.titanium.engine.ui.button.ButtonRendererFactory
import io.github.digorydoo.titanium.engine.ui.button.ButtonTextureFactory
import io.github.digorydoo.titanium.engine.ui.dialogue.DlgIntItemDef
import io.github.digorydoo.titanium.engine.ui.dlgTextColour
import io.github.digorydoo.titanium.engine.ui.dlg_item.DlgItemGel.Companion.FADE_DELAY_SECONDS
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max

class DlgIntItemGel<Id>(
    override val def: DlgIntItemDef<Id>,
    alignment: Align.Alignment,
    override val btnWidth: Int,
    override val btnHeight: Int,
    precomputedTextTex: Texture?,
): GraphicElement(), DlgItemGel<Id> {
    init {
        inDialog = Visibility.ACTIVE
        inMenu = Visibility.ACTIVE
        inEditor = Visibility.ACTIVE
    }

    var curValue = def.initialValue

    private val bgTex = ButtonTextureFactory.makeBgTexture(btnWidth, btnHeight)
    private val otlTex = ButtonTextureFactory.makeOtlTexture(btnWidth, btnHeight)
    private val textTex = precomputedTextTex ?: ButtonTextureFactory.makeTextTexture(def)

    private val valueTex = makeStringValueTexture(curValue.toString())
    val valueFrameSize = MutableVector2f(valueTex.width, valueTex.height)

    val incDecFrames = FrameCollection()
    val incTexOffset: Vector2f
    val decTexOffset: Vector2f

    init {
        val img = App.textures.getOrLoadImageDataSync("ui-incdec.png")
        incDecFrames.setTexture(img, 2, 1)

        incDecFrames.setFrame(0)
        decTexOffset = Vector2f(incDecFrames.texOffset)

        incDecFrames.setFrame(1)
        incTexOffset = Vector2f(incDecFrames.texOffset)
    }

    private var _hilited = false

    override var hilited: Boolean
        get() = _hilited
        set(b) {
            _hilited = b
            if (b) glow.reset(0.25f)
        }

    override val canSelect = false
    override val autoDismiss = false

    private val canIncrement get() = curValue < def.maxValue
    private val canDecrement get() = curValue > def.minValue

    override fun increment(smallStep: Boolean) {
        if (canIncrement) {
            val oldValue = curValue
            curValue += if (smallStep) def.smallStep else def.step
            keepConstraints()

            if (oldValue < def.initialValue && curValue > def.initialValue) {
                curValue = def.initialValue // startValue may be outside step grid or even outside range
            }

            App.sound.play(EngineSampleId.MENU_INC_DEC)
            valueChanged()
        } else {
            App.sound.play(EngineSampleId.NO_ACTION)
        }
    }

    override fun decrement(smallStep: Boolean) {
        if (canDecrement) {
            val oldValue = curValue
            curValue -= if (smallStep) def.smallStep else def.step
            keepConstraints()

            if (oldValue > def.initialValue && curValue < def.initialValue) {
                curValue = def.initialValue
            }

            App.sound.play(EngineSampleId.MENU_INC_DEC)
            valueChanged()
        } else {
            App.sound.play(EngineSampleId.NO_ACTION)
        }
    }

    private fun keepConstraints() {
        curValue = clamp(curValue, def.minValue, def.maxValue)
    }

    private fun valueChanged() {
        redrawStringValue(valueTex, curValue.toString())
        def.onChange?.invoke(curValue)
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

    override fun toString() = "DlgIntItemGel($def)"

    companion object {
        private val TAG = Log.Tag("DlgIntItemGel")

        private fun makeCombinedRenderer(gel: DlgIntItemGel<*>): Renderer {
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

        private fun makeValueRenderer(gel: DlgIntItemGel<*>): Renderer {
            val incDecFrames = gel.incDecFrames
            val incDecWidth = incDecFrames.frameSize.x
            val bgTex = gel.bgTex
            val valueTex = gel.valueTex
            val valuePos = MutableVector3f() // bound variable
            val left = bgTex.width - ITEM_TEXT_OUTER_PADDING - incDecWidth - ITEM_VALUE_MAX_WIDTH

            // Using floor is important here, because a sub-pixel offset would lead to artefacts
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

        private fun makeIncDecRenderer(gel: DlgIntItemGel<*>, isInc: Boolean): Renderer {
            val bgTex = gel.bgTex
            val incDecFrames = gel.incDecFrames
            val incDecTex = incDecFrames.tex
            val incDecPos = MutableVector3f() // bound variable
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
