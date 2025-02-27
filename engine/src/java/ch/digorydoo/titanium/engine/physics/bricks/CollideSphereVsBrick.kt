package ch.digorydoo.titanium.engine.physics.bricks

import ch.digorydoo.kutils.math.clamp
import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.titanium.engine.brick.Brick
import ch.digorydoo.titanium.engine.brick.Brick.Companion.WORLD_BRICK_SIZE
import ch.digorydoo.titanium.engine.brick.BrickVolume
import ch.digorydoo.titanium.engine.physics.FixedSphereBody
import ch.digorydoo.titanium.engine.utils.Direction
import ch.digorydoo.titanium.engine.utils.EPSILON
import kotlin.math.sqrt

internal class CollideSphereVsBrick: BrickCollisionStrategy<FixedSphereBody>() {
    enum class ResultState { NO_HIT, HIT, TENTATIVE }

    private val hitPt = MutablePoint3f()
    private val hitNormal = MutablePoint3f()
    private val brick = Brick()
    private val brickWorldCoords = MutablePoint3f()
    private val cuboidCentre = MutablePoint3f()
    private val cuboidSize = MutablePoint3f()
    private val faceCentreToSphereCentre = MutablePoint3f()
    private val closestPtOnPlane = MutablePoint3f()

    override fun checkNextPos(
        body: FixedSphereBody,
        brickVolume: BrickVolume,
        onHit: (brick: Brick, hitPt: Point3f, bounce: () -> Unit) -> Unit,
    ) {
        val cx = body.nextPos.x
        val cy = body.nextPos.y
        val cz = body.nextPos.z + body.zOffset
        val r = body.radius

        val minBrickX: Int = ((cx - r) / WORLD_BRICK_SIZE).toInt()
        val minBrickY: Int = ((cy - r) / WORLD_BRICK_SIZE).toInt()
        val minBrickZ: Int = ((cz - r) / WORLD_BRICK_SIZE).toInt()

        val maxBrickX: Int = ((cx + r) / WORLD_BRICK_SIZE).toInt()
        val maxBrickY: Int = ((cy + r) / WORLD_BRICK_SIZE).toInt()
        val maxBrickZ: Int = ((cz + r) / WORLD_BRICK_SIZE).toInt()

        for (brickX in minBrickX .. maxBrickX) {
            for (brickY in minBrickY .. maxBrickY) {
                for (brickZ in minBrickZ .. maxBrickZ) {
                    brickVolume.getAtBrickCoord(brickX, brickY, brickZ, brick, outWorldCoords = brickWorldCoords)

                    if (brick.isValid()) {
                        findCuboid(brickWorldCoords)

                        if (checkAgainstCuboid(cx, cy, cz, r, brickVolume, brickX, brickY, brickZ)) {
                            val bounce = { bounce(body) }
                            onHit(brick, hitPt, bounce)
                        }
                    }
                }
            }
        }
    }

    // TODO: change according to brick.shape
    private fun findCuboid(origin: Point3f) {
        cuboidCentre.set(
            origin.x + WORLD_BRICK_SIZE / 2,
            origin.y + WORLD_BRICK_SIZE / 2,
            origin.z + WORLD_BRICK_SIZE / 2,
        )
        cuboidSize.set(WORLD_BRICK_SIZE, WORLD_BRICK_SIZE, WORLD_BRICK_SIZE)
    }

