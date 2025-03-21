package ch.digorydoo.titanium.engine.physics.collide_bricks

import ch.digorydoo.kutils.math.clamp
import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.kutils.point.Point3i
import ch.digorydoo.titanium.engine.brick.Brick
import ch.digorydoo.titanium.engine.brick.BrickVolume
import ch.digorydoo.titanium.engine.brick.BrickVolume.BrickFaceCovering
import ch.digorydoo.titanium.engine.brick.BrickVolume.Companion.WORLD_BRICK_SIZE
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedSphereBody
import ch.digorydoo.titanium.engine.physics.rigid_body.RigidBody.Companion.LARGE_MASS
import ch.digorydoo.titanium.engine.utils.Direction
import ch.digorydoo.titanium.engine.utils.EPSILON
import kotlin.math.sqrt

internal class CollideSphereVsBrick: BrickCollisionStrategy<FixedSphereBody>() {
    enum class ResultState { NO_HIT, HIT, TENTATIVE }

    private val hitPt = MutablePoint3f()
    private val hitNormal = MutablePoint3f()
    private val brick = Brick()
    private val brickWorldCoords = MutablePoint3f()
    private val geometry = BrickGeometry()
    private val faceCentreToSphereCentre = MutablePoint3f()
    private val closestPtOnPlane = MutablePoint3f()

