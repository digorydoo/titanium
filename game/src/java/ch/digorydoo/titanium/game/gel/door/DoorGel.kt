package ch.digorydoo.titanium.game.gel.door

import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.titanium.engine.behaviours.CreateConcurrently
import ch.digorydoo.titanium.engine.camera.CameraProps
import ch.digorydoo.titanium.engine.core.ActionManager
import ch.digorydoo.titanium.engine.core.ActionManager.ActionDelegate
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.gel.GraphicElement
import ch.digorydoo.titanium.engine.mesh.ComplexMesh
import ch.digorydoo.titanium.engine.mesh.ComplexMeshRenderer
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedCuboidBody
import ch.digorydoo.titanium.engine.physics.rigid_body.RigidBody.Companion.LARGE_MASS
import ch.digorydoo.titanium.engine.shader.Renderer
import ch.digorydoo.titanium.game.gel.door.DoorSpawnPt.Kind
import ch.digorydoo.titanium.game.i18n.GameTextId
import ch.digorydoo.titanium.game.player.PlayerGel
import kotlin.math.PI
import kotlin.math.cos
import kotlin.reflect.KClass

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

    private var frameMesh: ComplexMesh? = null
    private var slabMesh: ComplexMesh? = null

    override val renderer = makeCombinedRenderer(this)

    private val createConcurrently = CreateConcurrently(
        this,
        object: CreateConcurrently.Delegate {
            private lateinit var tmpFrameMesh: ComplexMesh
            private lateinit var tmpSlabMesh: ComplexMesh

            override suspend fun onJobStart() {
                // Do not modify gel here! Store everything in temporary variables!
                tmpFrameMesh = App.meshes.getOrLoadMeshAsync("door-01-frame.msh")
                tmpSlabMesh = App.meshes.getOrLoadMeshAsync("door-01-slab.msh")
            }

            override fun onJobDone() {
                // Back in main thread
                frameMesh = tmpFrameMesh
                slabMesh = tmpSlabMesh
            }
        }
    )

    override fun getBehaviour(klass: KClass<*>) = when (klass) {
        CreateConcurrently::class -> createConcurrently
        else -> null
    }

    override fun onAnimateActive() {
        slabPos.set(pos.x, pos.y - SLAB_OFFSET_Y / 2, pos.z)
        slabRotation = 0.25f * cos(App.time.sessionTime)
    }

    private val actionDelegate = object: ActionDelegate {
        override fun onSelect(action: ActionManager.Action) {
            val camera = App.camera
            val origTargetGel = camera.targetGel
            val origCameraMode = camera.mode

            App.intermissions.begin {
                showDlg<Unit> {
                    text = "This is a test of intermission. Sleeping for 5 seconds..."
                }
            }.then {
                sleep(5.0f)
            }.then {
                camera.mode = CameraProps.Mode.FIXED_DISTANCE
                camera.setTarget(this@DoorGel)

                camera.setSourceRelativeToTarget(
                    phi = (3 * PI / 2.0).toFloat() + 0.1f,
                    rho = (spawnPt?.rotation ?: 0.0f) - (PI / 2.0).toFloat(),
                    distance = 2.0f
                )
                showDlg<Unit> {
                    text = "Now we're moving the camera while the text is being shown. " +
                        "If you quickly dismiss this dialogue, we'll wait until the camera slows down."
                }
            }.then {
                waitFor { camera.currentSpeedApprox < 0.0001f }
            }.then {
                showDlg {
                    text = "Is everything in order?"

                    item {
                        text = "Yes, perfect"
                        id = true
                    }
                    item {
                        text = "Not quite"
                        id = false
                    }
                }
            }.then {
                camera.mode = origCameraMode
                camera.setTarget(origTargetGel)
                showDlg<Unit> {
                    text = "You responded with $it"
                }
            }
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
            val frameRenderer = App.factory.createComplexMeshRenderer(
                object: ComplexMeshRenderer.Delegate() {
                    override val mesh get() = gel.frameMesh
                    override val renderPos = gel.pos // shared mutable object
                    override val rotationPhi = frameRotation
                },
                antiAliasing = false,
                cullFace = true,
                depthTest = true
            )
            val slabRenderer = App.factory.createComplexMeshRenderer(
                object: ComplexMeshRenderer.Delegate() {
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
