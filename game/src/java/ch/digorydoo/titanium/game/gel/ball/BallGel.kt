package ch.digorydoo.titanium.game.gel.ball

import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.file.MeshFileReader
import ch.digorydoo.titanium.engine.gel.GraphicElement
import ch.digorydoo.titanium.engine.mesh.MeshRenderer
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedSphereBody
import ch.digorydoo.titanium.game.gel.ball.BallSpawnPt.Kind

class BallGel private constructor(
    override val spawnPt: BallSpawnPt?,
    initialPos: Point3f,
    kind: Kind,
): GraphicElement(spawnPt, initialPos) {
    constructor(spawnPt: BallSpawnPt): this(spawnPt, spawnPt.pos, spawnPt.kind)
    // constructor(kind: Kind, x: Float, y: Float, z: Float): this(null, Point3f(x, y, z), kind)

    private val radius = when (kind) {
        Kind.BALL_R25CM -> 0.25f
        Kind.BALL_R33CM -> 0.33f
    }

    init {
        bodyPosOffset.set(0.0f, 0.0f, radius)
        inDialog = Visibility.ACTIVE
        inMenu = Visibility.INVISIBLE
        inEditor = Visibility.ACTIVE
    }

    override val body = FixedSphereBody(
        "$kind",
        initialPos = pos + bodyPosOffset,
        elasticity = when (kind) {
            Kind.BALL_R25CM -> 0.96f
            Kind.BALL_R33CM -> 0.99f
        },
        mass = when (kind) {
            Kind.BALL_R25CM -> 0.52f
            Kind.BALL_R33CM -> 0.42f
        },
        radius = radius,
        friction = when (kind) {
            Kind.BALL_R25CM -> 0.001f
            Kind.BALL_R33CM -> 0.009f
        },
        gravity = true,
    )

    private val mesh = MeshFileReader.readFile(
        when (kind) {
            Kind.BALL_R25CM -> "ball-r25cm.msh"
            Kind.BALL_R33CM -> "ball-r33cm.msh"
        }
    )

    override val renderer = App.factory.createMeshRenderer(
        object: MeshRenderer.Delegate() {
            override val mesh get() = this@BallGel.mesh
            override val renderPos = this@BallGel.pos
            override val rotationPhi = spawnPt?.rotation ?: 0.0f
        },
        antiAliasing = false,
        cullFace = true,
        depthTest = true
    )

    override fun onRemoveZombie() {
        renderer.free()
    }

    override fun toString() = "BallGel(${spawnPt?.id})"
}
