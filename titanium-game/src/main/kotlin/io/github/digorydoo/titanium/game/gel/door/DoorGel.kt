package io.github.digorydoo.titanium.game.gel.door

import ch.digorydoo.kutils.math.decel
import ch.digorydoo.kutils.vector.MutableVector3f
import ch.digorydoo.kutils.vector.Vector3f
import io.github.digorydoo.titanium.engine.behaviours.CreateConcurrently
import io.github.digorydoo.titanium.engine.behaviours.Send
import io.github.digorydoo.titanium.engine.camera.CameraDirectingMode
import io.github.digorydoo.titanium.engine.camera.CameraInertia
import io.github.digorydoo.titanium.engine.core.ActionManager
import io.github.digorydoo.titanium.engine.core.ActionManager.ActionDelegate
import io.github.digorydoo.titanium.engine.core.App
import io.github.digorydoo.titanium.engine.gel.GraphicElement
import io.github.digorydoo.titanium.engine.mesh.ComplexMesh
import io.github.digorydoo.titanium.engine.mesh.ComplexMeshRenderer
import io.github.digorydoo.titanium.engine.physics.rigid_body.FixedCuboidBody
import io.github.digorydoo.titanium.engine.physics.rigid_body.RigidBody.Companion.LARGE_MASS
import io.github.digorydoo.titanium.engine.shader.Renderer
import io.github.digorydoo.titanium.game.gel.door.DoorSpawnPt.Kind
import io.github.digorydoo.titanium.game.i18n.GameTextId
import io.github.digorydoo.titanium.game.player.PlayerGel
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.reflect.KClass

