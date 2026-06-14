package io.github.digorydoo.titanium.engine.ui.dlg_item

import ch.digorydoo.kutils.vector.MutableVector2f
import ch.digorydoo.kutils.vector.MutableVector3f
import ch.digorydoo.kutils.vector.Vector2f
import io.github.digorydoo.titanium.engine.core.App
import io.github.digorydoo.titanium.engine.shader.Renderer
import io.github.digorydoo.titanium.engine.sprite.FrameCollection
import io.github.digorydoo.titanium.engine.sprite.UISpriteRenderer
import io.github.digorydoo.titanium.engine.texture.Texture
import io.github.digorydoo.titanium.engine.ui.ITEM_INCDEC_MARGIN_TOP
import io.github.digorydoo.titanium.engine.ui.ITEM_TEXT_OUTER_PADDING
import io.github.digorydoo.titanium.engine.ui.ITEM_VALUE_MAX_WIDTH
import io.github.digorydoo.titanium.engine.ui.SAVEGAME_THUMBNAIL_HEIGHT
import io.github.digorydoo.titanium.engine.ui.SAVEGAME_THUMBNAIL_WIDTH
import io.github.digorydoo.titanium.engine.ui.button.ButtonTextureFactory
import io.github.digorydoo.titanium.engine.ui.button.ButtonTextureFactory.makeStringValueTexture
import io.github.digorydoo.titanium.engine.ui.button.ButtonTextureFactory.redrawStringValue
import kotlin.math.floor
import kotlin.math.max

class DlgItemRenderer: Renderer {
    private var bgTex: Texture? = null
    private var otlTex: Texture? = null
    private var textTex: Texture? = null
    private var valueTex: Texture? = null
    private var thumbnailTex: Texture? = null

    private var bgRenderer: Renderer? = null
    private var otlRenderer: Renderer? = null
    private var textRenderer: Renderer? = null
    private var switchRenderer: Renderer? = null
    private var valueRenderer: Renderer? = null
    private var incRenderer: Renderer? = null
    private var decRenderer: Renderer? = null
    private var thumbnailRenderer: Renderer? = null

    private var gel: DlgItemGel? = null
    private var valueFrameSize = Vector2f.zero

    fun onValueChanged() {
        valueTex?.let { redrawStringValue(it, gel?.curValueAsString ?: "") }
    }

    override fun renderShadows() {}
    override fun renderSolid() {}

    override fun renderTransparent() {
        bgRenderer?.renderTransparent()
        otlRenderer?.renderTransparent()
        textRenderer?.renderTransparent()
        switchRenderer?.renderTransparent()
        valueRenderer?.renderTransparent()
        incRenderer?.renderTransparent()
        decRenderer?.renderTransparent()
        thumbnailRenderer?.renderTransparent()
    }

    override fun free() {
        bgRenderer?.free()
        otlRenderer?.free()
        thumbnailRenderer?.free()
        textRenderer?.free()
        switchRenderer?.free()
        valueRenderer?.free()
        incRenderer?.free()
        decRenderer?.free()

        bgTex?.freeRequireUnshared()
        otlTex?.freeRequireUnshared()
        textTex?.freeRequireUnshared()
        valueTex?.freeRequireUnshared()
        thumbnailTex?.freeRequireUnshared()
    }

