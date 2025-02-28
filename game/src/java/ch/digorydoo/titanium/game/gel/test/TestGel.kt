package ch.digorydoo.titanium.game.gel.test

import ch.digorydoo.kutils.point.MutablePoint2f
import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.titanium.engine.brick.Brick
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.gel.GraphicElement
import ch.digorydoo.titanium.engine.physics.FixedCylinderBody
import ch.digorydoo.titanium.engine.shader.PaperRenderer
import ch.digorydoo.titanium.engine.texture.FrameCollection
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class TestGel(initialPos: Point3f, initialRotation: Float): GraphicElement(initialPos) {
    override val inDialog = Visibility.ACTIVE
    override val inMenu = Visibility.INVISIBLE
    override val inEditor = Visibility.ACTIVE

    override val body = FixedCylinderBody(
        "Test",
        pos, // shared mutable object
        elasticity = 0.42f,
        friction = 0.5f,
        mass = 4.2f,
        gravity = true,
        radius = 0.5f,
        zOffset = 0.5f,
        height = 1.0f,
    )

    private val frames = FrameCollection()
    private val frameOrigin = MutablePoint2f()
    private val movingForce = MutablePoint2f()
    private var rotationSpeed = 0.0f

    private val renderProps = object: PaperRenderer.Delegate() {
        override val renderPos get() = this@TestGel.pos
        override val frameSize get() = frames.frameSize
        override val tex get() = frames.tex
        override val texOffset get() = frames.texOffset
        override val origin get() = frameOrigin
        override var rotationPhi = initialRotation
        override val rotationRho = 0.0f
        override val scaleFactor = MutablePoint2f(1.0f / 36, 1.0f / 36)
    }

    override val renderer = App.factory.createPaperRenderer(renderProps)

    override fun didCollide(other: GraphicElement, hitPt: Point3f): Boolean {
        // println("TestGel collided with $other")
        changeDirection()
        return true // true = bounce
    }

    override fun didCollide(brick: Brick, hitPt: Point3f, hitNormal: Point3f) {
        if (abs(hitNormal.x) + abs(hitNormal.y) > abs(hitNormal.z)) {
            // println("TestGel collided sideways, hitPt=$hitPt")
            changeDirection()
        }
    }

    private fun changeDirection() {
        val a = Random.nextFloat() * Math.PI * 2.0f
        movingForce.x = 1.0f * body.mass * cos(a).toFloat()
        movingForce.y = 1.0f * body.mass * sin(a).toFloat()
        rotationSpeed = 0.05f * (1.0f - 2.0f * Random.nextFloat())
    }

    override fun onAnimateActive() {
        renderProps.rotationPhi += rotationSpeed
        body.force.x += movingForce.x
        body.force.y += movingForce.y
    }

    init {
        frames.setTexture("test32x32.png")
        frameOrigin.set(renderProps.frameSize.x / 2, renderProps.frameSize.y)

        changeDirection()
    }

    override fun onRemoveZombie() {
        renderer.free()
    }
}
