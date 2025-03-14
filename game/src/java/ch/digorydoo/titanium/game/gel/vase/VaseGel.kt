package ch.digorydoo.titanium.game.gel.vase

import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.titanium.engine.brick.Brick
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.file.MeshFileReader
import ch.digorydoo.titanium.engine.gel.GraphicElement
import ch.digorydoo.titanium.engine.mesh.MeshRenderer
import ch.digorydoo.titanium.engine.physics.HitArea
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedCylinderBody
import ch.digorydoo.titanium.game.gel.vase.VaseSpawnPt.Kind

class VaseGel private constructor(
    override val spawnPt: VaseSpawnPt?,
    initialPos: Point3f,
    kind: Kind,
): GraphicElement(spawnPt, initialPos) {
    constructor(spawnPt: VaseSpawnPt): this(spawnPt, spawnPt.pos, spawnPt.kind)

    private val mesh = MeshFileReader.readFile(
        when (kind) {
            Kind.VASE_H1M -> "vase-h1m.msh"
        }
    )

    override val body = FixedCylinderBody(
        "$kind",
        pos, // shared mutable object
        elasticity = 0.3f,
        mass = 133.0f,
        gravity = true,
        radius = 0.25f,
        height = 1.0f,
        zOffset = 0.5f,
        friction = 0.6f,
    )

    override val renderer = App.factory.createMeshRenderer(
        object: MeshRenderer.Delegate() {
            override val mesh get() = this@VaseGel.mesh
            override val renderPos get() = this@VaseGel.pos
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

    override fun didCollide(other: GraphicElement, myHit: HitArea, otherHit: HitArea, hitPt: Point3f) {
        // println("Vase collided with $other")
    }

    override fun didCollide(brick: Brick, hitPt: Point3f, hitNormal: Point3f) {
    }

    override fun onRemoveZombie() {
        renderer.free()
    }

    override fun toString() = "VaseGel(${spawnPt?.id})"
}
