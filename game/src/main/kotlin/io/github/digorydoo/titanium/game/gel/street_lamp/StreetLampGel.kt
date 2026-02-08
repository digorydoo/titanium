package io.github.digorydoo.titanium.game.gel.street_lamp

import ch.digorydoo.kutils.colour.Colour
import ch.digorydoo.kutils.math.lerp
import ch.digorydoo.kutils.vector.MutableVector2f
import ch.digorydoo.kutils.vector.MutableVector3f
import ch.digorydoo.kutils.vector.Vector2f
import io.github.digorydoo.titanium.engine.behaviours.CreateConcurrently
import io.github.digorydoo.titanium.engine.behaviours.TurnTowardsCamera
import io.github.digorydoo.titanium.engine.core.App
import io.github.digorydoo.titanium.engine.core.FrameCounter
import io.github.digorydoo.titanium.engine.core.LampManager.Lamp
import io.github.digorydoo.titanium.engine.gel.GraphicElement
import io.github.digorydoo.titanium.engine.mesh.ComplexMesh
import io.github.digorydoo.titanium.engine.mesh.ComplexMeshRenderer
import io.github.digorydoo.titanium.engine.physics.rigid_body.FixedCylinderBody
import io.github.digorydoo.titanium.engine.physics.rigid_body.RigidBody
import io.github.digorydoo.titanium.engine.shader.PaperRenderer
import io.github.digorydoo.titanium.engine.shader.Renderer
import io.github.digorydoo.titanium.engine.shader.Renderer.BlendMode
import io.github.digorydoo.titanium.engine.texture.ImageData
import io.github.digorydoo.titanium.engine.texture.Texture
import io.github.digorydoo.titanium.game.gel.street_lamp.StreetLampSpawnPt.Kind.TRADITIONAL
import kotlin.math.abs
import kotlin.reflect.KClass

class StreetLampGel(override val spawnPt: StreetLampSpawnPt): GraphicElement(spawnPt) {
    init {
        bodyPosOffset.set(0.0f, 0.0f, BODY_HEIGHT / 2.0f)
        inDialog = Visibility.ACTIVE // flickering should continue in dialogues
        inEditor = Visibility.ACTIVE // in order that halo gets properly turned
    }

    override val body = FixedCylinderBody(
        "StreetLamp@${spawnPt.id}",
        initialPos = pos + bodyPosOffset,
        elasticity = 0.3f,
        friction = 0.2f,
        gravity = false,
        mass = RigidBody.LARGE_MASS,
        radius = 0.17f,
        height = BODY_HEIGHT,
    )

    private var mesh: ComplexMesh? = null
    private var haloTex: Texture? = null
    private val haloFrameSize = MutableVector2f()
    private val haloOrigin = MutableVector2f()
    private val haloCentre = MutableVector3f()

    private val turnHaloProps = object: TurnTowardsCamera.Delegate() {
        override val centre = this@StreetLampGel.haloCentre
    }

    private val turnHalo = TurnTowardsCamera(turnHaloProps, keepBehind = 1.0f, usePosition = true)

    private val isLightOn
        get() = when {
            !spawnPt.lightOn -> false
            spawnPt.offDuringDaylight && App.time.isDaylightStoryTime -> false
            else -> true
        }

    private var lightIntensity = if (isLightOn) 1.0f else 0.0f

    private val adaptLightIntensity = object {
        private val flickerCount = FrameCounter.everyNthFrame(4)
        private var flickerValue = 1.0f

        fun animate() {
            val targetIntensity = when {
                !isLightOn -> 0.0f
                !spawnPt.flickering -> 1.0f
                else -> {
                    if (flickerCount.next() != 0) {
                        flickerValue
                    } else {
                        // The flickerValue should never be 0, because then the LampManager would think the lamp is off,
                        // and give priority to other lamps.
                        flickerValue = if (Math.random() > 0.15f) 1.0f else 0.05f
                        flickerValue
                    }
                }
            }

            if (lightIntensity == targetIntensity) {
                return
            }

            lightIntensity = lerp(lightIntensity, targetIntensity, 0.1f)

            if (abs(lightIntensity - targetIntensity) < 0.0001f) {
                lightIntensity = targetIntensity
                return
            }
        }
    }

