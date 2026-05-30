package io.github.digorydoo.titanium.game.player

import ch.digorydoo.kutils.vector.MutableVector2f
import ch.digorydoo.kutils.vector.Vector3f
import io.github.digorydoo.titanium.engine.behaviours.CreateConcurrently
import io.github.digorydoo.titanium.engine.behaviours.Send
import io.github.digorydoo.titanium.engine.behaviours.TurnTowardsCamera
import io.github.digorydoo.titanium.engine.brick.BrickMaterial
import io.github.digorydoo.titanium.engine.brick.BrickShape
import io.github.digorydoo.titanium.engine.core.App
import io.github.digorydoo.titanium.engine.gel.AbstrPlayerGel
import io.github.digorydoo.titanium.engine.gel.GraphicElement
import io.github.digorydoo.titanium.engine.physics.HitArea
import io.github.digorydoo.titanium.engine.physics.rigid_body.FixedCapsuleBody
import io.github.digorydoo.titanium.engine.shader.PaperRenderer
import io.github.digorydoo.titanium.engine.sprite.FrameCollection
import io.github.digorydoo.titanium.engine.texture.ImageData
import io.github.digorydoo.titanium.engine.utils.Direction
import kotlin.reflect.KClass

class PlayerGel(initialPos: Vector3f, initialRotationPhi: Float): AbstrPlayerGel(initialPos) {
    init {
        bodyPosOffset.set(0.0f, 0.0f, BODY_HEIGHT / 2.0f)
    }

    override val body = FixedCapsuleBody(
        "Player",
        initialPos = pos + bodyPosOffset,
        elasticity = 0.3f,
        friction = 0.999f,
        mass = 72.0f,
        gravity = true,
        radius = 0.25f,
        height = BODY_HEIGHT,
    )

    private val frames = FrameCollection()
    private val frameOrigin = MutableVector2f()
    private val frameScaleFactor = MutableVector2f()
    private val frameCycles = PlayerFrameCycles(frames)

    private val sendBehaviour = Send(
        this,
        object: Send.Delegate {
            override fun onReachTarget() {
                frameCycles.goIdle()
            }
        }
    )

    private val playerBehaviour = PlayerBehaviour(this, frameCycles, sendBehaviour, initialRotationPhi)

    private val turnProps = object: TurnTowardsCamera.Delegate() {
        override var rotationPhi = 0.0f // this is the rotation of the paper, not the player's orientation
    }

    private val turnTowardsCamera = TurnTowardsCamera(turnProps, keepUpright = true)
    override val rotationPhi get() = playerBehaviour.orientation

    override val allowActions = true

    private val createConcurrently = CreateConcurrently(
        this,
        object: CreateConcurrently.Delegate {
            private lateinit var tmpImg: ImageData

            override suspend fun onJobStart() {
                // Do not modify gel here! Store everything in temporary variables!
                tmpImg = App.textures.getOrLoadImageDataAsync(TEX_FILENAME)
            }

            override fun onJobDone() {
                // Back in the main thread
                frames.setTexture(tmpImg, 22, 11) // also sets frameSize
                frameScaleFactor.x = 1.1f / 32
                frameScaleFactor.y = 1.5f / 32 // slightly larger, because camera usually is from above
                frameOrigin.set(renderProps.frameSize.x / 2, renderProps.frameSize.y)
                frameCycles.turn(Direction.SE)
            }
        }
    )

    override fun getBehaviour(klass: KClass<*>) = when (klass) {
        // We only need to mention the behaviours here that we want to make public.
        CreateConcurrently::class -> createConcurrently
        Send::class -> sendBehaviour
        else -> null
    }

    override fun onAnimateActive() {
        playerBehaviour.animate()
        sendBehaviour.animate()
        turnTowardsCamera.animate()
        frameCycles.cycle?.animate()
    }

    private val renderProps = object: PaperRenderer.Delegate() {
        override val renderPos = this@PlayerGel.pos // shared mutable object
        override val frameSize = frames.frameSize // shared mutable object
        override val tex get() = frames.tex
        override val texOffset = frames.texOffset // shared mutable object
        override val origin = this@PlayerGel.frameOrigin // shared mutable object
        override val scaleFactor = this@PlayerGel.frameScaleFactor // shared mutable object
        override val rotationPhi get() = turnProps.rotationPhi
        override val rotationRho get() = turnProps.rotationRho
    }

    override fun onCollide(
        shape: BrickShape,
        material: BrickMaterial,
        myHit: HitArea,
        otherHit: HitArea,
        hitPt: Vector3f,
        normalTowardsMe: Vector3f,
    ) {
        if (otherHit == HitArea.TOP_FACE && body.speedBeforeCollisions.z < 0.0f) {
            playerBehaviour.apply {
                didCollideWithFloor = true
                touchDownSpeed = body.nextSpeed.z
                timeOfGroundContact = App.time.sessionTime
            }
        }
    }

    override val renderer = App.factory.createPaperRenderer(renderProps)

    override fun canEncounterOrBeFound() = true

    override fun onEncounter(other: GraphicElement) {
        // The player will come across a lot of objects, e.g. a switch with an action, or an enemy that runs after the
        // player, etc. The *other* gel should usually implement onEncounter and implement the action, to reduce the
        // PlayerGel's scope of concern. That's why the player's encounterRadius can be left at 0 for now. There are
        // still things we could implement here, e.g. turning the player's head towards an interesting object or
        // person, etc. For now, let's throw an exception from here to verify that everything works as expected.
        throw Exception("PlayerGel's onEncounter called for $other, but the other gel should implement it")
    }

    override fun onRemoveZombie() {
        renderer.free()
    }

    override fun toString() = "PlayerGel"

    companion object {
        private const val BODY_HEIGHT = 1.6f
        private const val TEX_FILENAME = "sprite-player.png"
    }
}
