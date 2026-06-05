package io.github.digorydoo.titanium.engine.ui.dlg_item

import io.github.digorydoo.titanium.engine.behaviours.Align
import io.github.digorydoo.titanium.engine.behaviours.Glow
import io.github.digorydoo.titanium.engine.core.App
import io.github.digorydoo.titanium.engine.gel.GraphicElement
import io.github.digorydoo.titanium.engine.shader.Renderer
import io.github.digorydoo.titanium.engine.sound.EngineSampleId
import io.github.digorydoo.titanium.engine.texture.Texture
import io.github.digorydoo.titanium.engine.ui.button.BtnAlignDelegate
import io.github.digorydoo.titanium.engine.ui.button.ButtonRendererFactory
import io.github.digorydoo.titanium.engine.ui.button.ButtonTextureFactory
import io.github.digorydoo.titanium.engine.ui.dialogue.DlgTextItemDef
import io.github.digorydoo.titanium.engine.ui.dlg_item.DlgItemGel.Companion.FADE_DELAY_SECONDS
import io.github.digorydoo.titanium.engine.ui.dlg_item.DlgItemGel.Companion.SELECT_DELAY_SECONDS
import kotlin.math.max
import kotlin.math.pow

class DlgTextItemGel(
    override val def: DlgTextItemDef,
    alignment: Align.Alignment,
    override val btnWidth: Int,
    override val btnHeight: Int,
    precomputedTextTex: Texture?,
): GraphicElement(), DlgItemGel, CanAnimateSelectAndThen {
    init {
        inDialog = Visibility.ACTIVE
        inMenu = Visibility.ACTIVE
        inEditor = Visibility.ACTIVE
    }

    private val bgTex = ButtonTextureFactory.makeBgTexture(btnWidth, btnHeight)
    private val otlTex = ButtonTextureFactory.makeOtlTexture(btnWidth, btnHeight)
    private val textTex = precomputedTextTex ?: ButtonTextureFactory.makeTextTexture(def)

    private var _hilited = false

    override var hilited: Boolean
        get() = _hilited
        set(b) {
            _hilited = b
            if (b) glow.reset(0.25f)
        }

    override val autoDismiss = def.autoDismiss
    private var selected = false
    private var selectTime = 0.0f
    private var selectCallback: (() -> Unit)? = null

    override fun animateSelectAndThen(callback: () -> Unit) {
        if (!selected) {
            App.sound.play(EngineSampleId.BUTTON1)
            selectTime = App.time.sessionTime
            selectCallback = callback
            selected = true
            glow.reset(0.25f)
        }
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
            selected -> max(0.0f, 1.0f - ((App.time.sessionTime - fadeTime) / FADE_DELAY_SECONDS).pow(3.0f))
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
        if (selected && selectCallback != null) {
            val t = App.time.sessionTime - selectTime

            if (t >= SELECT_DELAY_SECONDS) {
                selected = false
                glow.reset(-0.5f)

                selectCallback?.invoke()
                selectCallback = null
            }
        }

        align.animate()
        glow.animate()
    }

    override val renderer = makeCombinedRenderer(this)

    override fun onRemoveZombie() {
        renderer.free()

        textTex.freeRequireUnshared()
        bgTex.freeRequireUnshared()
        otlTex.freeRequireUnshared()
    }

    override fun toString() = "DlgTextItemGel($def)"

    companion object {
        private fun makeCombinedRenderer(gel: DlgTextItemGel): Renderer {
            val delegate = object: ButtonRendererFactory.Delegate {
                override val pos = gel.pos // shared mutable object
                override val opacity get() = gel.opacity
                override val brightness get() = gel.glowProps.brightness
                override val hilited get() = gel._hilited
                override val selected get() = gel.selected
                override val selectTime get() = gel.selectTime
            }

            val bg = ButtonRendererFactory.makeBgRenderer(delegate, gel.bgTex)
            val otl = ButtonRendererFactory.makeOtlRenderer(delegate, gel.otlTex)
            val text = ButtonRendererFactory.makeTextRenderer(delegate, gel.textTex)

            return object: Renderer {
                override fun renderShadows() {}
                override fun renderSolid() {}

                override fun renderTransparent() {
                    bg.renderTransparent()
                    otl.renderTransparent()
                    text.renderTransparent()
                }

                override fun free() {
                    bg.free()
                    otl.free()
                    text.free()
                }
            }
        }
    }
}
