package ch.digorydoo.titanium.game.gel.static_paper

import ch.digorydoo.kutils.point.MutablePoint2f
import ch.digorydoo.titanium.engine.behaviours.TurnTowardsCamera
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.gel.GraphicElement
import ch.digorydoo.titanium.engine.physics.FixedCylinderBody
import ch.digorydoo.titanium.engine.physics.RigidBody
import ch.digorydoo.titanium.engine.shader.PaperRenderer
import ch.digorydoo.titanium.engine.texture.FrameCollection
import ch.digorydoo.titanium.game.gel.static_paper.StaticPaperSpawnPt.Kind.*

class StaticPaperGel(override val spawnPt: StaticPaperSpawnPt): GraphicElement(spawnPt) {
    private val frames = FrameCollection()
    private val frameOrigin = MutablePoint2f()

    private val turnProps = object: TurnTowardsCamera.Delegate() {
        override var rotationPhi = spawnPt.rotation
    }

    private val turn = TurnTowardsCamera(turnProps, keepUpright = true)

    override val body = FixedCylinderBody(
        "StaticPaper",
        pos, // shared mutable object
        elasticity = 0.3f,
        friction = 0.2f,
        gravity = false,
        mass = RigidBody.LARGE_MASS,
        radius = 0.25f,
        height = 1.5f,
        zOffset = 0.75f,
    )

    private val renderProps = object: PaperRenderer.Delegate() {
        override val renderPos get() = this@StaticPaperGel.pos
        override val frameSize get() = frames.frameSize
        override val tex get() = frames.tex
        override val texOffset get() = frames.texOffset
        override val origin get() = this@StaticPaperGel.frameOrigin
        override val rotationPhi get() = turnProps.rotationPhi
        override val rotationRho get() = turnProps.rotationRho
        override val scaleFactor = MutablePoint2f(1.0f / 36, 1.0f / 36)
    }

    override val renderer = App.factory.createPaperRenderer(renderProps)

    override fun onAnimateActive() {
        turn.animate()
    }

    init {
        val off: Int

        when (spawnPt.kind) {
            GNARLED_TREE_LARGE -> {
                frames.setTexture("sprite-static-gnarled-tree-large.png", 1, 1)
                off = 3
            }
            GNARLED_TREE_MEDIUM -> {
                frames.setTexture("sprite-static-gnarled-tree-medium.png", 1, 1)
                off = 2
            }
            GNARLED_TREE_SMALL -> {
                frames.setTexture("sprite-static-gnarled-tree-small.png", 1, 1)
                off = 1
            }
            ROUND_TREE -> {
                frames.setTexture("sprite-static-round-tree.png", 1, 1)
                off = 2
            }
        }
        frameOrigin.set(renderProps.frameSize.x / 2, renderProps.frameSize.y - off)
    }

    override fun onRemoveZombie() {
        renderer.free()
    }
}
