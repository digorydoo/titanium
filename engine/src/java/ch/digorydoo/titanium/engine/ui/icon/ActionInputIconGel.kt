package ch.digorydoo.titanium.engine.ui.icon

import ch.digorydoo.kutils.colour.Colour
import ch.digorydoo.kutils.math.accel
import ch.digorydoo.kutils.math.decel
import ch.digorydoo.kutils.math.lerp
import ch.digorydoo.kutils.point.MutablePoint2f
import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.kutils.point.Point2f
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.core.FrameCounter
import ch.digorydoo.titanium.engine.font.FontManager.FontName
import ch.digorydoo.titanium.engine.gel.GraphicElement
import ch.digorydoo.titanium.engine.i18n.EngineTextId
import ch.digorydoo.titanium.engine.i18n.ITextId
import ch.digorydoo.titanium.engine.shader.Renderer
import ch.digorydoo.titanium.engine.texture.GreyscaleImageBuffer
import ch.digorydoo.titanium.engine.texture.Texture
import ch.digorydoo.titanium.engine.ui.UISpriteRenderer
import kotlin.math.min

class ActionInputIconGel: GraphicElement() {
    init {
        inDialog = Visibility.ACTIVE
        inMenu = Visibility.ACTIVE
        inEditor = Visibility.ACTIVE
        visibleOnScreenshots = false
        hidden = true // the gel is initially hidden, an explicit call to show() is needed to set the verb
    }

    private enum class FadeState { FADING_IN, FADING_OUT, IDLE }

    var verb: ITextId = EngineTextId.CANCEL; private set

    private val bgTex = App.textures.getOrCreateTexture("ui-action-input-bg.png")
    private val bgFrameSize = MutablePoint2f(bgTex?.width?.toFloat() ?: 0.0f, bgTex?.height?.toFloat() ?: 0.0f)
    private val bgPos = MutablePoint3f()
    private var bgOpacity = 0.0f

    private val iconFrames = InputIconFrames(Icon.A, Icon.RETURN)
    private val iconPos = MutablePoint3f()
    private val iconScaleFactor = MutablePoint2f(ICON_MIN_SCALE_FACTOR, ICON_MIN_SCALE_FACTOR)
    private var iconOpacity = 0.0f

    private var textTex: Texture? = null
    private val cachedTextTextures = mutableMapOf<ITextId, Texture>()
    private val textFrameSize = MutablePoint2f()
    private val textPos = MutablePoint3f()
    private var textOpacity = 0.0f

    private var fadeState = FadeState.IDLE
    private var fadeStartTime = 0.0f
    private val refreshCounter = FrameCounter.everyNthSecond(REFRESH_INTERVAL)
    private var forceRefresh = true

    override val renderer = makeRenderer()

    private fun makeRenderer(): Renderer {
        val bgRenderer = App.factory.createUISpriteRenderer(
            object: UISpriteRenderer.Delegate() {
                override val renderPos = this@ActionInputIconGel.bgPos
                override val frameSize get() = this@ActionInputIconGel.bgFrameSize
                override val tex get() = this@ActionInputIconGel.bgTex
                override val opacity get() = this@ActionInputIconGel.bgOpacity
                override val scaleFactor = Point2f(BG_SCALE_FACTOR, BG_SCALE_FACTOR)
            }
        )
        val iconRenderer = App.factory.createUISpriteRenderer(
            object: UISpriteRenderer.Delegate() {
                override val renderPos = this@ActionInputIconGel.iconPos
                override val frameSize = iconFrames.frameSize // shared mutable object
                override val tex get() = iconFrames.tex
                override val texOffset = iconFrames.texOffset // shared mutable object
                override val opacity get() = this@ActionInputIconGel.iconOpacity
                override val scaleFactor = iconScaleFactor // shared mutable object
            }
        )
        val textRenderer = App.factory.createUISpriteRenderer(
            object: UISpriteRenderer.Delegate() {
                override val renderPos = this@ActionInputIconGel.textPos
                override val frameSize get() = this@ActionInputIconGel.textFrameSize
                override val tex get() = this@ActionInputIconGel.textTex
                override val opacity get() = this@ActionInputIconGel.textOpacity
            }
        )
        return object: Renderer {
            override fun renderShadows() {}

            override fun renderSolid() {
                bgRenderer.renderSolid()
                iconRenderer.renderSolid()
                textRenderer.renderSolid()
            }

            override fun renderTransparent() {
                bgRenderer.renderTransparent()
                iconRenderer.renderTransparent()
                textRenderer.renderTransparent()
            }

            override fun free() {
                bgRenderer.free()
                iconRenderer.free()
                textRenderer.free()
            }
        }
    }

