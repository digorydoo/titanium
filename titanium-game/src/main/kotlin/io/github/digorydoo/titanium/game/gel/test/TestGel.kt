package io.github.digorydoo.titanium.game.gel.test

import ch.digorydoo.kutils.logging.Log
import ch.digorydoo.kutils.vector.MutableVector2f
import ch.digorydoo.kutils.vector.Vector3f
import io.github.digorydoo.titanium.engine.behaviours.CreateConcurrently
import io.github.digorydoo.titanium.engine.brick.BrickMaterial
import io.github.digorydoo.titanium.engine.brick.BrickShape
import io.github.digorydoo.titanium.engine.core.App
import io.github.digorydoo.titanium.engine.gel.GraphicElement
import io.github.digorydoo.titanium.engine.physics.HitArea
import io.github.digorydoo.titanium.engine.physics.rigid_body.FixedCapsuleBody
import io.github.digorydoo.titanium.engine.physics.rigid_body.FixedCylinderBody
import io.github.digorydoo.titanium.engine.physics.rigid_body.FixedSphereBody
import io.github.digorydoo.titanium.engine.shader.PaperRenderer
import io.github.digorydoo.titanium.engine.sprite.FrameCollection
import io.github.digorydoo.titanium.engine.texture.ImageData
import io.github.digorydoo.titanium.engine.utils.EPSILON
import io.github.digorydoo.titanium.engine.utils.SmoothAngle
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random
import kotlin.reflect.KClass

class TestGel(override val spawnPt: TestSpawnPt): GraphicElement(spawnPt) {
    init {
        bodyPosOffset.set(0.0f, 0.0f, BODY_HEIGHT / 2.0f)
        inDialog = Visibility.ACTIVE
        inMenu = Visibility.INVISIBLE
        inEditor = Visibility.FROZEN_VISIBLE
    }

    override val body = FixedCylinderBody(
        "Test",
        initialPos = pos + bodyPosOffset,
        elasticity = 0.42f,
        friction = 0.1f,
        mass = 20.0f,
        gravity = true,
        radius = BODY_HEIGHT / 2.0f - 0.05f,
        height = BODY_HEIGHT,
    )

    private val frames = FrameCollection()
    private val frameOrigin = MutableVector2f()
    private val rotationPhi = SmoothAngle(initVal = spawnPt.rotation)
    private var jumpInFrames = JUMP_IN_FRAMES_MIN + (Random.nextFloat() * JUMP_IN_FRAMES_RANGE).toInt()
    private var didCollideWithFloor = true
    private var hasGroundContact = true
    private var timeOfChangeDir = 0.0f

    private val renderProps = object: PaperRenderer.Delegate() {
        override val renderPos = this@TestGel.pos
        override val frameSize get() = frames.frameSize
        override val tex get() = frames.tex
        override val texOffset get() = frames.texOffset
        override val origin get() = frameOrigin
        override val rotationPhi get() = this@TestGel.rotationPhi.current
        override val rotationRho = 0.0f
        override val scaleFactor = MutableVector2f(1.0f / 36, 1.0f / 36)
    }

    override val renderer = App.factory.createPaperRenderer(renderProps)

    private val createConcurrently = CreateConcurrently(
        this,
        object: CreateConcurrently.Delegate {
            private lateinit var tmpImg: ImageData

            override suspend fun onJobStart() {
                // Do not modify gel here! Store everything in temporary variables!
                tmpImg = App.textures.getOrLoadImageDataAsync("test32x32.png")
            }

            override fun onJobDone() {
                // Back in main thread
                frames.setTexture(tmpImg)
                frameOrigin.set(renderProps.frameSize.x / 2, renderProps.frameSize.y)
                changeDirection()
            }
        }
    )

    override fun getBehaviour(klass: KClass<*>) = when (klass) {
        CreateConcurrently::class -> createConcurrently
        else -> null
    }

