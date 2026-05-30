package io.github.digorydoo.titanium.game.gel.static_mesh

import io.github.digorydoo.titanium.engine.behaviours.CreateConcurrently
import io.github.digorydoo.titanium.engine.core.App
import io.github.digorydoo.titanium.engine.gel.GraphicElement
import io.github.digorydoo.titanium.engine.mesh.ComplexMesh
import io.github.digorydoo.titanium.engine.mesh.ComplexMeshRenderer
import io.github.digorydoo.titanium.engine.physics.rigid_body.FixedCylinderBody
import io.github.digorydoo.titanium.engine.physics.rigid_body.RigidBody
import io.github.digorydoo.titanium.game.gel.static_mesh.StaticMeshSpawnPt.Kind.*
import kotlin.reflect.KClass

class StaticMeshGel(override val spawnPt: StaticMeshSpawnPt): GraphicElement(spawnPt) {
    init {
        bodyPosOffset.set(0.0f, 0.0f, BODY_HEIGHT / 2.0f)
    }

    override val body = FixedCylinderBody(
        "StaticMesh(${spawnPt.kind})",
        initialPos = pos + bodyPosOffset,
        elasticity = 0.3f,
        friction = 0.2f,
        mass = RigidBody.LARGE_MASS,
        gravity = false,
        radius = 0.25f,
        height = BODY_HEIGHT,
    )

    private var mesh: ComplexMesh? = null

    override val renderer = App.factory.createComplexMeshRenderer(
        object: ComplexMeshRenderer.Delegate() {
            override val mesh get() = this@StaticMeshGel.mesh
            override val renderPos = this@StaticMeshGel.pos
            override val rotationPhi = spawnPt.rotation
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
                    when (spawnPt.kind) {
                        BENCH_1 -> "bench-01.msh"
                        STONE_1 -> "obj-stone-01.msh"
                        SIGN_1 -> "sign-01.msh"
                        ROBOT_POLICEMAN -> "robot-policeman.msh"
                        RAILING_1 -> "railing-01.msh"
                        RAILING_2 -> "railing-02.msh"
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

    override fun toString() = "StaticMeshGel(${spawnPt.id})"

    companion object {
        private const val BODY_HEIGHT = 1.5f
    }
}
