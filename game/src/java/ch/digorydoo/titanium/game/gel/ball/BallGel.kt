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
    constructor(kind: Kind, x: Float, y: Float, z: Float): this(null, Point3f(x, y, z), kind)

    init {
        bodyPosOffset.set(0.0f, 0.0f, BALL_RADIUS)
        inDialog = Visibility.ACTIVE
        inMenu = Visibility.INVISIBLE
        inEditor = Visibility.ACTIVE
    }

    override val body = FixedSphereBody(
        "$kind",
        initialPos = pos + bodyPosOffset,
        elasticity = 0.9f,
        mass = 0.5f,
        gravity = true,
        radius = BALL_RADIUS,
        friction = 0.1f,
    )

    private val mesh = MeshFileReader.readFile(
        when (kind) {
            Kind.BALL_R25CM -> "ball-r25cm.msh"
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

    companion object {
        private const val BALL_RADIUS = 0.25f
    }
}
