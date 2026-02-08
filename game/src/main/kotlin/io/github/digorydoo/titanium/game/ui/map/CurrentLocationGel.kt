package io.github.digorydoo.titanium.game.ui.map

import ch.digorydoo.kutils.vector.MutableVector2f
import ch.digorydoo.kutils.vector.MutableVector3f
import ch.digorydoo.kutils.vector.Vector2f
import io.github.digorydoo.titanium.engine.behaviours.CreateConcurrently
import io.github.digorydoo.titanium.engine.core.App
import io.github.digorydoo.titanium.engine.gel.GraphicElement
import io.github.digorydoo.titanium.engine.sprite.UISpriteRenderer
import io.github.digorydoo.titanium.engine.texture.ImageData
import io.github.digorydoo.titanium.engine.texture.Texture
import kotlin.math.PI
import kotlin.reflect.KClass

class CurrentLocationGel(private val page: MapPage): GraphicElement() {
    init {
        inDialog = Visibility.FROZEN_VISIBLE
        inMenu = Visibility.ACTIVE
        inEditor = Visibility.ACTIVE
    }

    private var tex: Texture? = null
    private val frameSize = MutableVector2f()
    private val scaleFactor = Vector2f(0.75f, 0.75f)
    private val renderPos = MutableVector3f()
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

    private val createConcurrently = CreateConcurrently(
        this,
        object: CreateConcurrently.Delegate {
            private lateinit var tmpImg: ImageData

            override suspend fun onJobStart() {
                // Do not modify gel here! Store everything in temporary variables!
                tmpImg = App.textures.getOrLoadImageDataAsync("ui-map-current-location.png")
            }

            override fun onJobDone() {
                // Back in main thread
                tex = App.textures.getOrCreateTexture(tmpImg).also {
                    frameSize.set(it.width, it.height)
                }
            }
        }
    )

    override fun getBehaviour(klass: KClass<*>) = when (klass) {
        CreateConcurrently::class -> createConcurrently
        else -> null
    }

    override fun show() {
        super.show()
        page.moveGelOnMap(this, App.player?.pos ?: App.camera.targetPos)
        rotationPhi = (App.player?.rotationPhi ?: 0.0f) + PI.toFloat()
    }

    override fun onRemoveZombie() {
        renderer.free()
    }
}