class DoorGel private constructor(
    override val spawnPt: DoorSpawnPt?,
    initialPos: Vector3f,
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

    enum class State { CLOSED, OPENING, OPEN, CLOSING }

    private enum class Side(val sign: Float) {
        FRONT(-1.0f), BACK(1.0f)
    }

    var state = State.CLOSED; private set
    private var stateChangedTime = App.time.sessionTime
    private var opensTowards = Side.FRONT

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

    private val slabPos = MutableVector3f()
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

    override fun onCreateDone() {
        slabPos.set(pos.x, pos.y - SLAB_OFFSET_Y / 2, pos.z)
    }

    private fun open(facingFront: Boolean = true) {
        if (state == State.OPEN || state == State.OPENING) return
        state = State.OPENING
        stateChangedTime = App.time.sessionTime
        opensTowards = if (facingFront) Side.BACK else Side.FRONT
    }

    private fun close() {
        if (state == State.CLOSED || state == State.CLOSING) return
        state = State.CLOSING
        stateChangedTime = App.time.sessionTime
    }

    override fun onAnimateActive() {
        when (state) {
            State.OPEN, State.CLOSED -> Unit
            State.OPENING -> {
                val secondsSinceStart = App.time.sessionTime - stateChangedTime
                val relTime = secondsSinceStart / OPENING_DURATION_SECONDS

                if (relTime >= 1.0f) {
                    this.state = State.OPEN
                    slabRotation = opensTowards.sign * ROTATION_ANGLE_WHEN_OPEN
                } else {
                    slabRotation = decel(relTime, k = 1.5f) * opensTowards.sign * ROTATION_ANGLE_WHEN_OPEN
                }
            }
            State.CLOSING -> {
                val secondsSinceStart = App.time.sessionTime - stateChangedTime
                val relTime = secondsSinceStart / CLOSING_DURATION_SECONDS

                if (relTime >= 1.0f) {
                    this.state = State.CLOSED
                    slabRotation = 0f
                } else {
                    slabRotation = (1.0f - relTime) * opensTowards.sign * ROTATION_ANGLE_WHEN_OPEN
                }
            }
        }
    }

    private val actionDelegate = object: ActionDelegate {
        override fun onSelect(action: ActionManager.Action) {
            val player = App.player ?: return

            val origCollisionsWithGelsSuppressed = player.collisionsWithGelsSuppressed
            player.collisionsWithGelsSuppressed = true

            val sendPlayer = player.get<Send>()
            val camera = App.camera
            val origTargetGel = camera.targetGel
            val origCameraMode = camera.directingMode
            val origCameraInertia = camera.inertia

            App.intermissions.begin {
                camera.directingMode = CameraDirectingMode.FIXED_DISTANCE
                camera.setTarget(this@DoorGel)
                camera.inertia = CameraInertia.HIGH

                val facingFront = isInFront(player.pos, angularOffset = (PI / 2.0).toFloat())

                val phiTowardsDoor = (spawnPt?.rotation ?: 0.0f) + when (facingFront) {
                    true -> -(PI / 2.0).toFloat()
                    false -> (PI / 2.0).toFloat()
                }

                val rhoHorizontal = -(PI / 2.0).toFloat()

                camera.setSourceRelativeToTarget(
                    phi = phiTowardsDoor,
                    rho = rhoHorizontal,
                    distance = 2.0f
                )

                // Wait until the camera has almost completed adjusting itself
                sleep(seconds = 0.1f)

                // Just a test for "cancellable" a.k.a. cutscene. FIXME remove this once we have an actual cutscene
                cancellable {
                    // Start opening the door
                    open(facingFront)

                    // Wait until the door has opened a little
                    sleep(seconds = 0.2f * OPENING_DURATION_SECONDS)

                    // Send the player towards the door centre while the door is still opening
                    sendPlayer?.sendTo(body.pos, ignoreZ = true)

                    // Wait until the door is fully open and the player has reached the door centre
                    waitFor { state == State.OPEN && sendPlayer?.active != true }

                    // Send the player further into the room
                    sendPlayer?.sendTo(getPlayerTargetPos(facingFront), ignoreZ = true, maxSpeed = 2.0f)

                    // Make the camera move towards the player and turn slightly towards the opening
                    camera.setTarget(player)
                    camera.setSourceRelativeToTarget(
                        phi = phiTowardsDoor + (if (facingFront) CAMERA_PHI_OFFSET else -CAMERA_PHI_OFFSET),
                        rho = rhoHorizontal,
                        distance = 0.2f,
                    )

                    // Wait until the camera has almost reached the door centre
                    sleep(seconds = 1.1f)

                    // Start closing the door
                    close()

                    // Make the camera move even closer and restore its direction
                    camera.setSourceRelativeToTarget(
                        phi = phiTowardsDoor,
                        rho = rhoHorizontal,
                        distance = 2.0f,
                    )

                    // Wait until the camera has moved a little closer
                    sleep(seconds = 0.42f)

                    // Change camera inertia to make it move a little faster for the last bit.
                    camera.inertia = CameraInertia.NORMAL

                    // Wait until camera has settled.
                    sleep(seconds = 0.5f)

                    // Wait for player to stop moving (if he hasn't).
                    waitFor { sendPlayer?.active != true }
                }

                // Restore camera properties and player
                camera.directingMode = origCameraMode
                camera.setTarget(origTargetGel)
                camera.inertia = origCameraInertia
                player.collisionsWithGelsSuppressed = origCollisionsWithGelsSuppressed
            }
        }
    }

    private fun getPlayerTargetPos(facingFront: Boolean): Vector3f {
        val doorCentre = body.pos
        val alpha = (spawnPt?.rotation ?: 0.0f) + (if (facingFront) -PLAYER_ALPHA_OFFSET else PLAYER_ALPHA_OFFSET)
        val signedDistance = DISTANCE_OF_PLAYER_FROM_DOOR_AFTERWARDS * (if (facingFront) -1.0f else 1.0f)
        return Vector3f(
            doorCentre.x + signedDistance * cos(alpha),
            doorCentre.y + signedDistance * sin(alpha),
            doorCentre.z
        )
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
        private const val OPENING_DURATION_SECONDS = 0.5f
        private const val CLOSING_DURATION_SECONDS = 1.3f
        private const val ROTATION_ANGLE_WHEN_OPEN = 1.7f
        private const val DISTANCE_OF_PLAYER_FROM_DOOR_AFTERWARDS = 3.0f
        private const val PLAYER_ALPHA_OFFSET = 0.3f // turn player slightly when moving through door
        private const val CAMERA_PHI_OFFSET = 0.42f // turn camera slightly when player is moving through door

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