    /**
     * This function may be called repeatedly by GameHUD, so make sure it's efficient, esp. when there's no change
     */
    fun show(verb: ITextId) {
        if (this.verb != verb || textTex == null) {
            this.verb = verb
            val tex = cachedTextTextures[verb] ?: makeTextTexture(verb).also { cachedTextTextures[verb] = it }
            textTex = tex
            textFrameSize.set(tex.width, tex.height)
        } else if (!hidden && fadeState != FadeState.FADING_OUT) {
            return // nothing to do
        }

        fadeState = FadeState.FADING_IN
        fadeStartTime = App.time.sessionTime
        forceRefresh = true
        hidden = false

        // We may have aborted FADING_OUT, so make sure to have the correct start values
        iconScaleFactor.set(ICON_MIN_SCALE_FACTOR, ICON_MIN_SCALE_FACTOR)
        bgOpacity = 0.0f
        iconOpacity = 0.0f
        textOpacity = 0.0f
    }

    /**
     * This function may be called repeatedly by GameHUD, so make sure it's efficient, esp. when there's no change
     */
    fun hide() {
        if (hidden || fadeState == FadeState.FADING_OUT) {
            return // nothing to do
        }

        fadeState = FadeState.FADING_OUT
        fadeStartTime = App.time.sessionTime

        // We may have aborted FADING_IN, so make sure to have the correct start values
        iconScaleFactor.set(ICON_FINAL_SCALE_FACTOR, ICON_FINAL_SCALE_FACTOR)
        bgOpacity = BG_OPACITY
        iconOpacity = 1.0f
        textOpacity = 1.0f
    }

