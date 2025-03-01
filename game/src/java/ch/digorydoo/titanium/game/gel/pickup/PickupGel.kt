package ch.digorydoo.titanium.game.gel.pickup

import ch.digorydoo.kutils.point.MutablePoint2f
import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.titanium.engine.behaviours.TurnTowardsCamera
import ch.digorydoo.titanium.engine.brick.Brick
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.gel.GraphicElement
import ch.digorydoo.titanium.engine.physics.FixedSphereBody
import ch.digorydoo.titanium.engine.shader.PaperRenderer
import ch.digorydoo.titanium.engine.texture.FrameCollection
import ch.digorydoo.titanium.game.gel.pickup.PickupSpawnPt.Kind
import ch.digorydoo.titanium.game.gel.pickup.PickupSpawnPt.Kind.VASE

class PickupGel private constructor(
    override val spawnPt: PickupSpawnPt?,
    initialPos: Point3f,
    kind: Kind,
): GraphicElement(spawnPt, initialPos) {
    constructor(spawnPt: PickupSpawnPt): this(spawnPt, spawnPt.pos, spawnPt.kind)
    constructor(kind: Kind, x: Float, y: Float, z: Float): this(null, Point3f(x, y, z), kind)

    override val body = FixedSphereBody(
        "Pickup",
        pos, // shared mutable object
        elasticity = 0.3f,
        mass = 20.0f,
        gravity = true,
        radius = 0.5f,
        zOffset = 0.5f,
        friction = 0.5f,
    )

    private val frames = FrameCollection()
    private val frameOrigin = MutablePoint2f()

    private val turnProps = object: TurnTowardsCamera.Delegate() {
        override var rotationPhi = spawnPt?.rotation ?: 0.0f
    }

    private val turn = TurnTowardsCamera(turnProps, keepUpright = true)

    private val renderProps = object: PaperRenderer.Delegate() {
        override val renderPos get() = this@PickupGel.pos
        override val frameSize get() = frames.frameSize
        override val tex get() = frames.tex
        override val texOffset get() = frames.texOffset
        override val origin get() = this@PickupGel.frameOrigin
        override val opacity = 1.0f
        override val scaleFactor = MutablePoint2f(1.0f / 36, 1.0f / 36)
        override val rotationPhi get() = turnProps.rotationPhi
    }

    override val renderer = App.factory.createPaperRenderer(renderProps)

    override val inDialog = Visibility.ACTIVE
    override val inMenu = Visibility.INVISIBLE
    override val inEditor = Visibility.ACTIVE

    init {
        when (kind) {
            VASE -> frames.setTexture("sprite-pickup-vase.png", 1, 1)
        }
        frameOrigin.set(renderProps.frameSize.x / 2, renderProps.frameSize.y - 4)
    }

    override fun onAnimateActive() {
        turn.animate()
    }

    override fun didCollide(other: GraphicElement, hitPt: Point3f): Boolean {
        // println("PickupGel collided with $other")
        return true // true = bounce
    }

    override fun didCollide(brick: Brick, hitPt: Point3f, hitNormal: Point3f) {
    }

    override fun onRemoveZombie() {
        renderer.free()
    }
}
