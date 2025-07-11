package ch.digorydoo.titanium.game.gel.door

import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.titanium.engine.core.ActionManager
import ch.digorydoo.titanium.engine.core.ActionManager.ActionDelegate
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.gel.GraphicElement
import ch.digorydoo.titanium.engine.mesh.Mesh
import ch.digorydoo.titanium.engine.mesh.MeshRenderer
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedCuboidBody
import ch.digorydoo.titanium.engine.physics.rigid_body.RigidBody.Companion.LARGE_MASS
import ch.digorydoo.titanium.engine.shader.Renderer
import ch.digorydoo.titanium.game.gel.door.DoorSpawnPt.Kind
import ch.digorydoo.titanium.game.i18n.GameTextId
import ch.digorydoo.titanium.game.player.PlayerGel
import kotlin.math.cos

class DoorGel private constructor(
    override val spawnPt: DoorSpawnPt?,
    initialPos: Point3f,
    kind: Kind,
): GraphicElement(spawnPt, initialPos) {
    constructor(spawnPt: DoorSpawnPt): this(spawnPt, spawnPt.pos, spawnPt.kind)

    init {
        bodyPosOffset.set(0.0f, 0.0f, FRAME_HEIGHT / 2.0f)
        inDialog = Visibility.ACTIVE
        inMenu = Visibility.INVISIBLE
        inEditor = Visibility.FROZEN_VISIBLE
        encounterRadius = 0.5f
        callOnCreateConcurrently = true
    }

    override val body = FixedCuboidBody(
        "$kind",
        initialPos = pos + bodyPosOffset,
        elasticity = 0.75f,
        friction = 0.5f,
        sizeX = 0.25f, // FIXME handle 90° rotated door
        sizeY = 1.01f,
        sizeZ = FRAME_HEIGHT,
        mass = LARGE_MASS,
        gravity = false,
    )

    private val slabPos = MutablePoint3f()
    private var slabRotation = 0.0f

    private var frameMesh: Mesh? = null
    private var slabMesh: Mesh? = null

    override val renderer = makeCombinedRenderer(this)

    override suspend fun onCreateConcurrently(): () -> Unit {
        val theFrameMesh = App.meshes.getOrLoadMeshAsync("door-01-frame.msh")
        val theSlabMesh = App.meshes.getOrLoadMeshAsync("door-01-slab.msh")
        return {
            // Back in main thread
            frameMesh = theFrameMesh
            slabMesh = theSlabMesh
        }
    }

    override fun onAnimateActive() {
        slabPos.set(pos.x, pos.y - SLAB_OFFSET_Y / 2, pos.z)
        slabRotation = 0.25f * cos(App.time.sessionTime)
    }

    private val actionDelegate = object: ActionDelegate {
        override fun onSelect(action: ActionManager.Action) {
            println("TODO: Open door") // TODO
        }
    }

    override fun canEncounterOrBeFound() = true

    override fun onEncounter(other: GraphicElement) {
        if (other is PlayerGel) {
            App.actions.register(GameTextId.OPEN, this, actionDelegate)
        }
    }

    override fun onRemoveZombie() {
        renderer.free()
    }

    override fun toString() = "DoorGel(${spawnPt?.id})"

    companion object {
        private const val FRAME_HEIGHT = 2.6f
        private const val SLAB_OFFSET_Y = 0.87f

        private fun makeCombinedRenderer(gel: DoorGel): Renderer {
            val frameRotation = gel.spawnPt?.rotation ?: 0.0f
            val frameRenderer = App.factory.createMeshRenderer(
                object: MeshRenderer.Delegate() {
                    override val mesh get() = gel.frameMesh
                    override val renderPos = gel.pos // shared mutable object
                    override val rotationPhi = frameRotation
                },
                antiAliasing = false,
                cullFace = true,
                depthTest = true
            )
            val slabRenderer = App.factory.createMeshRenderer(
                object: MeshRenderer.Delegate() {
                    override val mesh get() = gel.slabMesh
                    override val renderPos = gel.slabPos // shared mutable object
                    override val rotationPhi get() = frameRotation + gel.slabRotation
                },
                antiAliasing = false,
                cullFace = true,
                depthTest = true
            )
            return object: Renderer {
                override fun renderShadows() {
                    frameRenderer.renderShadows()
                    slabRenderer.renderShadows()
                }

                override fun renderSolid() {
                    frameRenderer.renderSolid()
                    slabRenderer.renderSolid()
                }

                override fun renderTransparent() {
                    frameRenderer.renderTransparent()
                    slabRenderer.renderTransparent()
                }

                override fun free() {
                    frameRenderer.free()
                    slabRenderer.free()
                }
            }
        }
    }
}
