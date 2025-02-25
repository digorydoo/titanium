package ch.digorydoo.titanium.engine.physics

import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.titanium.engine.brick.Brick
import ch.digorydoo.titanium.engine.brick.BrickVolume

internal abstract class BrickCollisionStrategy<B: RigidBody> {
    abstract fun forEachTouchingNonEmptyBrick(brickVolume: BrickVolume, lambda: (brick: Brick) -> Unit)
    abstract fun check(body: B, brick: Brick, outHitPt: MutablePoint3f): Boolean
    abstract fun bounce(body: B, brick: Brick)
}
