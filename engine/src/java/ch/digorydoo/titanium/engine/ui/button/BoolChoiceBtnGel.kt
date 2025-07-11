package ch.digorydoo.titanium.engine.ui.button

import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.titanium.engine.behaviours.Align
import ch.digorydoo.titanium.engine.behaviours.Glow
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.gel.GraphicElement
import ch.digorydoo.titanium.engine.shader.Renderer
import ch.digorydoo.titanium.engine.sound.EngineSampleId
import ch.digorydoo.titanium.engine.sprite.FrameCollection
import ch.digorydoo.titanium.engine.sprite.UISpriteRenderer
import ch.digorydoo.titanium.engine.texture.Texture
import ch.digorydoo.titanium.engine.ui.ITEM_TEXT_OUTER_PADDING
import ch.digorydoo.titanium.engine.ui.ITEM_VALUE_MAX_WIDTH
import ch.digorydoo.titanium.engine.ui.button.IButtonGel.Companion.FADE_DELAY_SECONDS
import ch.digorydoo.titanium.engine.ui.button.helper.BtnAlignDelegate
import ch.digorydoo.titanium.engine.ui.button.helper.ButtonRendererFactory
import ch.digorydoo.titanium.engine.ui.button.helper.ButtonTextureFactory
import ch.digorydoo.titanium.engine.ui.choice.BoolChoice
import kotlin.math.floor
import kotlin.math.max

class BoolChoiceBtnGel(
    private val choice: BoolChoice,
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

    private val switchFrames = FrameCollection().also {
        val img = App.textures.getOrLoadImageDataSync("ui-switch.png") // TODO load this async
        it.setTexture(img, 2, 1)
        it.setFrame(if (choice.curValue) 1 else 0)
    }

    private var _hilited = false

    override var hilited: Boolean
        get() = _hilited
        set(b) {
            _hilited = b
            if (b) glow.reset(0.25f)
        }

    override val canSelect = false

    override fun increment(smallStep: Boolean) {
        if (!choice.curValue) {
            App.sound.play(EngineSampleId.MENU_INC_DEC)
            choice.curValue = true
            valueChanged()
        } else {
            App.sound.play(EngineSampleId.NO_ACTION)
        }
    }

    override fun decrement(smallStep: Boolean) {
        if (choice.curValue) {
            App.sound.play(EngineSampleId.MENU_INC_DEC)
            choice.curValue = false
            valueChanged()
        } else {
            App.sound.play(EngineSampleId.NO_ACTION)
        }
    }

    private fun valueChanged() {
        switchFrames.setFrame(if (choice.curValue) 1 else 0)
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
        // switchFrames.tex is a shared texture
    }

    override fun toString() = "BoolChoiceBtnGel($choice)"

    companion object {
        private fun makeCombinedRenderer(gel: BoolChoiceBtnGel): Renderer {
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
            val switch = makeSwitchRenderer(gel)

            return object: Renderer {
                override fun renderShadows() {}
                override fun renderSolid() {}

                override fun renderTransparent() {
                    bg.renderTransparent()
                    otl.renderTransparent()
                    text.renderTransparent()
                    switch.renderTransparent()
                }

                override fun free() {
                    bg.free()
                    otl.free()
                    text.free()
                    switch.free()
                }
            }
        }

        private fun makeSwitchRenderer(gel: BoolChoiceBtnGel): Renderer {
            val bgTex = gel.bgTex
            val frames = gel.switchFrames
            val valueTex = frames.tex
            val valuePos = MutablePoint3f() // bound variable
            val left = bgTex.width.toFloat() - ITEM_TEXT_OUTER_PADDING - ITEM_VALUE_MAX_WIDTH

            // Using floor is important here, because a sub-pixel offset would lead to artifacts
            val xOffset = floor(left + ITEM_VALUE_MAX_WIDTH / 2 - frames.frameSize.x / 2)
            val yOffset = floor(bgTex.height / 2 - frames.frameSize.y / 2)

            return App.factory.createUISpriteRenderer(
                object: UISpriteRenderer.Delegate() {
                    override val renderPos get() = valuePos.set(gel.pos.x + xOffset, gel.pos.y + yOffset, gel.pos.z)
                    override val tex = valueTex
                    override val frameSize = frames.frameSize // shared mutable object
                    override val texOffset = frames.texOffset // shared mutable object
                    override val opacity get() = gel.opacity
                    override val brightness get() = if (gel.hilited) 1.0f else 0.8f
                }
            )
        }
    }
}
