package ch.digorydoo.titanium.game.gel.vase

import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.titanium.engine.core.ActionManager
import ch.digorydoo.titanium.engine.core.ActionManager.ActionDelegate
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.file.MeshFileReader
import ch.digorydoo.titanium.engine.gel.GraphicElement
import ch.digorydoo.titanium.engine.mesh.MeshRenderer
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedCapsuleBody
import ch.digorydoo.titanium.game.gel.vase.VaseSpawnPt.Kind
import ch.digorydoo.titanium.game.i18n.GameTextId
import ch.digorydoo.titanium.game.player.PlayerGel
import kotlin.random.Random

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
        encounterRadius = 0.5f // we're implementing onEncounter, not the PlayerGel!
    }

    override val body = FixedCapsuleBody(
        "$kind",
        initialPos = pos + bodyPosOffset,
        mass = 101.0f,
        gravity = true,
        radius = 0.3f,
        height = BODY_HEIGHT,
        elasticity = 0.64f,
        friction = 0.999f,
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

    private val actionDelegate = object: ActionDelegate {
        override fun onSelect(action: ActionManager.Action) {
            println("VaseGel actionDelegate onSelect") // TODO
        }
    }

    override fun canEncounterOrBeFound() = true

    private val randomVerb = arrayOf(
        GameTextId.SETTINGS,
        GameTextId.ACHIEVEMENTS,
        GameTextId.INVENTORY,
        GameTextId.MAP,
        GameTextId.EXIT_TO_TITLE,
    ).let { it[Random.nextInt(it.size)] }

    override fun onEncounter(other: GraphicElement) {
        // will also be called for VaseGel vs VaseGel, etc.
        if (other is PlayerGel) {
            App.actions.register(randomVerb, this, actionDelegate)
        }
    }

    override fun onRemoveZombie() {
        renderer.free()
    }

    override fun toString() = "VaseGel(${spawnPt?.id})"

    companion object {
        private const val BODY_HEIGHT = 1.0f
    }
}