    override fun checkNextPos(
        body: FixedSphereBody,
        brickVolume: BrickVolume,
        onHit: (brick: Brick, hitPt: Point3f, hitNormal: Point3f, bounce: () -> Unit) -> Unit,
    ) {
        val cx = body.nextPos.x
        val cy = body.nextPos.y
        val cz = body.nextPos.z
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
                    geometry.determine(brick.shape, brickWorldCoords)

                    if (check(cx, cy, cz, r, brickVolume, brickX, brickY, brickZ)) {
                        val bounce = { bounce(body) }
                        onHit(brick, hitPt, hitNormal, bounce)
                    }
                }
            }
        }
    }

    private fun check(
        cx: Float,
        cy: Float,
        cz: Float,
        r: Float,
        brickVolume: BrickVolume,
        brickX: Int,
        brickY: Int,
        brickZ: Int,
    ): Boolean = when (geometry.type) {
        BrickGeometryType.NONE -> false
        BrickGeometryType.CUBOID -> checkCuboid(
            cx,
            cy,
            cz,
            r,
            geometry.cuboidCentre.x,
            geometry.cuboidCentre.y,
            geometry.cuboidCentre.z,
            geometry.cuboidSize.x,
            geometry.cuboidSize.y,
            geometry.cuboidSize.z,
            brickVolume,
            brickX,
            brickY,
            brickZ,
        )
    }

    private fun checkCuboid(
        // Sphere
        cx: Float,
        cy: Float,
        cz: Float,
        r: Float,
        // Cuboid
        cuboidCentreX: Float,
        cuboidCentreY: Float,
        cuboidCentreZ: Float,
        cuboidSizeX: Float,
        cuboidSizeY: Float,
        cuboidSizeZ: Float,
        // Brick
        brickVolume: BrickVolume,
        brickX: Int,
        brickY: Int,
        brickZ: Int,
    ): Boolean {
        var result = ResultState.NO_HIT
        val rsqr = r * r
        val halfSizeX = cuboidSizeX / 2.0f
        val halfSizeY = cuboidSizeY / 2.0f
        val halfSizeZ = cuboidSizeZ / 2.0f

        fun check(normal: Point3f): Boolean {
            // Find the centre on the face described by the normal, and subtract it from the sphere's centre.
            faceCentreToSphereCentre.set(
                cx - (cuboidCentreX + normal.x * halfSizeX),
                cy - (cuboidCentreY + normal.y * halfSizeY),
                cz - (cuboidCentreZ + normal.z * halfSizeZ),
            )

            // Compute the projected distance between the sphere's centre and the face.
            val d = faceCentreToSphereCentre.dotProduct(normal)

            // If d > r, the sphere is on the front side of the face, and is farther than r away. This means that the
            // sphere cannot possibly collide with any of the other faces either.
            if (d > r) return false

            // If d >= -r, the sphere collides with our face.
            if (d >= -r) {
                // Compute the closest point on the plane of the cuboid face.
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
                        try {
                            val brickCoords = Point3i(brickX, brickY, brickZ)
                            val normXY = Point3f(normal.x, normal.y, 0.0f)
                            val covering = brickVolume.getBrickFaceCovering(brickCoords, normXY)
                            return when (covering) {
                                BrickFaceCovering.FULLY_COVERED -> true
                                BrickFaceCovering.PARTIALLY_COVERED -> true
                                BrickFaceCovering.NOT_COVERED -> false
                            }
                        } catch (_: Exception) {
                            // We come here if normal.x and normal.y are both 0.
                            return false
                        }
                    }

                    if (closestPtOnPlane.x < cuboidCentreX - halfSizeX) {
                        tentative = hasValidNeighbour()
                    } else if (closestPtOnPlane.x > cuboidCentreX + halfSizeX) {
                        tentative = hasValidNeighbour()
                    } else if (closestPtOnPlane.y < cuboidCentreY - halfSizeY) {
                        tentative = hasValidNeighbour()
                    } else if (closestPtOnPlane.y > cuboidCentreY + halfSizeY) {
                        tentative = hasValidNeighbour()
                    } else if (closestPtOnPlane.z < cuboidCentreZ - halfSizeZ) {
                        tentative = hasValidNeighbour()
                    } else if (closestPtOnPlane.z > cuboidCentreZ + halfSizeZ) {
                        tentative = hasValidNeighbour()
                    } else {
                        tentative = false
                    }
                }

                // Clamp the point to the extent of the cuboid.
                closestPtOnPlane.set(
                    clamp(closestPtOnPlane.x, cuboidCentreX - halfSizeX, cuboidCentreX + halfSizeX),
                    clamp(closestPtOnPlane.y, cuboidCentreY - halfSizeY, cuboidCentreY + halfSizeY),
                    clamp(closestPtOnPlane.z, cuboidCentreZ - halfSizeZ, cuboidCentreZ + halfSizeZ),
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
        val dz = body.nextPos.z - hitPt.z
        val distance = sqrt(dx * dx + dy * dy + dz * dz)
        if (distance <= EPSILON) return // we can't tell a clear distance

        body.nextPos.x = hitPt.x + body.radius * dx / distance
        body.nextPos.y = hitPt.y + body.radius * dy / distance
        body.nextPos.z = hitPt.z + body.radius * dz / distance

        val v1 = body.nextSpeed

        // This may change v1
        applyFriction(
            m1 = body.mass,
            v1 = v1,
            friction = 1.0f - (1.0f - body.friction) * (1.0f - brick.material.friction),
        )

        val elasticity = body.elasticity * brick.material.elasticity

        val normalX = hitNormal.x
        val normalY = hitNormal.y
        val normalZ = hitNormal.z

        val v1dotn = v1.x * normalX + v1.y * normalY + v1.z * normalZ

        if (v1dotn >= 0.0f) {
            return // nextSpeed already looks away from the face of the cuboid
        }

        val v1parallelX = normalX * v1dotn
        val v1parallelY = normalY * v1dotn
        val v1parallelZ = normalZ * v1dotn

        v1.x = v1.x - v1parallelX - v1parallelX * elasticity
        v1.y = v1.y - v1parallelY - v1parallelY * elasticity
        v1.z = v1.z - v1parallelZ - v1parallelZ * elasticity
    }

    private fun applyFriction(m1: Float, v1: MutablePoint3f, friction: Float) {
        if (friction <= 0.0f || m1 >= LARGE_MASS) return

        val normalX = hitNormal.x
        val normalY = hitNormal.y
        val normalZ = hitNormal.z

        val v1dotn = v1.x * normalX + v1.y * normalY + v1.z * normalZ
        val v1parallelX = normalX * v1dotn
        val v1parallelY = normalY * v1dotn
        val v1parallelZ = normalZ * v1dotn

        var v1perpendX = v1.x - v1parallelX
        var v1perpendY = v1.y - v1parallelY
        var v1perpendZ = v1.z - v1parallelZ

        // Since v2 is 0, vperpendDiff is just the length of v1perpend
        val vperpendDiff = sqrt(v1perpendX * v1perpendX + v1perpendY * v1perpendY + v1perpendZ * v1perpendZ)

        val vfricX = v1parallelX * friction
        val vfricY = v1parallelY * friction
        val vfricZ = v1parallelZ * friction
        val vfricLen = sqrt(vfricX * vfricX + vfricY * vfricY + vfricZ * vfricZ)

        if (vfricLen >= vperpendDiff) {
            v1perpendX = 0.0f
            v1perpendY = 0.0f
            v1perpendZ = 0.0f
        } else {
            v1perpendX -= vfricLen * v1perpendX / vperpendDiff
            v1perpendY -= vfricLen * v1perpendY / vperpendDiff
            v1perpendZ -= vfricLen * v1perpendZ / vperpendDiff
        }

        v1.x = v1perpendX + v1parallelX
        v1.y = v1perpendY + v1parallelY
        v1.z = v1perpendZ + v1parallelZ
    }
}
