package ch.digorydoo.titanium.game.player

import ch.digorydoo.kutils.point.MutablePoint2f
import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.titanium.engine.behaviours.TurnTowardsCamera
import ch.digorydoo.titanium.engine.brick.BrickMaterial
import ch.digorydoo.titanium.engine.brick.BrickShape
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.gel.AbstrPlayerGel
import ch.digorydoo.titanium.engine.gel.GraphicElement
import ch.digorydoo.titanium.engine.physics.HitArea
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedCapsuleBody
import ch.digorydoo.titanium.engine.shader.PaperRenderer
import ch.digorydoo.titanium.engine.texture.FrameCollection
import ch.digorydoo.titanium.engine.utils.Direction

class PlayerGel(initialPos: Point3f, initialRotationPhi: Float): AbstrPlayerGel(initialPos) {
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
    private val frameOrigin = MutablePoint2f()
    private val frameScaleFactor = MutablePoint2f()
    private val frameMgr = PlayerFrameManager(frames)

    private val playerBehaviour = PlayerBehaviour(this, frameMgr)

    private val turnProps = object: TurnTowardsCamera.Delegate() {
        override var rotationPhi = initialRotationPhi
    }

    private val turnTowardsCamera = TurnTowardsCamera(turnProps, keepUpright = true)
    override val rotationPhi get() = turnProps.rotationPhi

    override val allowActions = true

    override fun onAnimateActive() {
        playerBehaviour.animate()
        turnTowardsCamera.animate()
        frameMgr.cycle?.animate()
    }

    private val renderProps = object: PaperRenderer.Delegate() {
        override val renderPos get() = this@PlayerGel.pos
        override val frameSize get() = frames.frameSize
        override val tex get() = frames.tex
        override val texOffset get() = frames.texOffset
        override val origin get() = this@PlayerGel.frameOrigin
        override val scaleFactor get() = this@PlayerGel.frameScaleFactor
        override val rotationPhi get() = turnProps.rotationPhi
        override val rotationRho get() = turnProps.rotationRho
    }

    override fun onCollide(
        shape: BrickShape,
        material: BrickMaterial,
        myHit: HitArea,
        otherHit: HitArea,
        hitPt: Point3f,
        normalTowardsMe: Point3f,
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

    init {
        frames.setTexture("sprite-player.png", 22, 11) // sets frameSize
        frameScaleFactor.x = 1.1f / 32
        frameScaleFactor.y = 1.5f / 32 // slightly larger, because camera usually is from above
        frameOrigin.set(renderProps.frameSize.x / 2, renderProps.frameSize.y)
        frameMgr.turn(Direction.SE)
    }

    override fun canEncounterOrBeFound() = true

    override fun onEncounter(other: GraphicElement) {
        // The player will come across a lot of objects, e.g. a switch with an action, or an enemy that runs after the
        // player, etc. The *other* gel should usually implement onEncounter, to reduce the PlayerGel's scope of
        // concern. That's why the player's encounterRadius can be left at 0 for now. If we're ever going to implement
        // the player turning his head towards an interesting object or person, we should give the player an
        // encounterRadius and implement that from here, because that should not be the concern of the other object.
        // For now, let's throw an exception from here to verify that everything works as expected.
        throw Exception("PlayerGel's onEncounter called for $other, but the other gel should implement it")
    }

    override fun onRemoveZombie() {
        renderer.free()
    }

    override fun toString() = "PlayerGel"

    companion object {
        private const val BODY_HEIGHT = 1.6f
    }
}
