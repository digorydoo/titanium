package ch.digorydoo.titanium.game.gel.test

import ch.digorydoo.kutils.point.MutablePoint2f
import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.titanium.engine.brick.BrickMaterial
import ch.digorydoo.titanium.engine.brick.BrickShape
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.gel.GraphicElement
import ch.digorydoo.titanium.engine.physics.helper.HitArea
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedCylinderBody
import ch.digorydoo.titanium.engine.shader.PaperRenderer
import ch.digorydoo.titanium.engine.texture.FrameCollection
import ch.digorydoo.titanium.engine.utils.EPSILON
import kotlin.math.atan2
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
    private var hasGroundContact = true
    private var didChangeDir = false

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

    override fun didCollide(
        other: GraphicElement,
        myHit: HitArea,
        otherHit: HitArea,
        hitPt: Point3f,
        normalTowardsMe: Point3f,
    ) {
        if (myHit == HitArea.TOP) {
            // print("t")
        } else if (otherHit == HitArea.TOP) {
            // print("T")
            didCollideWithFloor = true
            return
        } else if (myHit == HitArea.BOTTOM && otherHit == HitArea.UNSPECIFIED) {
            // Typically a sphere. Add some random instability force.
            // print("U")
            jumpInFrames = JUMP_IN_FRAMES_MIN // don't jump as long as this happens
            val a = Random.nextFloat() * Math.PI * 2.0f
            val fx = INSTABILITY_FORCE * cos(a).toFloat()
            val fy = INSTABILITY_FORCE * sin(a).toFloat()
            body.addForce(fx * body.mass, fy * body.mass, INSTABILITY_FORCE * body.mass)
            other.body?.let {
                it.addForce(-fx * it.mass, -fy * it.mass, 0.0f)
            }
        } else {
            // print("g")
        }

        changeDirection()
        val ob = other.body

        if (other !is TestGel && ob != null && ob.resultingForce.maxAbsComponent() < 0.1f) {
            // Give the other body a slight push.
            val dx = other.pos.x - pos.x
            val dy = other.pos.y - pos.y
            val dist2D = sqrt(dx * dx + dy * dy)

            if (dist2D > EPSILON) {
                ob.addForce(PUSHING_FORCE_XY * dx / dist2D, PUSHING_FORCE_XY * dy / dist2D, 0.0f)
            }

            if (ob.nextSpeed.z <= 0.0f) {
                ob.addForce(0.0f, 0.0f, PUSHING_FORCE_Z)
            }
        }
    }

    override fun didCollide(
        shape: BrickShape,
        material: BrickMaterial,
        myHit: HitArea,
        otherHit: HitArea,
        hitPt: Point3f,
        normalTowardsMe: Point3f,
    ) {
        when (otherHit) {
            HitArea.NORTH_FACE,
            HitArea.EAST_FACE,
            HitArea.SOUTH_FACE,
            HitArea.WEST_FACE,
            -> {
                changeDirection(normalTowardsMe)
                // print("b")
            }
            HitArea.TOP -> {
                didCollideWithFloor = true
            }
            else -> Unit
        }
    }

    override fun onAnimateActive() {
        if (didChangeDir) {
            // print("-")
            didChangeDir = false
        }

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

        if (--jumpInFrames <= 0) {
            jumpInFrames = JUMP_IN_FRAMES_MIN + (Random.nextFloat() * JUMP_IN_FRAMES_RANGE).toInt()
            body.addForce(0.0f, 0.0f, JUMPING_FORCE)
        }
    }

    private fun changeDirection() {
        val a = (Random.nextFloat() * Math.PI * 2.0).toFloat()
        movingForce.x = MOVING_FORCE * body.mass * cos(a)
        movingForce.y = MOVING_FORCE * body.mass * sin(a)
        rotationSpeed = 0.05f * (1.0f - 2.0f * Random.nextFloat())
        didChangeDir = true
    }

    private fun changeDirection(normalTowardsMe: Point3f) {
        val rnd = 0.42f * (1.0f - 2.0f * Random.nextFloat())
        val a = atan2(normalTowardsMe.y, normalTowardsMe.x) + rnd
        movingForce.x = MOVING_FORCE * body.mass * cos(a)
        movingForce.y = MOVING_FORCE * body.mass * sin(a)
        rotationSpeed = 0.05f * (1.0f - 2.0f * Random.nextFloat())
        didChangeDir = true
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
        private const val MOVING_FORCE = 10.0f
        private const val PUSHING_FORCE_XY = 48.0f
        private const val PUSHING_FORCE_Z = 105.0f
        private const val JUMPING_FORCE = 9200.0f
        private const val JUMP_IN_FRAMES_MIN = 210
        private const val JUMP_IN_FRAMES_RANGE = 680
        private const val INSTABILITY_FORCE = 11.0f
    }
}
