package ch.digorydoo.titanium.game.gel.static_mesh

import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.file.MeshFileReader
import ch.digorydoo.titanium.engine.gel.GraphicElement
import ch.digorydoo.titanium.engine.mesh.MeshRenderer
import ch.digorydoo.titanium.engine.physics.FixedCylinderBody
import ch.digorydoo.titanium.engine.physics.RigidBody
import ch.digorydoo.titanium.game.gel.static_mesh.StaticMeshSpawnPt.Kind.*

class StaticMeshGel(override val spawnPt: StaticMeshSpawnPt): GraphicElement(spawnPt) {
    private val mesh = MeshFileReader.readFile(
        when (spawnPt.kind) {
            BENCH_1 -> "bench-01.msh"
            STONE_1 -> "obj-stone-01.msh"
            SIGN_1 -> "sign-01.msh"
            ROBOT_POLICEMAN -> "robot-policeman.msh"
            RAILING_1 -> "railing-01.msh"
            RAILING_2 -> "railing-02.msh"
        }
    )

    override val renderer = App.factory.createMeshRenderer(
        object: MeshRenderer.Delegate() {
            override val mesh get() = this@StaticMeshGel.mesh
            override val renderPos get() = this@StaticMeshGel.pos
            override val rotationPhi = spawnPt.rotation
        },
        antiAliasing = false,
        cullFace = true,
        depthTest = true
    )

    override val body = FixedCylinderBody(
        "StaticMesh(${spawnPt.kind})",
        pos, // shared mutable object
        elasticity = 0.3f,
        friction = 0.2f,
        mass = RigidBody.LARGE_MASS,
        gravity = false,
        radius = 0.25f,
        height = 1.5f,
        zOffset = 0.75f,
    )

    override fun onRemoveZombie() {
        renderer.free()
    }

    override fun toString() = "StaticMeshGel(${spawnPt.id})"
}
