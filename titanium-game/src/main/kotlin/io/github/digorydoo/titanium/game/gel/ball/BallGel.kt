package io.github.digorydoo.titanium.game.gel.ball

import ch.digorydoo.kutils.vector.Vector3f
import io.github.digorydoo.titanium.engine.behaviours.CreateConcurrently
import io.github.digorydoo.titanium.engine.core.App
import io.github.digorydoo.titanium.engine.gel.GraphicElement
import io.github.digorydoo.titanium.engine.mesh.ComplexMesh
import io.github.digorydoo.titanium.engine.mesh.ComplexMeshRenderer
import io.github.digorydoo.titanium.engine.physics.rigid_body.FixedSphereBody
import io.github.digorydoo.titanium.game.gel.ball.BallSpawnPt.Kind
import kotlin.reflect.KClass

class BallGel private constructor(
    override val spawnPt: BallSpawnPt?,
    initialPos: Vector3f,
    private val kind: Kind,
): GraphicElement(spawnPt, initialPos) {
    constructor(spawnPt: BallSpawnPt): this(spawnPt, spawnPt.pos, spawnPt.kind)
    // constructor(kind: Kind, x: Float, y: Float, z: Float): this(null, Vector3f(x, y, z), kind)

    private val radius = when (kind) {
        Kind.BALL_R25CM -> 0.25f
        Kind.BALL_R33CM -> 0.33f
    }

    init {
        bodyPosOffset.set(0.0f, 0.0f, radius)
        inDialog = Visibility.ACTIVE
        inMenu = Visibility.INVISIBLE
        inEditor = Visibility.FROZEN_VISIBLE
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
            Kind.BALL_R25CM -> 0.05f
            Kind.BALL_R33CM -> 0.09f
        },
        gravity = true,
    )

    private var mesh: ComplexMesh? = null

    override val renderer = App.factory.createComplexMeshRenderer(
        object: ComplexMeshRenderer.Delegate() {
            override val mesh get() = this@BallGel.mesh
            override val renderPos = this@BallGel.pos
            override val rotationPhi = spawnPt?.rotation ?: 0.0f
        },
        antiAliasing = false,
        cullFace = true,
        depthTest = true
    )

    private val createConcurrently = CreateConcurrently(
        this,
        object: CreateConcurrently.Delegate {
            private lateinit var tmpMesh: ComplexMesh

            override suspend fun onJobStart() {
                // Do not modify gel here! Store everything in temporary variables!
                tmpMesh = App.meshes.getOrLoadMeshAsync(
                    when (kind) {
                        Kind.BALL_R25CM -> "ball-r25cm.msh"
                        Kind.BALL_R33CM -> "ball-r33cm.msh"
                    }
                )
            }

            override fun onJobDone() {
                // Back in main thread
                mesh = tmpMesh
            }
        }
    )

    override fun getBehaviour(klass: KClass<*>) = when (klass) {
        CreateConcurrently::class -> createConcurrently
        else -> null
    }

    override fun onRemoveZombie() {
        renderer.free()
    }

    override fun toString() = "BallGel(${spawnPt?.id})"
}
