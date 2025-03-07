package ch.digorydoo.titanium.game.gel.ball

import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.titanium.engine.brick.Brick
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.file.MeshFileReader
import ch.digorydoo.titanium.engine.gel.GraphicElement
import ch.digorydoo.titanium.engine.mesh.MeshRenderer
import ch.digorydoo.titanium.engine.physics.FixedSphereBody
import ch.digorydoo.titanium.game.gel.ball.BallSpawnPt.Kind

class BallGel private constructor(
    override val spawnPt: BallSpawnPt?,
    initialPos: Point3f,
    kind: Kind,
): GraphicElement(spawnPt, initialPos) {
    constructor(spawnPt: BallSpawnPt): this(spawnPt, spawnPt.pos, spawnPt.kind)
    constructor(kind: Kind, x: Float, y: Float, z: Float): this(null, Point3f(x, y, z), kind)

    private val mesh = MeshFileReader.readFile(
        when (kind) {
            Kind.BALL_R25CM -> "ball-r25cm.msh"
        }
    )

    override val body = FixedSphereBody(
        "$kind",
        pos, // shared mutable object
        elasticity = 0.9f,
        mass = 0.5f,
        gravity = true,
        radius = 0.25f,
        zOffset = 0.25f,
        friction = 0.1f,
    )

    override val renderer = App.factory.createMeshRenderer(
        object: MeshRenderer.Delegate() {
            override val mesh get() = this@BallGel.mesh
            override val renderPos get() = this@BallGel.pos
            override val rotationPhi = spawnPt?.rotation ?: 0.0f
        },
        antiAliasing = false,
        cullFace = true,
        depthTest = true
    )

    override val inDialog = Visibility.ACTIVE
    override val inMenu = Visibility.INVISIBLE
    override val inEditor = Visibility.ACTIVE

    override fun onAnimateActive() {
    }

    override fun didCollide(other: GraphicElement, hitPt: Point3f): Boolean {
        // println("Ball collided with $other")
        return true // true = bounce
    }

    override fun didCollide(brick: Brick, hitPt: Point3f, hitNormal: Point3f) {
    }

    override fun onRemoveZombie() {
        renderer.free()
    }

    override fun toString() = "BallGel(${spawnPt?.id})"
}
