package ch.digorydoo.titanium.game.gel.vase

import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.file.MeshFileReader
import ch.digorydoo.titanium.engine.gel.GraphicElement
import ch.digorydoo.titanium.engine.mesh.MeshRenderer
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedCylinderBody
import ch.digorydoo.titanium.game.gel.vase.VaseSpawnPt.Kind

class VaseGel private constructor(
    override val spawnPt: VaseSpawnPt?,
    initialPos: Point3f,
    kind: Kind,
): GraphicElement(spawnPt, initialPos) {
    constructor(spawnPt: VaseSpawnPt): this(spawnPt, spawnPt.pos, spawnPt.kind)

    init {
        bodyPosOffset.set(0.0f, 0.0f, BODY_HEIGHT / 2.0f)
        inDialog = Visibility.ACTIVE
        inMenu = Visibility.INVISIBLE
        inEditor = Visibility.ACTIVE
    }

    override val body = FixedCylinderBody(
        "$kind",
        initialPos = pos + bodyPosOffset,
        elasticity = 0.64f,
        mass = 64.0f, // 133.0f,
        gravity = true,
        radius = 0.3f,
        height = BODY_HEIGHT,
        friction = 0.6f,
    )

    private val mesh = MeshFileReader.readFile(
        when (kind) {
            Kind.VASE_H1M -> "vase-h1m.msh"
        }
    )

    override val renderer = App.factory.createMeshRenderer(
        object: MeshRenderer.Delegate() {
            override val mesh get() = this@VaseGel.mesh
            override val renderPos = this@VaseGel.pos
            override val rotationPhi = spawnPt?.rotation ?: 0.0f
        },
        antiAliasing = false,
        cullFace = true,
        depthTest = true
    )

    override fun onRemoveZombie() {
        renderer.free()
    }

    override fun toString() = "VaseGel(${spawnPt?.id})"

    companion object {
        private const val BODY_HEIGHT = 1.0f
    }
}
