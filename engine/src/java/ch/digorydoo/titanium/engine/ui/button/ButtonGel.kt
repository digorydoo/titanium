package ch.digorydoo.titanium.engine.ui.button

import ch.digorydoo.kutils.point.MutablePoint2f
import ch.digorydoo.kutils.point.Point2f
import ch.digorydoo.titanium.engine.behaviours.Align
import ch.digorydoo.titanium.engine.behaviours.Glow
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.gel.GraphicElement
import ch.digorydoo.titanium.engine.texture.Texture
import ch.digorydoo.titanium.engine.ui.choice.Choice
import kotlin.math.max
import kotlin.math.pow

class ButtonGel(
    private val choice: Choice,
    private val alignment: Align.Alignment,
    val textTex: Texture,
    val bgTex: Texture,
    val otlTex: Texture,
    val value: ButtonValue?,
    val incDec: ButtonIncDec?,
    val imageTex: Texture?,
): GraphicElement() {
    init {
        inDialog = Visibility.ACTIVE
        inMenu = Visibility.ACTIVE
        inEditor = Visibility.ACTIVE
    }

    class ButtonValue(
        val tex: Texture,
        val frameSize: Point2f,
        val texOffset: MutablePoint2f, // offsets can change through callbacks received by ButtonBuilder
        val centreHorizontally: Boolean = true,
        val centreVertically: Boolean = true,
    )

    class ButtonIncDec(
        val tex: Texture,
        val frameSize: Point2f,
        val incTexOffset: Point2f,
        val decTexOffset: Point2f,
    )

    private var _hilited = false

    var hilited: Boolean
        get() = _hilited
        set(b) {
            _hilited = b
            if (b) glow.reset(0.25f)
        }

    var selected = false; private set
    var selectTime = 0.0f; private set
    private var selectCallback: (() -> Unit)? = null

    val canIncrement get() = choice.canIncrement
    val canDecrement get() = choice.canDecrement

    fun select() {
        selectAndCall {
            unselect()
            choice.onSelect()
        }
    }

    fun selectAndCall(callback: () -> Unit) {
        if (!selected) {
            selectTime = App.time.sessionTime
            selectCallback = callback
            selected = true
            glow.reset(0.25f)
        }
    }

    fun unselect() {
        selected = false
        selectCallback = null
        glow.reset(-0.5f)
    }

    private var fading = false
    private var fadeTime = 0.0f

    fun fadeOut() {
        if (!fading) {
            fadeTime = App.time.sessionTime
            fading = true
        }
    }

    val opacity: Float
        get() = when {
            !fading -> 1.0f
            selected -> max(0.0f, 1.0f - ((App.time.sessionTime - fadeTime) / FADE_DELAY_SECONDS).pow(3.0f))
            else -> max(0.0f, 1.0f - (App.time.sessionTime - fadeTime) / FADE_DELAY_SECONDS)
        }

    private val handleSelect = object: Behaviour {
        override fun animate() {
            if (selected && selectCallback != null) {
                val t = App.time.sessionTime - selectTime

                if (t >= SELECT_DELAY_SECONDS) {
                    selectCallback?.invoke()
                    selectCallback = null
                }
            }
        }
    }

    private val glowProps: Glow.Delegate = object: Glow.Delegate {
        override var brightness = 1.0f
    }

    val brightness: Float get() = glowProps.brightness

    private val glow = Glow(glowProps).apply {
        minBrite = 0.0f
        maxBrite = 1.0f
    }

    var scrollOffset = 0.0f
    val height get() = bgTex.height

    private val align = Align(
        object: Align.Delegate() {
            override val anchor = alignment.anchor
            override val xOffset = alignment.xOffset
            override val yOffset get() = this@ButtonGel.scrollOffset.toInt() + alignment.yOffset
            override val marginLeft = alignment.marginLeft
            override val marginTop = alignment.marginTop
            override val marginRight = alignment.marginRight
            override val marginBottom = alignment.marginBottom
            override val width = bgTex.width
            override val height = bgTex.height

            override fun setPos(x: Int, y: Int) {
                this@ButtonGel.pos.set(x, y, 0)
            }
        }
    )

    override fun onAnimateActive() {
        align.animate()
        handleSelect.animate()
        glow.animate()
    }

    override val renderer = ButtonRenderer.create(this)

    override fun onRemoveZombie() {
        renderer.free()

        // Textures that were generated such as text will be unshared.
        // Icon textures will be shared.
        // Shared textures are freed by TextureManager.
        textTex.freeRequireUnshared()
        bgTex.freeRequireUnshared()
        otlTex.freeRequireUnshared()
        imageTex?.freeRequireUnshared()
        value?.tex?.apply { if (!shared) freeRequireUnshared() }
        incDec?.tex?.apply { if (!shared) freeRequireUnshared() }
    }

    override fun toString() = "MenuItemGel($choice)"

    companion object {
        const val SELECT_DELAY_SECONDS = 0.3f
        private const val FADE_DELAY_SECONDS = 0.3f
    }
}
