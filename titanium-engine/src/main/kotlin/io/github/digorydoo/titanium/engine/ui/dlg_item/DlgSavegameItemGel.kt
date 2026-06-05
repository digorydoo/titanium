package io.github.digorydoo.titanium.engine.ui.dlg_item

import ch.digorydoo.kutils.colour.Colour
import ch.digorydoo.kutils.logging.Log
import ch.digorydoo.kutils.vector.MutableVector2f
import ch.digorydoo.kutils.vector.MutableVector3f
import io.github.digorydoo.titanium.engine.behaviours.Align
import io.github.digorydoo.titanium.engine.behaviours.Glow
import io.github.digorydoo.titanium.engine.core.App
import io.github.digorydoo.titanium.engine.file.SaveGameFileWriter
import io.github.digorydoo.titanium.engine.gel.GraphicElement
import io.github.digorydoo.titanium.engine.shader.Renderer
import io.github.digorydoo.titanium.engine.sound.EngineSampleId
import io.github.digorydoo.titanium.engine.sprite.UISpriteRenderer
import io.github.digorydoo.titanium.engine.texture.Texture
import io.github.digorydoo.titanium.engine.ui.ITEM_TEXT_OUTER_PADDING
import io.github.digorydoo.titanium.engine.ui.SAVEGAME_THUMBNAIL_HEIGHT
import io.github.digorydoo.titanium.engine.ui.SAVEGAME_THUMBNAIL_WIDTH
import io.github.digorydoo.titanium.engine.ui.button.BtnAlignDelegate
import io.github.digorydoo.titanium.engine.ui.button.ButtonRendererFactory
import io.github.digorydoo.titanium.engine.ui.button.ButtonTextureFactory
import io.github.digorydoo.titanium.engine.ui.dialogue.DlgSavegameItemDef
import io.github.digorydoo.titanium.engine.ui.dlg_item.DlgItemGel.Companion.FADE_DELAY_SECONDS
import io.github.digorydoo.titanium.engine.ui.dlg_item.DlgItemGel.Companion.SELECT_DELAY_SECONDS
import kotlin.math.max
import kotlin.math.pow

/**
 * Performance note: Loading the list of files and creating the entire LoadGameMenu with 10 savegames including loading
 * their thumbnails and initializing all DlgSavegameItemGels takes only about 50ms on Mac Mini. So, loading the files
 * concurrently would not help much. Still, the list takes about 250ms on the first render cycle, creating a noticable
 * lag. The culprit appears to be GL: UISpriteRendererImpl's first call to drawArray takes about 5ms, and since this
 * gel has four parts, the gel's first render takes about 20ms. Can this be improved?
 */
class DlgSavegameItemGel(
    override val def: DlgSavegameItemDef,
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
    private val thumbnailTex = def.summary.let { makeSavegameImageTexture(it) }

    private var _hilited = false

    override var hilited: Boolean
        get() = _hilited
        set(b) {
            _hilited = b
            if (b) glow.reset(0.25f)
        }

    override val autoDismiss = true
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
        thumbnailTex.freeRequireUnshared()
    }

    override fun toString() = "DlgSavegameItemGel($def)"

    companion object {
        private val TAG = Log.Tag("DlgSavegameItemGel")

        private fun makeSavegameImageTexture(summary: SaveGameFileWriter.Summary) =
            App.textures.createTexture(SAVEGAME_THUMBNAIL_WIDTH, SAVEGAME_THUMBNAIL_HEIGHT).apply {
                drawInto {
                    val src = summary.screenshot
                    if (src == null) {
                        Log.warn(TAG, "Drawing an empty thumbnail, because summary.screenshot is null")
                        clear(Colour.black)
                    } else if (src.width != SAVEGAME_THUMBNAIL_WIDTH || src.height != SAVEGAME_THUMBNAIL_HEIGHT) {
                        Log.warn(TAG, "Thumbnail does not have the expected size: ${src.width}x${src.height}")
                        clear(Colour.black)
                    } else {
                        // We can't help redrawing the image here, because the thumbnail is RGB8, but we need RGBA8.
                        drawImage(src, 0, 0)
                    }
                }
            }

        private fun makeCombinedRenderer(gel: DlgSavegameItemGel): Renderer {
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

            val xoffset = gel.thumbnailTex.width + 2 * ITEM_TEXT_OUTER_PADDING
            val text = ButtonRendererFactory.makeTextRenderer(delegate, gel.textTex, xoffset)

            val thumbnail = makeThumbnailRenderer(gel)

            return object: Renderer {
                override fun renderShadows() {}
                override fun renderSolid() {}

                override fun renderTransparent() {
                    bg.renderTransparent()
                    otl.renderTransparent()
                    text.renderTransparent()
                    thumbnail.renderTransparent()
                }

                override fun free() {
                    bg.free()
                    otl.free()
                    text.free()
                    thumbnail.free()
                }
            }
        }

        private fun makeThumbnailRenderer(gel: DlgSavegameItemGel): Renderer {
            val imageTex = gel.thumbnailTex
            val imagePos = MutableVector3f() // bound variable
            return App.factory.createUISpriteRenderer(
                object: UISpriteRenderer.Delegate() {
                    override val renderPos
                        get() = imagePos.set(
                            gel.pos.x + ITEM_TEXT_OUTER_PADDING,
                            gel.pos.y + ITEM_TEXT_OUTER_PADDING,
                            gel.pos.z
                        )
                    override val tex = imageTex
                    override val frameSize = MutableVector2f(SAVEGAME_THUMBNAIL_WIDTH, SAVEGAME_THUMBNAIL_HEIGHT)
                    override val opacity get() = gel.opacity
                }
            )
        }
    }
}