    override fun onAnimateActive() {
        var iconOffsetX = 0.0f
        var iconOffsetY = 0.0f
        var textOffsetX = 0.0f

        when (fadeState) {
            FadeState.IDLE -> Unit
            FadeState.FADING_IN -> {
                val rel = (App.time.sessionTime - fadeStartTime) / FADING_IN_DURATION

                if (rel >= 1.0f) {
                    iconScaleFactor.set(ICON_FINAL_SCALE_FACTOR, ICON_FINAL_SCALE_FACTOR)
                    bgOpacity = BG_OPACITY
                    iconOpacity = 1.0f
                    textOpacity = 1.0f
                    fadeState = FadeState.IDLE
                } else {
                    bgOpacity = BG_OPACITY * decel(rel, 2.0f) // slower towards the end
                    textOpacity = decel(rel, 1.2f)
                    iconOpacity = decel(rel, 1.1f)

                    val scale = when {
                        rel < SCALE_STEP1 -> lerp(
                            ICON_MIN_SCALE_FACTOR,
                            ICON_MAX_SCALE_FACTOR,
                            rel / SCALE_STEP1,
                        )
                        rel < SCALE_STEP2 -> lerp(
                            ICON_MAX_SCALE_FACTOR,
                            ICON_FINAL_SCALE_FACTOR,
                            (rel - SCALE_STEP1) / (SCALE_STEP2 - SCALE_STEP1),
                        )
                        else -> ICON_FINAL_SCALE_FACTOR
                    }

                    iconScaleFactor.set(scale, scale)

                    iconOffsetX = iconFrames.frameSize.x * (ICON_FINAL_SCALE_FACTOR - scale) / 2.0f
                    iconOffsetY = iconFrames.frameSize.y * (ICON_FINAL_SCALE_FACTOR - scale) / 2.0f
                    textOffsetX = TEXT_TRANSITION_X * (
                        1.0f - decel(min(TEXT_TRANSITION_REL_DURATION, rel) / TEXT_TRANSITION_REL_DURATION, 1.15f))
                    forceRefresh = true
                }
            }
            FadeState.FADING_OUT -> {
                val rel = (App.time.sessionTime - fadeStartTime) / FADING_OUT_DURATION

                if (rel >= 1.0f) {
                    bgOpacity = 0.0f
                    iconOpacity = 0.0f
                    textOpacity = 0.0f
                    fadeState = FadeState.IDLE
                    hidden = true // sleep until someone calls show() again
                } else {
                    bgOpacity = BG_OPACITY * accel(1.0f - rel, 1.1f) // faster towards the end
                    textOpacity = accel(1.0f - rel, 1.2f)
                    iconOpacity = accel(1.0f - rel, 2.0f)
                }
            }
        }

        if (forceRefresh || refreshCounter.next() == 0) {
            forceRefresh = false

            // Watch keyboard/gamepad and adjust icon
            iconFrames.update()

            // Watch screen size and realign
            val screenWidth = App.screenWidthDp
            val screenHeight = App.screenHeightDp

            bgPos.apply {
                x = screenWidth - PROMINENT_ACTION_DELTA_X
                y = screenHeight - PROMINENT_ACTION_DELTA_Y
            }
            iconPos.apply {
                x = bgPos.x + ICON_POS_X_OFFSET + iconOffsetX
                y = bgPos.y + ICON_POS_Y_OFFSET + iconOffsetY
            }
            textPos.apply {
                x = bgPos.x + TEXT_POS_X_OFFSET + textOffsetX
                y = bgPos.y + TEXT_POS_Y_OFFSET
            }
        }
    }

    override fun onRemoveZombie() {
        renderer.free()

        cachedTextTextures.forEach { _, texture ->
            texture.freeRequireUnshared()
            if (textTex == texture) textTex = null
        }

        cachedTextTextures.clear()
        textTex?.freeRequireUnshared() // just in case it wasn't found in the cache
        // bgTex is shared and is not freed here
    }

    companion object {
        private const val PROMINENT_ACTION_DELTA_X = 512.0f
        private const val PROMINENT_ACTION_DELTA_Y = 256.0f
        private const val BG_SCALE_FACTOR = 0.5f
        private const val BG_OPACITY = 0.2f
        private const val ICON_MIN_SCALE_FACTOR = 0.25f
        private const val ICON_MAX_SCALE_FACTOR = 0.64f
        private const val ICON_FINAL_SCALE_FACTOR = 0.375f
        private const val SCALE_STEP1 = 0.06f
        private const val SCALE_STEP2 = 0.26f
        private const val ICON_POS_X_OFFSET = 14.0f
        private const val ICON_POS_Y_OFFSET = 14.0f
        private const val TEXT_POS_X_OFFSET = ICON_POS_X_OFFSET + 48.0f
        private const val TEXT_POS_Y_OFFSET = ICON_POS_Y_OFFSET - 3.0f
        private const val TEXT_TRANSITION_X = 48.0f
        private const val TEXT_TRANSITION_REL_DURATION = 0.2f
        private const val REFRESH_INTERVAL = 0.5f // seconds
        private const val FADING_IN_DURATION = 1.0f // seconds
        private const val FADING_OUT_DURATION = 0.25f // seconds

        private fun makeTextTexture(textId: ITextId): Texture {
            val text = App.i18n.getString(textId)
            val font = FontName.LARGE_HUD_FONT
            val size = App.fonts.measureText(text, font)
            val padding = GreyscaleImageBuffer.OUTLINE_RANGE
            size.x += 2 * padding
            size.y += 2 * padding
            return App.textures.createTexture(size.x.toInt(), size.y.toInt()).apply {
                drawInto {
                    clear(Colour.transparent)
                    drawText(text, padding, padding, Colour.white, font, Colour.grey900)
                }
            }
        }
    }
}
