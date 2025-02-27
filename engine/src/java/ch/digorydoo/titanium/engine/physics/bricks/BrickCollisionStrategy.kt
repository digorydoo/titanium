package ch.digorydoo.titanium.engine.physics.bricks

import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.titanium.engine.brick.Brick
import ch.digorydoo.titanium.engine.brick.BrickVolume
import ch.digorydoo.titanium.engine.physics.RigidBody

internal abstract class BrickCollisionStrategy<B: RigidBody> {
    abstract fun checkNextPos(
        body: B,
        brickVolume: BrickVolume,
        onHit: (brick: Brick, hitPt: Point3f, bounce: () -> Unit) -> Unit,
    )

    companion object {
        @JvmStatic
        protected val EPSILON = 0.000001f // small number to avoid division by almost zero

        @JvmStatic
        protected val BRICK_ELASTICITY = 0.8f // currently the same for all BrickMaterials
    }
}
