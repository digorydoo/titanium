package ch.digorydoo.titanium.engine.brick

import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.titanium.engine.brick.BrickVolume.Companion.WORLD_BRICK_SIZE

/**
 * A Floor is basically the combination of the properties of a Brick together with the four corners of its floor plane.
 */
class Floor {
    val pt0 = MutablePoint3f()
    val pt1 = MutablePoint3f()
    val pt2 = MutablePoint3f()
    val pt3 = MutablePoint3f()

    val brick = Brick()

    fun set(ix: Int, iy: Int, iz: Int, subvolume: BrickSubvolume, downSide: Boolean = false) {
        subvolume.getBrick(ix, iy, iz, brick, acrossBounds = true)

        val baseX = WORLD_BRICK_SIZE * ix
        val baseY = WORLD_BRICK_SIZE * iy
        var baseZ = WORLD_BRICK_SIZE * iz

        if (!downSide) {
            baseZ += WORLD_BRICK_SIZE
        }

        pt2.x = baseX
        pt2.y = baseY
        pt2.z = baseZ

        pt3.x = baseX
        pt3.y = baseY + WORLD_BRICK_SIZE
        pt3.z = baseZ

        pt0.x = baseX + WORLD_BRICK_SIZE
        pt0.y = baseY
        pt0.z = baseZ

        pt1.x = baseX + WORLD_BRICK_SIZE
        pt1.y = baseY + WORLD_BRICK_SIZE
        pt1.z = baseZ
    }

    override fun toString() =
        "Floor($pt0, $pt1, $pt2, $pt3, ${brick.shape})"
}
