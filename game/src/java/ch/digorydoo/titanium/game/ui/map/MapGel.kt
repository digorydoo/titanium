package ch.digorydoo.titanium.game.ui.map

import ch.digorydoo.kutils.point.MutablePoint2f
import ch.digorydoo.kutils.point.Point2f
import ch.digorydoo.titanium.engine.behaviours.CreateConcurrently
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.gel.GraphicElement
import ch.digorydoo.titanium.engine.sprite.UISpriteRenderer
import ch.digorydoo.titanium.engine.texture.ImageData
import ch.digorydoo.titanium.engine.texture.Texture
import kotlin.reflect.KClass

class MapGel(val page: MapPage): GraphicElement() {
    init {
        inDialog = Visibility.FROZEN_VISIBLE
        inMenu = Visibility.ACTIVE
        inEditor = Visibility.ACTIVE
    }

    private var tex: Texture? = null
    private val frameSize = Point2f(TEX_WIDTH, TEX_HEIGHT)
    private val scaleFactor = MutablePoint2f()

    override val renderer = App.factory.createUISpriteRenderer(
        object: UISpriteRenderer.Delegate() {
            override val frameSize = this@MapGel.frameSize
            override val renderPos = this@MapGel.pos // shared mutable object
            override val tex get() = this@MapGel.tex
            override val scaleFactor = this@MapGel.scaleFactor
        },
        antiAliasing = true
    )

    private val createConcurrently = CreateConcurrently(
        this,
        object: CreateConcurrently.Delegate {
            private lateinit var tmpImg: ImageData

            override suspend fun onJobStart() {
                // Do not modify gel here! Store everything in temporary variables!
                tmpImg = App.textures.getOrLoadImageDataAsync("map-town-01.png")
            }

            override fun onJobDone() {
                // Back in main thread
                tex = App.textures.getOrCreateTexture(tmpImg).also {
                    require(it.width == TEX_WIDTH)
                    require(it.height == TEX_HEIGHT)
                }
            }
        }
    )

    override fun getBehaviour(klass: KClass<*>) = when (klass) {
        CreateConcurrently::class -> createConcurrently
        else -> null
    }

    fun show() {
        setHiddenOnNextFrameTo = false
        page.mapScaleFactor.let { scaleFactor.set(it, it) }
    }

    fun hide() {
        setHiddenOnNextFrameTo = true
    }

    override fun onRemoveZombie() {
        renderer.free()
    }

    companion object {
        const val TEX_WIDTH = 745
        const val TEX_HEIGHT = 589
    }
}
