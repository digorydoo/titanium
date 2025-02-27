package ch.digorydoo.titanium.engine.brick

import ch.digorydoo.kutils.geometry.CheckRayIntersectionResult
import ch.digorydoo.kutils.geometry.checkRayIntersectsTriangle
import ch.digorydoo.kutils.math.lerp
import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.titanium.engine.brick.BrickVolume.Companion.WORLD_BRICK_SIZE
import kotlin.math.floor

/**
 * This class implements a way how to send a ray through a BrickVolume. The Camera uses it to determine its brick
 * surroundings, for instance.
 */
class BrickWalker(private val volume: BrickVolume) {
    private val tempBrick = Brick()

    fun forEachBrickOnWorldLine(
        worldStartPt: Point3f,
        worldEndPt: Point3f,
        lambda: (brick: Brick, worldPtOfBrickEntry: Point3f, rayVector: Point3f) -> Boolean,
    ) {
        val brickOrigin = MutablePoint3f(
            floor(worldStartPt.x / WORLD_BRICK_SIZE) * WORLD_BRICK_SIZE,
            floor(worldStartPt.y / WORLD_BRICK_SIZE) * WORLD_BRICK_SIZE,
            floor(worldStartPt.z / WORLD_BRICK_SIZE) * WORLD_BRICK_SIZE,
        )

        val worldPtInsideBrick = MutablePoint3f(brickOrigin).add(
            WORLD_BRICK_SIZE * 0.5f,
            WORLD_BRICK_SIZE * 0.5f,
            WORLD_BRICK_SIZE * 0.5f,
        )

        // When worldStartPt is on the side of a brick, we may fail to find an intersection of the ray with the start
        // brick due to floating point inaccuracy. To avoid this, we move the start slightly towards the brick centre.

        val newStartPt = Point3f(
            lerp(worldStartPt.x, worldPtInsideBrick.x, 0.001f),
            lerp(worldStartPt.y, worldPtInsideBrick.y, 0.001f),
            lerp(worldStartPt.z, worldPtInsideBrick.z, 0.001f),
        )

        val delta = worldEndPt - newStartPt
        val maxRayLength = delta.length()
        val rayDir = delta.newNormalized()
        val intersection = MutablePoint3f()

        while (findFarBrickSideOnRay(newStartPt, rayDir, brickOrigin, maxRayLength, intersection)) {
            // The intersection pt is expected to be on a side of the brick. Again, move it slightly into the brick
            // centre to avoid problems due to floating point inaccuracy.

            intersection.x = lerp(intersection.x, brickOrigin.x + WORLD_BRICK_SIZE * 0.5f, 0.001f)
            intersection.y = lerp(intersection.y, brickOrigin.y + WORLD_BRICK_SIZE * 0.5f, 0.001f)
            intersection.z = lerp(intersection.z, brickOrigin.z + WORLD_BRICK_SIZE * 0.5f, 0.001f)

            // Find the next brick by moving along the ray. Move a short distance only, otherwise we might pass through
            // a brick's corner!

            var move = 0.01f
            var ox: Float
            var oy: Float
            var oz: Float

            while (true) {
                worldPtInsideBrick.set(intersection).addScaled(rayDir, move)

                ox = floor(worldPtInsideBrick.x / WORLD_BRICK_SIZE) * WORLD_BRICK_SIZE
                oy = floor(worldPtInsideBrick.y / WORLD_BRICK_SIZE) * WORLD_BRICK_SIZE
                oz = floor(worldPtInsideBrick.z / WORLD_BRICK_SIZE) * WORLD_BRICK_SIZE

                if (ox != brickOrigin.x || oy != brickOrigin.y || oz != brickOrigin.z) {
                    break
                } else {
                    // Oops, we ended up in the same brick! Try again with a larger move distance!

                    if (move < WORLD_BRICK_SIZE) {
                        move *= 2.0f
                    } else {
                        throw Exception("forEachBrickOnWorldLine ran into an endless loop")
                    }
                }
            }

            if (!volume.isWorldPtInBounds(worldPtInsideBrick)) {
                break // we've crossed the volume boundary
            }

            volume.getAtWorldCoord(worldPtInsideBrick, tempBrick)

            if (!lambda(tempBrick, intersection, rayDir)) {
                break // the lambda told us to stop
            }

            brickOrigin.set(ox, oy, oz)
        }
    }

    private fun findFarBrickSideOnRay(
        rayOrigin: Point3f,
        rayDir: Point3f,
        brickOrigin: Point3f,
        maxRayLength: Float,
        intersection: MutablePoint3f,
    ): Boolean {
        val v0 = MutablePoint3f()
        val v1 = MutablePoint3f()
        val v2 = MutablePoint3f()
        val result = CheckRayIntersectionResult()
        var shortestLength = Double.POSITIVE_INFINITY

        fun check() {
            // To find the far side, the triangle needs to face away from the ray origin
            val normal = (v1.toMutable() - v0).cross(v2 - v0).normalize()
            val isFacingAway = normal.dotProduct(rayDir) > 0.0f

            if (isFacingAway) {
                checkRayIntersectsTriangle(rayOrigin, rayDir, v0, v1, v2, result)

                if (result.intersects && result.rayLength < shortestLength && result.rayLength < maxRayLength) {
                    intersection.set(rayOrigin + rayDir * result.rayLength)
                    shortestLength = result.rayLength
                }
            }
        }

        val px = brickOrigin.x
        val py = brickOrigin.y
        val pz = brickOrigin.z
        val qx = px + WORLD_BRICK_SIZE
        val qy = py + WORLD_BRICK_SIZE
        val qz = pz + WORLD_BRICK_SIZE

        // Downside (XY)

        v0.set(px, py, pz)
        v1.set(px, qy, pz)
        v2.set(qx, py, pz)
        check()

        v0.set(qx, py, pz)
        v1.set(px, qy, pz)
        v2.set(qx, qy, pz)
        check()

        // Upside (XY)

        v0.set(px, py, qz)
        v1.set(qx, py, qz)
        v2.set(px, qy, qz)
        check()

        v0.set(qx, py, qz)
        v1.set(qx, qy, qz)
        v2.set(px, qy, qz)
        check()

        // West (XZ)

        v0.set(px, py, pz)
        v1.set(qx, py, pz)
        v2.set(px, py, qz)
        check()

        v0.set(px, py, qz)
        v1.set(qx, py, pz)
        v2.set(qx, py, qz)
        check()

        // East (XZ)

        v0.set(px, qy, pz)
        v1.set(px, qy, qz)
        v2.set(qx, qy, pz)
        check()

        v0.set(px, qy, qz)
        v1.set(qx, qy, qz)
        v2.set(qx, qy, pz)
        check()

        // North (YZ)

        v0.set(px, py, pz)
        v1.set(px, py, qz)
        v2.set(px, qy, pz)
        check()

        v0.set(px, py, qz)
        v1.set(px, qy, qz)
        v2.set(px, qy, pz)
        check()

        // South (YZ)

        v0.set(qx, py, pz)
        v1.set(qx, qy, pz)
        v2.set(qx, py, qz)
        check()

        v0.set(qx, py, qz)
        v1.set(qx, qy, pz)
        v2.set(qx, qy, qz)
        check()

        return shortestLength < Double.POSITIVE_INFINITY
    }
}