    override fun onCollide(
        other: GraphicElement,
        myHit: HitArea,
        otherHit: HitArea,
        hitPt: Vector3f,
        normalTowardsMe: Vector3f,
    ) {
        when (myHit) {
            HitArea.TOP_FACE -> Unit
            HitArea.BOTTOM_FACE -> {
                val otherBody = other.body

                if (otherBody is FixedSphereBody || otherBody is FixedCapsuleBody) {
                    // This is an unstable configuration. Add some random instability force.
                    // print("U")
                    jumpInFrames = JUMP_IN_FRAMES_MIN // don't jump as long as this happens
                    val a = Random.nextFloat() * PI * 2.0f
                    val fx = INSTABILITY_FORCE * cos(a).toFloat()
                    val fy = INSTABILITY_FORCE * sin(a).toFloat()
                    body.addForce(fx * body.mass, fy * body.mass, INSTABILITY_FORCE * body.mass)
                    other.body?.let {
                        it.addForce(-fx * it.mass, -fy * it.mass, 0.0f)
                    }
                } else {
                    // print("u")
                    didCollideWithFloor = true
                }
                return
            }
            HitArea.SIDE -> Unit
            else -> Log.error(TAG, "Unexpected value of myHit ($myHit) while colliding with $other")
        }

        if (App.time.sessionTime - timeOfChangeDir > MIN_DELAY_BETWEEN_CHANGE_DIR) {
            changeDirection()
        }

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

    override fun onCollide(
        shape: BrickShape,
        material: BrickMaterial,
        myHit: HitArea,
        otherHit: HitArea,
        hitPt: Vector3f,
        normalTowardsMe: Vector3f,
    ) {
        when (myHit) {
            HitArea.TOP_FACE -> Unit
            HitArea.BOTTOM_FACE -> {
                if (otherHit == HitArea.TOP_FACE || otherHit == HitArea.TOP_TIP) {
                    didCollideWithFloor = true
                }
            }
            HitArea.SIDE -> {
                if (App.time.sessionTime - timeOfChangeDir > MIN_DELAY_BETWEEN_CHANGE_DIR) {
                    changeDirection(normalTowardsMe)
                }
            }
            else -> Log.error(TAG, "Unexpected value of myHit: $myHit while colliding with brick $shape")
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
            rotationPhi.animate()

            val phi = rotationPhi.current
            val forceX = MOVING_FORCE * body.mass * cos(phi)
            val forceY = MOVING_FORCE * body.mass * sin(phi)
            val speed = body.speed.length()

            if (speed <= MIN_SPEED_BEFORE_LESS_ACCEL) {
                // Full acceleration
                body.addForce(forceX, forceY, 0.0f)
            } else {
                // FIXME gel should be able to change direction at max speed
                val relSpeed = (speed - MIN_SPEED_BEFORE_LESS_ACCEL) / (MAX_SPEED - MIN_SPEED_BEFORE_LESS_ACCEL)

                if (relSpeed < 1.0f) {
                    val inv = 1.0f - relSpeed
                    body.addForce(inv * forceX, inv * forceY, 0.0f)
                }
            }
        }

        if (--jumpInFrames <= 0) {
            jumpInFrames = JUMP_IN_FRAMES_MIN + (Random.nextFloat() * JUMP_IN_FRAMES_RANGE).toInt()
            body.addForce(0.0f, 0.0f, JUMPING_FORCE)
        }
    }

    private fun changeDirection() {
        val a = (Random.nextFloat() * PI * 2.0).toFloat()
        rotationPhi.desired = a
        timeOfChangeDir = App.time.sessionTime
    }

    private fun changeDirection(normalTowardsMe: Vector3f) {
        val rnd = 0.72f * (1.0f - 2.0f * Random.nextFloat())
        val a = atan2(normalTowardsMe.y, normalTowardsMe.x) + rnd
        rotationPhi.desired = a
        timeOfChangeDir = App.time.sessionTime
    }

    override fun onRemoveZombie() {
        renderer.free()
    }

    override fun toString() = "TestGel(${spawnPt.id})"

    companion object {
        private val TAG = Log.Tag("TestGel")
        private const val BODY_HEIGHT = 1.0f
        private const val MOVING_FORCE = 10.0f
        private const val PUSHING_FORCE_XY = 48.0f
        private const val PUSHING_FORCE_Z = 105.0f
        private const val JUMPING_FORCE = 9200.0f
        private const val JUMP_IN_FRAMES_MIN = 210
        private const val JUMP_IN_FRAMES_RANGE = 680
        private const val INSTABILITY_FORCE = 23.0f
        private const val MIN_SPEED_BEFORE_LESS_ACCEL = 10.0f
        private const val MAX_SPEED = 18.0f
        private const val MIN_DELAY_BETWEEN_CHANGE_DIR = 0.5f // seconds
    }
}
