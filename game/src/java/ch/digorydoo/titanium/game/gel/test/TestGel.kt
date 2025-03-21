package ch.digorydoo.titanium.game.gel.test

import ch.digorydoo.kutils.point.MutablePoint2f
import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.titanium.engine.brick.Brick
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.gel.GraphicElement
import ch.digorydoo.titanium.engine.physics.HitArea
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedCylinderBody
import ch.digorydoo.titanium.engine.shader.PaperRenderer
import ch.digorydoo.titanium.engine.texture.FrameCollection
import ch.digorydoo.titanium.engine.utils.EPSILON
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

class TestGel(override val spawnPt: TestSpawnPt): GraphicElement(spawnPt) {
    init {
        bodyPosOffset.set(0.0f, 0.0f, BODY_HEIGHT / 2.0f)
        inDialog = Visibility.ACTIVE
        inMenu = Visibility.INVISIBLE
        inEditor = Visibility.ACTIVE
    }

    override val body = FixedCylinderBody(
        "Test",
        initialPos = pos + bodyPosOffset,
        elasticity = 0.42f,
        friction = 0.1f,
        mass = 20.0f,
        gravity = true,
        radius = 0.5f,
        height = BODY_HEIGHT,
    )

    private val frames = FrameCollection()
    private val frameOrigin = MutablePoint2f()
    private val movingForce = MutablePoint2f()
    private var rotationSpeed = 0.0f
    private var jumpInFrames: Int
    private var didCollideWithFloor = true
    private var speedOfTouchDown = 0.0f
    private var hasGroundContact = true

    private val renderProps = object: PaperRenderer.Delegate() {
        override val renderPos = this@TestGel.pos
        override val frameSize get() = frames.frameSize
        override val tex get() = frames.tex
        override val texOffset get() = frames.texOffset
        override val origin get() = frameOrigin
        override var rotationPhi = spawnPt.rotation
        override val rotationRho = 0.0f
        override val scaleFactor = MutablePoint2f(1.0f / 36, 1.0f / 36)
    }

    override val renderer = App.factory.createPaperRenderer(renderProps)

    override fun didCollide(other: GraphicElement, myHit: HitArea, otherHit: HitArea, hitPt: Point3f) {
        println("$this collided with $other")
        changeDirection()
        val ob = other.body

        if (other !is TestGel && ob != null) {
            // Give the other body a slight push in XY.
            val dx = other.pos.x - pos.x
            val dy = other.pos.y - pos.y
            val dist2D = sqrt(dx * dx + dy * dy)

            if (dist2D > EPSILON) {
                ob.addForce(PUSHING_FORCE * dx / dist2D, PUSHING_FORCE * dy / dist2D, 0.0f)
            }
        }
    }

    override fun didCollide(brick: Brick, hitPt: Point3f, hitNormal: Point3f) {
        if (abs(hitNormal.x) + abs(hitNormal.y) > abs(hitNormal.z)) {
            changeDirection()
        }

        if (hitNormal.z > 0.0f && body.nextSpeed.z < 0.0f) {
            didCollideWithFloor = true
            speedOfTouchDown = body.nextSpeed.z
        }
    }

    override fun onAnimateActive() {
        if (didCollideWithFloor) {
            hasGroundContact = true
            didCollideWithFloor = false // must be set again by didCollide
        } else {
            hasGroundContact = false
        }

        if (hasGroundContact) {
            renderProps.rotationPhi += rotationSpeed
            body.addForce(movingForce.x, movingForce.y, 0.0f)
        }

        // if (--jumpInFrames <= 0) {
        //     jumpInFrames = JUMP_IN_FRAMES_MIN + (Random.nextFloat() * JUMP_IN_FRAMES_RANGE).toInt()
        //     body.force.z += JUMP_FORCE
        // }
    }

    private fun changeDirection() {
        val a = Random.nextFloat() * Math.PI * 2.0f
        movingForce.x = MOVING_FORCE * body.mass * cos(a).toFloat()
        movingForce.y = MOVING_FORCE * body.mass * sin(a).toFloat()
        rotationSpeed = 0.05f * (1.0f - 2.0f * Random.nextFloat())
    }

    init {
        frames.setTexture("test32x32.png")
        frameOrigin.set(renderProps.frameSize.x / 2, renderProps.frameSize.y)

        changeDirection()
        jumpInFrames = JUMP_IN_FRAMES_MIN + (Random.nextFloat() * JUMP_IN_FRAMES_RANGE).toInt()
    }

    override fun onRemoveZombie() {
        renderer.free()
    }

    override fun toString() = "TestGel(${spawnPt.id})"

    companion object {
        private const val BODY_HEIGHT = 1.0f
        private const val MOVING_FORCE = 7.0f
        private const val PUSHING_FORCE = 42.0f
        private const val JUMPING_FORCE = 1200.0f
        private const val JUMP_IN_FRAMES_MIN = 120
        private const val JUMP_IN_FRAMES_RANGE = 420
    }
}