    private fun checkAgainstCuboid(
        cx: Float,
        cy: Float,
        cz: Float,
        r: Float,
        brickVolume: BrickVolume,
        brickX: Int,
        brickY: Int,
        brickZ: Int,
    ): Boolean {
        var result = ResultState.NO_HIT
        val rsqr = r * r
        val halfSizeX = cuboidSize.x / 2.0f
        val halfSizeY = cuboidSize.y / 2.0f
        val halfSizeZ = cuboidSize.z / 2.0f

        fun check(normal: Point3f): Boolean {
            // Find the centre on the face described by the normal, and subtract it from the sphere's centre.
            faceCentreToSphereCentre.set(
                cx - (cuboidCentre.x + normal.x * halfSizeX),
                cy - (cuboidCentre.y + normal.y * halfSizeY),
                cz - (cuboidCentre.z + normal.z * halfSizeZ),
            )

            // Compute the projected distance between the sphere's centre and the face.
            val d = faceCentreToSphereCentre.dotProduct(normal)

            // If d > r, the sphere is on the front side of the face, and is farther than r away. This means that the
            // sphere cannot possibly collide with any of the other faces either.
            if (d > r) return false

            // If d >= -r, the sphere collides with our face.
            if (d >= -r) {
                // Compute the closest point on the plane of the cuboid face, and clamp it to the extent of the cuboid.
                closestPtOnPlane.set(
                    cx - d * normal.x,
                    cy - d * normal.y,
                    cz - d * normal.z,
                )

                // Determine whether this hit is "tentative", i.e. there may be a better solution yet.
                val tentative: Boolean

                if (d < 0.0f) {
                    // The sphere's centre is behind the cuboid face. There may be a more directly colliding face.
                    tentative = true
                } else {
                    // If closestPtOnPlane is outside the cuboid, check if there is a brick in the dir of the normal.

                    fun hasValidNeighbour(): Boolean {
                        val neighbourX = (brickX + normal.x).toInt()
                        val neighbourY = (brickY + normal.y).toInt()
                        val neighbourZ = (brickZ + normal.z).toInt()

                        if (brickX == neighbourX && brickY == neighbourY && brickZ == neighbourZ) {
                            // This shouldn't happen, since normal should always point in the direction of an axis
                            return false
                        } else {
                            return brickVolume.hasValidBrickAt(neighbourX, neighbourY, neighbourZ)
                        }
                    }

                    if (closestPtOnPlane.x < cuboidCentre.x - halfSizeX) {
                        tentative = hasValidNeighbour()
                    } else if (closestPtOnPlane.x > cuboidCentre.x + halfSizeX) {
                        tentative = hasValidNeighbour()
                    } else if (closestPtOnPlane.y < cuboidCentre.y - halfSizeY) {
                        tentative = hasValidNeighbour()
                    } else if (closestPtOnPlane.y > cuboidCentre.y + halfSizeY) {
                        tentative = hasValidNeighbour()
                    } else if (closestPtOnPlane.z < cuboidCentre.z - halfSizeZ) {
                        tentative = hasValidNeighbour()
                    } else if (closestPtOnPlane.z > cuboidCentre.z + halfSizeZ) {
                        tentative = hasValidNeighbour()
                    } else {
                        tentative = false
                    }
                }

                // Clamp the point to the extent of the cuboid.
                closestPtOnPlane.set(
                    clamp(closestPtOnPlane.x, cuboidCentre.x - halfSizeX, cuboidCentre.x + halfSizeX),
                    clamp(closestPtOnPlane.y, cuboidCentre.y - halfSizeY, cuboidCentre.y + halfSizeY),
                    clamp(closestPtOnPlane.z, cuboidCentre.z - halfSizeZ, cuboidCentre.z + halfSizeZ),
                )

                // Check if the clamped point actually collides with the sphere.
                val dx = closestPtOnPlane.x - cx
                val dy = closestPtOnPlane.y - cy
                val dz = closestPtOnPlane.z - cz
                val dsqr = dx * dx + dy * dy + dz * dz

                if (dsqr <= rsqr) {
                    // If this hit is "tentative", take it only if the hitPt we've found so far is also still tentative.
                    if (tentative) {
                        if (result != ResultState.HIT) {
                            result = ResultState.TENTATIVE
                            hitPt.set(closestPtOnPlane)
                            hitNormal.set(normal)
                        }
                    } else {
                        result = ResultState.HIT
                        hitPt.set(closestPtOnPlane)
                        hitNormal.set(normal)
                    }
                }
            }

            // If d <= -r, the sphere is completely on the other side of this face. But it remains unclear whether it
            // is completely on the other side of the brick, or if it collides with another face of the brick. We return
            // true here to indicate that the check needs to go on.
            return true
        }

        if (!check(Direction.northVector)) return false
        if (!check(Direction.eastVector)) return false
        if (!check(Direction.southVector)) return false
        if (!check(Direction.westVector)) return false
        if (!check(Direction.upVector)) return false
        if (!check(Direction.downVector)) return false
        return result != ResultState.NO_HIT
    }

    private fun bounce(body: FixedSphereBody) {
        val dx = body.nextPos.x - hitPt.x
        val dy = body.nextPos.y - hitPt.y
        val dz = (body.nextPos.z + body.zOffset) - hitPt.z
        val distance = sqrt(dx * dx + dy * dy + dz * dz)
        if (distance <= EPSILON) return // we can't tell a clear distance

        body.nextPos.x = hitPt.x + body.radius * dx / distance
        body.nextPos.y = hitPt.y + body.radius * dy / distance
        body.nextPos.z = hitPt.z + body.radius * dz / distance - body.zOffset

        val e = body.elasticity * brick.material.elasticity
        val n = hitNormal
        val v1 = body.nextSpeed
        val v1n = v1.dotProduct(n)

        if (v1n >= 0.0f) {
            return // nextSpeed already looks away from the face of the cuboid
        }

        val v1parallel = n * v1n
        v1.set(v1 - v1parallel - v1parallel * e)
    }
}