    override val renderer = makeRenderer()

    private fun makeRenderer(): Renderer {
        val meshRenderer = App.factory.createComplexMeshRenderer(
            object: ComplexMeshRenderer.Delegate() {
                override val mesh get() = this@StreetLampGel.mesh
                override val renderPos = this@StreetLampGel.pos
                override val rotationPhi = spawnPt.rotation
                override val emittingLight get() = lightIntensity * 0.1f
            },
            antiAliasing = false,
            cullFace = true,
            depthTest = true
        )

        val haloRenderer = App.factory.createPaperRenderer(
            object: PaperRenderer.Delegate() {
                override val tex get() = haloTex
                override val frameSize = haloFrameSize // shared mutable object
                override val origin = haloOrigin // shared mutable object
                override val scaleFactor = Vector2f(HALO_SCALING, HALO_SCALING)
                override val renderPos = turnHaloProps.renderPos
                override val rotationPhi get() = turnHaloProps.rotationPhi
                override val rotationRho get() = turnHaloProps.rotationRho
                override val opacity get() = lightIntensity
            },
            antiAliasing = true,
            blendMode = BlendMode.ADD,
        )

        return object: Renderer {
            override fun free() {
                meshRenderer.free()
                haloRenderer.free()
            }

            override fun renderShadows() {}

            override fun renderSolid() {
                meshRenderer.renderSolid()
            }

            override fun renderTransparent() {
                haloRenderer.renderTransparent()
            }
        }
    }

    private val lamp = object: Lamp {
        override val pos = this@StreetLampGel.haloCentre
        override val colour = lampColour
        override val intensity get() = if (visible) this@StreetLampGel.lightIntensity else 0.0f
        override val radius = LAMP_RADIUS
    }

    private val createConcurrently = CreateConcurrently(
        this,
        object: CreateConcurrently.Delegate {
            private lateinit var tmpImg: ImageData
            private lateinit var tmpMesh: ComplexMesh

            override suspend fun onJobStart() {
                // Do not modify gel here! Store everything in temporary variables!
                tmpImg = App.textures.getOrLoadImageDataAsync("halo-lamp-yellow.png")

                tmpMesh = App.meshes.getOrLoadMeshAsync(
                    when (spawnPt.kind) {
                        TRADITIONAL -> "street-lamp-01.msh"
                    }
                )
            }

            override fun onJobDone() {
                // Back in main thread
                haloTex = App.textures.getOrCreateTexture(tmpImg).also {
                    haloFrameSize.set(it.width, it.height)
                    haloOrigin.set(it.width / 2, it.height / 2)
                }
                mesh = tmpMesh
                App.lamps.add(lamp)
            }
        }
    )

    override fun getBehaviour(klass: KClass<*>) = when (klass) {
        CreateConcurrently::class -> createConcurrently
        else -> null
    }

    override fun onAboutToRender() {
        haloCentre.set(pos.x, pos.y, pos.z + HALO_Z_OFFSET)
    }

    override fun onAnimateActive() {
        turnHalo.animate()
        adaptLightIntensity.animate()
    }

    override fun onRemoveZombie() {
        App.lamps.remove(lamp)
        renderer.free()
    }

    override fun toString() = "StreetLampGel(${spawnPt.id})"

    companion object {
        private const val BODY_HEIGHT = 4.9f
        private const val HALO_SCALING = 0.025f
        private const val HALO_Z_OFFSET = 4.36f
        private const val LAMP_RADIUS = 10.0f
        private val lampColour = Colour(1.0f, 1.0f, 0.5f)
    }
}
