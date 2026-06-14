package io.github.digorydoo.titanium.engine.ui.dlg_item

import io.github.digorydoo.titanium.engine.behaviours.Align
import io.github.digorydoo.titanium.engine.behaviours.Glow
import io.github.digorydoo.titanium.engine.core.App
import io.github.digorydoo.titanium.engine.gel.GraphicElement
import io.github.digorydoo.titanium.engine.shader.Renderer
import io.github.digorydoo.titanium.engine.sound.EngineSampleId
import io.github.digorydoo.titanium.engine.sprite.FrameCollection
import io.github.digorydoo.titanium.engine.texture.Texture
import io.github.digorydoo.titanium.engine.ui.button.ButtonTextureFactory.makeSavegameImageTexture
import io.github.digorydoo.titanium.engine.ui.dialogue.*
import kotlin.math.max
import kotlin.math.pow

class DlgItemGel private constructor(
    val def: DlgItemDef,
    private val impl: Impl,
    private val alignment: Align.Alignment,
    val width: Int,
    val height: Int,
    override val renderer: Renderer,
): GraphicElement() {
    interface Impl {
        val canSelect: Boolean

        val canToggle: Boolean
        fun toggle() {}

        val curValueAsString: String

        val canIncrement: Boolean
        fun increment(smallStep: Boolean) {}

        val canDecrement: Boolean
        fun decrement(smallStep: Boolean) {}
    }

    init {
        inDialog = Visibility.ACTIVE
        inMenu = Visibility.ACTIVE
        inEditor = Visibility.ACTIVE
    }

    var scrollOffset = 0f

    private val align = Align(
        this,
        object: Align.Alignment(alignment) {
            override val yOffset get() = scrollOffset.toInt() + alignment.yOffset
        },
        object: Align.Delegate {
            override val width = this@DlgItemGel.width
            override val height = this@DlgItemGel.height
        }
    )

    val autoDismiss: Boolean
        get() = def.let {
            when (it) {
                is DlgTextItemDef -> it.autoDismiss
                is DlgSavegameItemDef -> true
                else -> false
            }
        }

    private val glowProps = object: Glow.Delegate {
        override var brightness = 1f
    }

    val brightness get() = glowProps.brightness

    private val glow = Glow(glowProps).apply {
        minBrite = 0.0f
        maxBrite = 1.0f
    }

    private var _hilited = false

    var hilited: Boolean
        get() = _hilited
        set(b) {
            _hilited = b
            if (b) glow.reset(0.25f)
        }

    val opacity: Float
        get() = when {
            !fading -> 1.0f
            selected -> max(0.0f, 1.0f - ((App.time.sessionTime - fadeTime) / FADE_DELAY_SECONDS).pow(3.0f))
            else -> max(0.0f, 1.0f - (App.time.sessionTime - fadeTime) / FADE_DELAY_SECONDS)
        }

    private var fading = false
    private var fadeTime = 0.0f

    fun fadeOut() {
        if (!fading) {
            fadeTime = App.time.sessionTime
            fading = true
        }
    }

    val canSelect get() = impl.canSelect
    val canToggle get() = impl.canToggle
    var selected = false; private set
    var selectTime = 0.0f; private set
    private var selectCallback: (() -> Unit)? = null

    fun animateSelectAndThen(callback: () -> Unit) {
        if (canSelect && !selected) {
            App.sound.play(EngineSampleId.BUTTON1)
            selectTime = App.time.sessionTime
            selectCallback = callback
            selected = true
            glow.reset(0.25f)
        }
    }

    fun toggle() {
        impl.toggle()
    }

    val curValueAsString get() = impl.curValueAsString
    val canIncrement get() = impl.canIncrement
    val canDecrement get() = impl.canDecrement

    fun increment(smallStep: Boolean) {
        impl.increment(smallStep)
    }

    fun decrement(smallStep: Boolean) {
        impl.decrement(smallStep)
    }

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

    override fun onRemoveZombie() {
        renderer.free()
    }

    companion object {
        const val SELECT_DELAY_SECONDS = 0.3f
        const val FADE_DELAY_SECONDS = 0.3f

        fun create(
            def: DlgItemDef,
            alignment: Align.Alignment,
            btnWidth: Int,
            btnHeight: Int,
            textTex: Texture,
        ): DlgItemGel {
            val thumbnailTex = if (def !is DlgSavegameItemDef) null else makeSavegameImageTexture(def.summary)

            val switchFrames = if (def !is DlgBooleanItemDef) null else FrameCollection().also {
                val img = App.textures.getOrLoadImageDataSync("ui-switch.png")
                it.setTexture(img, 2, 1)
                it.setFrame(if (def.initialValue) 1 else 0)
            }

            val incDecFrames = when (def) {
                is DlgIntItemDef, is DlgFloatItemDef -> FrameCollection().also {
                    val img = App.textures.getOrLoadImageDataSync("ui-incdec.png")
                    it.setTexture(img, 2, 1)
                }
                else -> null
            }

            val renderer = DlgItemRenderer()

            val impl = when (def) {
                is DlgBooleanItemDef -> DlgBooleanItemGelImpl(
                    def,
                    onValueChanged = { b ->
                        switchFrames?.setFrame(if (b) 1 else 0)
                        def.onChange?.invoke(b)
                    }
                )
                is DlgIntItemDef -> DlgIntItemGelImpl(
                    def,
                    onValueChanged = { i ->
                        renderer.onValueChanged()
                        def.onChange?.invoke(i)
                    }
                )
                is DlgFloatItemDef -> DlgFloatItemGelImpl(
                    def,
                    onValueChanged = { f ->
                        renderer.onValueChanged()
                        def.onChange?.invoke(f)
                    }
                )
                is DlgSavegameItemDef -> DlgSavegameItemGelImpl()
                is DlgTextItemDef -> DlgTextItemGelImpl()
            }

            val gel = DlgItemGel(def, impl, alignment, btnWidth, btnHeight, renderer)
            renderer.attach(gel, textTex, thumbnailTex, switchFrames, incDecFrames)
            return gel
        }
    }
}
