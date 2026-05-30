package io.github.digorydoo.titanium.engine.ui.dlg_item

import ch.digorydoo.kutils.vector.MutableVector3f
import io.github.digorydoo.titanium.engine.behaviours.Align
import io.github.digorydoo.titanium.engine.behaviours.Glow
import io.github.digorydoo.titanium.engine.core.App
import io.github.digorydoo.titanium.engine.gel.GraphicElement
import io.github.digorydoo.titanium.engine.shader.Renderer
import io.github.digorydoo.titanium.engine.sound.EngineSampleId
import io.github.digorydoo.titanium.engine.sprite.FrameCollection
import io.github.digorydoo.titanium.engine.sprite.UISpriteRenderer
import io.github.digorydoo.titanium.engine.texture.Texture
import io.github.digorydoo.titanium.engine.ui.ITEM_TEXT_OUTER_PADDING
import io.github.digorydoo.titanium.engine.ui.ITEM_VALUE_MAX_WIDTH
import io.github.digorydoo.titanium.engine.ui.button.BtnAlignDelegate
import io.github.digorydoo.titanium.engine.ui.button.ButtonRendererFactory
import io.github.digorydoo.titanium.engine.ui.button.ButtonTextureFactory
import io.github.digorydoo.titanium.engine.ui.dialogue.DlgBooleanItemDef
import io.github.digorydoo.titanium.engine.ui.dlg_item.DlgItemGel.Companion.FADE_DELAY_SECONDS
import kotlin.math.floor
import kotlin.math.max

class DlgBooleanItemGel<Id>(
    override val def: DlgBooleanItemDef<Id>,
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

    private var curValue = def.initialValue

    private val bgTex = ButtonTextureFactory.makeBgTexture(btnWidth, btnHeight)
    private val otlTex = ButtonTextureFactory.makeOtlTexture(btnWidth, btnHeight)
    private val textTex = precomputedTextTex ?: ButtonTextureFactory.makeTextTexture(def)

    private val switchFrames = FrameCollection().also {
        val img = App.textures.getOrLoadImageDataSync("ui-switch.png") // TODO load this async
        it.setTexture(img, 2, 1)
        it.setFrame(if (curValue) 1 else 0)
    }

    private var _hilited = false

    override var hilited: Boolean
        get() = _hilited
        set(b) {
            _hilited = b
            if (b) glow.reset(0.25f)
        }

    override val canSelect = true // def has no onSelect, but we implement a toggle action
    override val autoDismiss = false

    override fun select(onBeforeAction: () -> Unit) {
        if (!curValue) increment(true)
        else decrement(true)
    }

    override fun increment(smallStep: Boolean) {
        if (!curValue) {
            App.sound.play(EngineSampleId.MENU_INC_DEC)
            curValue = true
            valueChanged()
        } else {
            App.sound.play(EngineSampleId.NO_ACTION)
        }
    }

    override fun decrement(smallStep: Boolean) {
        if (curValue) {
            App.sound.play(EngineSampleId.MENU_INC_DEC)
            curValue = false
            valueChanged()
        } else {
            App.sound.play(EngineSampleId.NO_ACTION)
        }
    }

    private fun valueChanged() {
        switchFrames.setFrame(if (curValue) 1 else 0)
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
        // switchFrames.tex is a shared texture
    }

    override fun toString() = "DlgBooleanItemGel($def)"

    companion object {
        private fun makeCombinedRenderer(gel: DlgBooleanItemGel<*>): Renderer {
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

        private fun makeSwitchRenderer(gel: DlgBooleanItemGel<*>): Renderer {
            val bgTex = gel.bgTex
            val frames = gel.switchFrames
            val valueTex = frames.tex
            val valuePos = MutableVector3f() // bound variable
            val left = bgTex.width.toFloat() - ITEM_TEXT_OUTER_PADDING - ITEM_VALUE_MAX_WIDTH

            // Using floor is important here, because a sub-pixel offset would lead to artefacts
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
