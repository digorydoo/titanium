package ch.digorydoo.titanium.engine.physics

import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.titanium.engine.brick.Brick
import ch.digorydoo.titanium.engine.brick.BrickVolume

internal class CollideSphereVsBrick: BrickCollisionStrategy<FixedSphereBody>() {
    override fun forEachTouchingNonEmptyBrick(brickVolume: BrickVolume, lambda: (Brick) -> Unit) {
        // TODO
    }

    override fun check(body: FixedSphereBody, brick: Brick, outHitPt: MutablePoint3f): Boolean {
        // TODO
        return false
    }

    override fun bounce(body: FixedSphereBody, brick: Brick) {
        // TODO
    }
}
