package ch.digorydoo.titanium.engine.ui

import ch.digorydoo.kutils.colour.Colour
import ch.digorydoo.kutils.point.MutablePoint2f
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.gel.GraphicElement
import ch.digorydoo.titanium.engine.texture.Texture

class UIAreaGel(
    bgTex: Texture? = null, // needs to be unshared; ownership is passed to gel
    bgColour: Colour = Colour.black, // ignored if bgTex is not null
    private val marginLeft: Int? = null,
    private val marginTop: Int? = null,
    private val marginRight: Int? = null,
    private val marginBottom: Int? = null,
    private val width: Int? = null,
    private val height: Int? = null,
    private val scaleTexToFrameSize: Boolean = false,
): GraphicElement() {
    init {
        inDialog = Visibility.ACTIVE
        inMenu = Visibility.ACTIVE
        inEditor = Visibility.ACTIVE
    }

    private val texture = when {
        bgTex != null -> bgTex // will be freed in aboutToRemove
        else -> makeTexture(bgColour)
    }

    private val frameSize = MutablePoint2f()
    private val texScaleFactor = MutablePoint2f(1.0f, 1.0f)

    override val renderer = App.factory.createUISpriteRenderer(
        object: UISpriteRenderer.Delegate() {
            override val tex = this@UIAreaGel.texture
            override val frameSize = this@UIAreaGel.frameSize
            override val renderPos = this@UIAreaGel.pos
            override val texScaleFactor = this@UIAreaGel.texScaleFactor
        },
        antiAliasing = true
    )

    private var prevScreenWidth = 0
    private var prevScreenHeight = 0

    private val setPosBehaviour: Behaviour = object: Behaviour {
        override fun animate() {
            val screenWidthDp = App.screenWidthDp
            val screenHeightDp = App.screenHeightDp

            if (screenWidthDp == prevScreenWidth && screenHeightDp == prevScreenHeight) {
                return // positions do not change
            }

            val left = when {
                marginLeft != null -> marginLeft
                width != null -> screenWidthDp - (marginRight ?: 0) - width
                else -> 0
            }

            val right = when {
                marginRight != null -> screenWidthDp - marginRight
                width != null -> (marginLeft ?: 0) + width
                else -> screenWidthDp
            }

            val top = when {
                marginTop != null -> marginTop
                height != null -> screenHeightDp - (marginBottom ?: 0) - height
                else -> 0
            }

            val bottom = when {
                marginBottom != null -> screenHeightDp - marginBottom
                height != null -> (marginTop ?: 0) + height
                else -> screenHeightDp
            }

            pos.set(left, top, 0)
            frameSize.set(right - left, bottom - top)

            prevScreenWidth = screenWidthDp
            prevScreenHeight = screenHeightDp

            if (scaleTexToFrameSize) {
                texScaleFactor.x = texture.width.toFloat() / frameSize.x
                texScaleFactor.y = texture.height.toFloat() / frameSize.y
            }
        }
    }

    override fun onAnimateActive() {
        setPosBehaviour.animate()
    }

    override fun onRemoveZombie() {
        renderer.free()
        texture.freeRequireUnshared()
    }

    companion object {
        fun makeTexture(bgColour: Colour) =
            App.textures.createTexture(2, 2).apply {
                drawInto { clear(bgColour) }
            }
    }
}
