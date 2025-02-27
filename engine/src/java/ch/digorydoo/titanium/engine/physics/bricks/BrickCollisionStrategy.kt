package ch.digorydoo.titanium.engine.physics.bricks

import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.titanium.engine.brick.Brick
import ch.digorydoo.titanium.engine.brick.BrickShape
import ch.digorydoo.titanium.engine.brick.BrickVolume
import ch.digorydoo.titanium.engine.brick.BrickVolume.Companion.WORLD_BRICK_SIZE
import ch.digorydoo.titanium.engine.physics.RigidBody

internal abstract class BrickCollisionStrategy<B: RigidBody> {
    protected enum class BrickGeometryType { NONE, CUBOID }

    protected class BrickGeometry {
        var type = BrickGeometryType.NONE
        val cuboidCentre = MutablePoint3f()
        val cuboidSize = MutablePoint3f()

        fun determine(shape: BrickShape, brickWorldCoords: Point3f) {
            // TODO support more shapes
            when (shape) {
                BrickShape.NONE -> {
                    type = BrickGeometryType.NONE
                }
                else -> {
                    type = BrickGeometryType.CUBOID
                    cuboidCentre.set(
                        brickWorldCoords.x + WORLD_BRICK_SIZE / 2,
                        brickWorldCoords.y + WORLD_BRICK_SIZE / 2,
                        brickWorldCoords.z + WORLD_BRICK_SIZE / 2,
                    )
                    cuboidSize.set(WORLD_BRICK_SIZE, WORLD_BRICK_SIZE, WORLD_BRICK_SIZE)
                }
            }
        }
    }

    abstract fun checkNextPos(
        body: B,
        brickVolume: BrickVolume,
        onHit: (brick: Brick, hitPt: Point3f, hitNormal: Point3f, bounce: () -> Unit) -> Unit,
    )
}