    fun attach(
        gel: DlgItemGel,
        textTex: Texture, // ownership is passed to this class
        thumbnailTex: Texture?, // dito
        switchFrames: FrameCollection?, // contained texture assumed to be shared, ownership NOT passed
        incDecFrames: FrameCollection?, // dito
    ) {
        require(this.gel == null) { "Already attached to some gel" }
        this.gel = gel

        val btnWidth = gel.width
        val btnHeight = gel.height

        val bgTex = ButtonTextureFactory.makeBgTexture(btnWidth, btnHeight)
        val otlTex = ButtonTextureFactory.makeOtlTexture(btnWidth, btnHeight)

        this.textTex = textTex
        this.thumbnailTex = thumbnailTex
        this.bgTex = bgTex
        this.otlTex = otlTex

        bgRenderer = App.factory.createUISpriteRenderer(
            object: UISpriteRenderer.Delegate() {
                override val renderPos = gel.pos // shared mutable object
                override val tex = bgTex
                override val frameSize = MutableVector2f(tex.width, tex.height)
                override val opacity get() = gel.opacity

                override val brightness: Float
                    get() = when {
                        gel.selected -> (0.8f + 0.8f * max(
                            0.0f,
                            1.0f - (App.time.sessionTime - gel.selectTime) / DlgItemGel.SELECT_DELAY_SECONDS
                        ))
                        gel.hilited -> 1.15f + 0.05f * gel.brightness
                        else -> 1.0f
                    }
            }
        )

        otlRenderer = App.factory.createUISpriteRenderer(
            object: UISpriteRenderer.Delegate() {
                override val renderPos = gel.pos // shared mutable object
                override val tex = otlTex
                override val frameSize = MutableVector2f(tex.width, tex.height)
                override val opacity get() = gel.opacity

                override val brightness
                    get() = when {
                        !gel.hilited -> 0.1f
                        !gel.selected -> 0.7f + 0.5f * gel.brightness
                        else -> 1.0f
                    }
            }
        )

        textRenderer = run {
            val xoffset = when {
                thumbnailTex != null -> thumbnailTex.width + 2 * ITEM_TEXT_OUTER_PADDING
                else -> ITEM_TEXT_OUTER_PADDING
            }

            val textPos = MutableVector3f()

            return@run App.factory.createUISpriteRenderer(
                object: UISpriteRenderer.Delegate() {
                    override val renderPos
                        get() = gel.pos.let {
                            textPos.set(it.x + xoffset, it.y + ITEM_TEXT_OUTER_PADDING, it.z)
                        }
                    override val tex = textTex
                    override val frameSize = MutableVector2f(tex.width, tex.height)
                    override val opacity get() = gel.opacity
                    override val brightness get() = if (gel.hilited) 1.0f else 0.8f
                }
            )
        }

        switchRenderer = switchFrames?.let { frames ->
            val valueTex = frames.tex
            val valuePos = MutableVector3f() // bound variable
            val left = bgTex.width.toFloat() - ITEM_TEXT_OUTER_PADDING - ITEM_VALUE_MAX_WIDTH

            // Using floor is important here, because a sub-pixel offset would lead to artefacts
            val xOffset = floor(left + ITEM_VALUE_MAX_WIDTH / 2 - frames.frameSize.x / 2)
            val yOffset = floor(bgTex.height / 2 - frames.frameSize.y / 2)

            return@let App.factory.createUISpriteRenderer(
                object: UISpriteRenderer.Delegate() {
                    override val renderPos
                        get() = gel.pos.let {
                            valuePos.set(it.x + xOffset, it.y + yOffset, it.z)
                        }
                    override val tex = valueTex
                    override val frameSize = frames.frameSize // shared mutable object
                    override val texOffset = frames.texOffset // shared mutable object
                    override val opacity get() = gel.opacity
                    override val brightness get() = if (gel.hilited) 1.0f else 0.8f
                }
            )
        }

        valueTex = if (incDecFrames == null) null else makeStringValueTexture(gel.curValueAsString)
        valueFrameSize = Vector2f(valueTex?.width ?: 0, valueTex?.height ?: 0)

        valueRenderer = incDecFrames?.let { frames ->
            val incDecWidth = frames.frameSize.x
            val valuePos = MutableVector3f() // bound variable
            val left = bgTex.width - ITEM_TEXT_OUTER_PADDING - incDecWidth - ITEM_VALUE_MAX_WIDTH

            // Using floor is important here, because a sub-pixel offset would lead to artefacts
            val xOffset = floor(left + ITEM_VALUE_MAX_WIDTH / 2 - valueFrameSize.x / 2)
            val yOffset = floor(ITEM_TEXT_OUTER_PADDING.toFloat())

            return@let App.factory.createUISpriteRenderer(
                object: UISpriteRenderer.Delegate() {
                    override val renderPos
                        get() = gel.pos.let {
                            valuePos.set(it.x + xOffset, it.y + yOffset, it.z)
                        }
                    override val tex = valueTex
                    override val frameSize = valueFrameSize
                    override val opacity get() = gel.opacity
                    override val brightness get() = if (gel.hilited) 1.0f else 0.8f
                }
            )
        }

        incRenderer = incDecFrames?.let { frames ->
            frames.setFrame(1)
            val texOffset = Vector2f(incDecFrames.texOffset)

            val p = MutableVector3f() // bound variable
            val xOffset = bgTex.width.toFloat() - ITEM_TEXT_OUTER_PADDING - frames.frameSize.x

            return@let App.factory.createUISpriteRenderer(
                object: UISpriteRenderer.Delegate() {
                    override val renderPos
                        get() = gel.pos.let {
                            p.set(it.x + xOffset, it.y + ITEM_INCDEC_MARGIN_TOP, it.z)
                        }
                    override val tex = frames.tex
                    override val frameSize = frames.frameSize // shared mutable object
                    override val texOffset = texOffset

                    override val opacity
                        get() = when (gel.hilited) {
                            true -> gel.opacity * when {
                                gel.canIncrement -> 1.0f
                                else -> 0.42f
                            }
                            false -> 0.0f
                        }
                }
            )
        }

        decRenderer = incDecFrames?.let { frames ->
            frames.setFrame(0)
            val texOffset = Vector2f(incDecFrames.texOffset)

            val p = MutableVector3f() // bound variable
            val xOffset =
                bgTex.width.toFloat() - ITEM_TEXT_OUTER_PADDING - ITEM_VALUE_MAX_WIDTH - 2 * frames.frameSize.x

            return@let App.factory.createUISpriteRenderer(
                object: UISpriteRenderer.Delegate() {
                    override val renderPos
                        get() = gel.pos.let {
                            p.set(it.x + xOffset, it.y + ITEM_INCDEC_MARGIN_TOP, it.z)
                        }
                    override val tex = frames.tex
                    override val frameSize = frames.frameSize // shared mutable object
                    override val texOffset = texOffset

                    override val opacity
                        get() = when (gel.hilited) {
                            true -> gel.opacity * when {
                                gel.canDecrement -> 1.0f
                                else -> 0.42f
                            }
                            false -> 0.0f
                        }
                }
            )
        }

        thumbnailRenderer = if (thumbnailTex == null) null else run {
            val thumbnailPos = MutableVector3f()
            return@run App.factory.createUISpriteRenderer(
                object: UISpriteRenderer.Delegate() {
                    override val renderPos
                        get() = thumbnailPos.set(
                            gel.pos.x + ITEM_TEXT_OUTER_PADDING,
                            gel.pos.y + ITEM_TEXT_OUTER_PADDING,
                            gel.pos.z
                        )
                    override val tex = thumbnailTex
                    override val frameSize = MutableVector2f(SAVEGAME_THUMBNAIL_WIDTH, SAVEGAME_THUMBNAIL_HEIGHT)
                    override val opacity get() = gel.opacity
                }
            )
        }
    }
}
