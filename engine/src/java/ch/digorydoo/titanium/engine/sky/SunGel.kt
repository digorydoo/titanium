package ch.digorydoo.titanium.engine.sky

import ch.digorydoo.kutils.point.MutablePoint2f
import ch.digorydoo.kutils.point.Point2f
import ch.digorydoo.titanium.engine.behaviours.TurnTowardsCamera
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.gel.GraphicElement
import ch.digorydoo.titanium.engine.shader.PaperRenderer
import ch.digorydoo.titanium.engine.shader.Renderer.BlendMode

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

    private val tex = App.textures.getOrCreateTexture("sky-sun.png")
    private val frameSize = MutablePoint2f(tex?.width ?: 0, tex?.height ?: 0)

    private val renderProps = object: PaperRenderer.Delegate() {
        override val renderPos get() = this@SunGel.pos
        override val frameSize get() = this@SunGel.frameSize
        override val tex get() = this@SunGel.tex
        override val rotationPhi get() = turnProps.rotationPhi
        override val rotationRho get() = turnProps.rotationRho
        override val origin = Point2f(this@SunGel.frameSize.x / 2.0f, this@SunGel.frameSize.y / 2.0f)
        override val opacity get() = App.scene.lighting.sunOpacity
        override val scaleFactor = MutablePoint2f(SCALE_FACTOR, SCALE_FACTOR)
        override val multColour get() = App.scene.lighting.sunColour
    }

    override val renderer = App.factory.createPaperRenderer(
        renderProps,
        antiAliasing = true,
        blendMode = BlendMode.ADD,
        depthTest = true,
        stellarObject = true
    )

    private val move = object: Behaviour {
        override fun animate() {
            pos.set(App.camera.sourcePos)
            val dir = App.scene.lighting.sunDir.vector
            pos.add(
                dir.x * SUN_DISTANCE,
                dir.y * SUN_DISTANCE,
                dir.z * SUN_DISTANCE,
            )
        }
    }

    override fun onAnimateActive() {
        move.animate()
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
