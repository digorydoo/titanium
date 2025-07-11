package ch.digorydoo.titanium.game.ui.map

import ch.digorydoo.kutils.point.MutablePoint2f
import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.kutils.point.Point2f
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.gel.GraphicElement
import ch.digorydoo.titanium.engine.sprite.UISpriteRenderer
import ch.digorydoo.titanium.engine.texture.Texture
import kotlin.math.PI

class CurrentLocationGel(private val page: MapPage): GraphicElement() {
    init {
        inDialog = Visibility.FROZEN_VISIBLE
        inMenu = Visibility.ACTIVE
        inEditor = Visibility.ACTIVE
        callOnCreateConcurrently = true
    }

    private var tex: Texture? = null
    private val frameSize = MutablePoint2f()
    private val scaleFactor = Point2f(0.75f, 0.75f)
    private val renderPos = MutablePoint3f()
    private var rotationPhi = 0.0f

    override val renderer = App.factory.createUISpriteRenderer(
        object: UISpriteRenderer.Delegate() {
            override val frameSize = this@CurrentLocationGel.frameSize // shared mutable object
            override val renderPos
                get() = this@CurrentLocationGel.renderPos.apply {
                    set(pos.x - scaleFactor.x * frameSize.x / 2, pos.y - scaleFactor.y * frameSize.y / 2, pos.z)
                }
            override val tex get() = this@CurrentLocationGel.tex
            override val scaleFactor = this@CurrentLocationGel.scaleFactor
            override val rotation get() = this@CurrentLocationGel.rotationPhi
        },
        antiAliasing = true
    )

    override suspend fun onCreateConcurrently(): () -> Unit {
        val img = App.textures.getOrLoadImageDataAsync("ui-map-current-location.png")
        return {
            // Back in main thread
            tex = App.textures.getOrCreateTexture(img).also {
                frameSize.set(it.width, it.height)
            }
        }
    }

    fun show() {
        setHiddenOnNextFrameTo = false
        page.moveGelOnMap(this, App.player?.pos ?: App.camera.targetPos)
        rotationPhi = (App.player?.rotationPhi ?: 0.0f) + PI.toFloat()
    }

    fun hide() {
        setHiddenOnNextFrameTo = true
    }

    override fun onRemoveZombie() {
        renderer.free()
    }
}
