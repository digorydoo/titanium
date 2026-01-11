package ch.digorydoo.titanium.game.gel.static_paper

import ch.digorydoo.kutils.point.MutablePoint2f
import ch.digorydoo.titanium.engine.behaviours.CreateConcurrently
import ch.digorydoo.titanium.engine.behaviours.TurnTowardsCamera
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.gel.GraphicElement
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedCylinderBody
import ch.digorydoo.titanium.engine.physics.rigid_body.RigidBody
import ch.digorydoo.titanium.engine.shader.PaperRenderer
import ch.digorydoo.titanium.engine.sprite.FrameCollection
import ch.digorydoo.titanium.engine.texture.ImageData
import ch.digorydoo.titanium.game.gel.static_paper.StaticPaperSpawnPt.Kind.*
import kotlin.reflect.KClass

class StaticPaperGel(override val spawnPt: StaticPaperSpawnPt): GraphicElement(spawnPt) {
    init {
        bodyPosOffset.set(0.0f, 0.0f, BODY_HEIGHT / 2.0f)
    }

    override val body = FixedCylinderBody(
        "StaticPaper",
        initialPos = pos + bodyPosOffset,
        elasticity = 0.3f,
        friction = 0.2f,
        gravity = false,
        mass = RigidBody.LARGE_MASS,
        radius = 0.25f,
        height = BODY_HEIGHT,
    )

    private val turnProps = object: TurnTowardsCamera.Delegate() {
        override var rotationPhi = spawnPt.rotation
    }

    private val turn = TurnTowardsCamera(turnProps, keepUpright = true)

    private val frames = FrameCollection()
    private val frameOrigin = MutablePoint2f()

    private val renderProps = object: PaperRenderer.Delegate() {
        override val renderPos = this@StaticPaperGel.pos
        override val frameSize = frames.frameSize
        override val tex get() = frames.tex
        override val texOffset get() = frames.texOffset
        override val origin get() = this@StaticPaperGel.frameOrigin
        override val rotationPhi get() = turnProps.rotationPhi
        override val rotationRho get() = turnProps.rotationRho
        override val scaleFactor = MutablePoint2f(1.0f / 36, 1.0f / 36)
    }

    override val renderer = App.factory.createPaperRenderer(renderProps)

    private val createConcurrently = CreateConcurrently(
        this,
        object: CreateConcurrently.Delegate {
            private lateinit var tmpImg: ImageData
            private var offset = 0

            override suspend fun onJobStart() {
                // Do not modify gel here! Store everything in temporary variables!
                when (spawnPt.kind) {
                    GNARLED_TREE_LARGE -> {
                        tmpImg = App.textures.getOrLoadImageDataAsync("sprite-static-gnarled-tree-large.png")
                        offset = 3
                    }
                    GNARLED_TREE_MEDIUM -> {
                        tmpImg = App.textures.getOrLoadImageDataAsync("sprite-static-gnarled-tree-medium.png")
                        offset = 2
                    }
                    GNARLED_TREE_SMALL -> {
                        tmpImg = App.textures.getOrLoadImageDataAsync("sprite-static-gnarled-tree-small.png")
                        offset = 1
                    }
                    ROUND_TREE -> {
                        tmpImg = App.textures.getOrLoadImageDataAsync("sprite-static-round-tree.png")
                        offset = 2
                    }
                }
            }

            override fun onJobDone() {
                // Back in main thread
                frames.setTexture(tmpImg, 1, 1)
                frameOrigin.set(renderProps.frameSize.x / 2, renderProps.frameSize.y - offset)
            }
        }
    )

    override fun getBehaviour(klass: KClass<*>) = when (klass) {
        CreateConcurrently::class -> createConcurrently
        else -> null
    }

    override fun onAnimateActive() {
        turn.animate()
    }

    override fun onRemoveZombie() {
        renderer.free()
    }

    override fun toString() = "StaticPaperGel(${spawnPt.id})"

    companion object {
        private const val BODY_HEIGHT = 2.0f
    }
}
