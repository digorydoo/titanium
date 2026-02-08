package io.github.digorydoo.titanium.engine.sky

import ch.digorydoo.kutils.vector.MutableVector2f
import ch.digorydoo.kutils.vector.Vector2f
import io.github.digorydoo.titanium.engine.behaviours.CreateConcurrently
import io.github.digorydoo.titanium.engine.behaviours.TurnTowardsCamera
import io.github.digorydoo.titanium.engine.core.App
import io.github.digorydoo.titanium.engine.gel.GraphicElement
import io.github.digorydoo.titanium.engine.shader.PaperRenderer
import io.github.digorydoo.titanium.engine.shader.Renderer.BlendMode
import io.github.digorydoo.titanium.engine.texture.ImageData
import io.github.digorydoo.titanium.engine.texture.Texture
import kotlin.reflect.KClass

class SunGel: GraphicElement() {
    init {
        inDialog = Visibility.ACTIVE
        inMenu = Visibility.INVISIBLE
        inEditor = Visibility.ACTIVE
        allowNegativeZ = true
    }

    private val turnProps = object: TurnTowardsCamera.Delegate() {
        override var rotationPhi = 0.0f
        override var rotationRho = 0.0f
    }

    private val turn = TurnTowardsCamera(turnProps, keepUpright = false)

    private var tex: Texture? = null
    private val frameSize = MutableVector2f()

    private val renderProps = object: PaperRenderer.Delegate() {
        override val renderPos get() = this@SunGel.pos
        override val frameSize get() = this@SunGel.frameSize
        override val tex get() = this@SunGel.tex
        override val rotationPhi get() = turnProps.rotationPhi
        override val rotationRho get() = turnProps.rotationRho
        override val origin = Vector2f(this@SunGel.frameSize.x / 2.0f, this@SunGel.frameSize.y / 2.0f)
        override val opacity get() = App.scene.lighting.sunOpacity
        override val scaleFactor = MutableVector2f(SCALE_FACTOR, SCALE_FACTOR)
        override val multColour get() = App.scene.lighting.sunColour
    }

    override val renderer = App.factory.createPaperRenderer(
        renderProps,
        antiAliasing = true,
        blendMode = BlendMode.ADD,
        depthTest = true,
        stellarObject = true
    )

    private val createConcurrently = CreateConcurrently(
        this,
        object: CreateConcurrently.Delegate {
            private lateinit var tmpImg: ImageData

            override suspend fun onJobStart() {
                // Do not modify gel here! Store everything in temporary variables!
                tmpImg = App.textures.getOrLoadImageDataAsync("sky-sun.png")
            }

            override fun onJobDone() {
                // Back in the main thread
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

    override fun onAnimateActive() {
        val cam = App.camera.sourcePos
        val dir = App.scene.lighting.sunDir.vector

        moveTo(
            cam.x + dir.x * SUN_DISTANCE,
            cam.y + dir.y * SUN_DISTANCE,
            cam.z + dir.z * SUN_DISTANCE,
        )

        turn.animate()
    }

    override fun onRemoveZombie() {
        renderer.free()
    }

    companion object {
        private const val SCALE_FACTOR = 1.0f / 32.0f
        private const val SUN_DISTANCE = 10.0f // not too far, otherwise it gets clipped
    }
}
