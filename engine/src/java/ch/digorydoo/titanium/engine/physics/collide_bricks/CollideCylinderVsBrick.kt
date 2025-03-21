package ch.digorydoo.titanium.engine.physics.collide_bricks

import ch.digorydoo.kutils.math.clamp
import ch.digorydoo.kutils.point.MutablePoint2f
import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.kutils.point.Point2f
import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.kutils.point.Point3i
import ch.digorydoo.titanium.engine.brick.Brick
import ch.digorydoo.titanium.engine.brick.BrickVolume
import ch.digorydoo.titanium.engine.brick.BrickVolume.BrickFaceCovering
import ch.digorydoo.titanium.engine.brick.BrickVolume.Companion.WORLD_BRICK_SIZE
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedCylinderBody
import ch.digorydoo.titanium.engine.physics.rigid_body.RigidBody.Companion.LARGE_MASS
import ch.digorydoo.titanium.engine.utils.Direction
import ch.digorydoo.titanium.engine.utils.EPSILON
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

internal class CollideCylinderVsBrick: BrickCollisionStrategy<FixedCylinderBody>() {
    enum class ResultState { NO_HIT, HIT, TENTATIVE }

    private val hitPt = MutablePoint3f()
    private val hitNormal = MutablePoint3f()
    private val brick = Brick()
    private val brickWorldCoords = MutablePoint3f()
    private val geometry = BrickGeometry()
    private val faceCentreToCylinderCentre2D = MutablePoint2f()
    private val closestPtOnPlane = MutablePoint3f()
    private var vertical = false

    override fun checkNextPos(
        body: FixedCylinderBody,
        brickVolume: BrickVolume,
        onHit: (brick: Brick, hitPt: Point3f, hitNormal: Point3f, bounce: () -> Unit) -> Unit,
    ) {
        val cx = body.nextPos.x
        val cy = body.nextPos.y
        val cz = body.nextPos.z
        val r = body.radius
        val h = body.height

        val minBrickX: Int = ((cx - r) / WORLD_BRICK_SIZE).toInt()
        val minBrickY: Int = ((cy - r) / WORLD_BRICK_SIZE).toInt()
        val minBrickZ: Int = ((cz - h / 2) / WORLD_BRICK_SIZE).toInt()

        val maxBrickX: Int = ((cx + r) / WORLD_BRICK_SIZE).toInt()
        val maxBrickY: Int = ((cy + r) / WORLD_BRICK_SIZE).toInt()
        val maxBrickZ: Int = ((cz + h / 2) / WORLD_BRICK_SIZE).toInt()

        for (brickX in minBrickX .. maxBrickX) {
            for (brickY in minBrickY .. maxBrickY) {
                for (brickZ in minBrickZ .. maxBrickZ) {
                    brickVolume.getAtBrickCoord(brickX, brickY, brickZ, brick, outWorldCoords = brickWorldCoords)
                    geometry.determine(brick.shape, brickWorldCoords)

                    // Don't pass cx, cy, cz down, because nextPos may have changed!
                    if (checkNextPos(body, brickVolume, brickX, brickY, brickZ)) {
                        val bounce = { bounce(body) }
                        onHit(brick, hitPt, hitNormal, bounce)
                    }
                }
            }
        }
    }

    private fun checkNextPos(
        body: FixedCylinderBody,
        brickVolume: BrickVolume,
        brickX: Int,
        brickY: Int,
        brickZ: Int,
    ): Boolean = when (geometry.type) {
        BrickGeometryType.NONE -> false
        BrickGeometryType.CUBOID -> checkCuboid(
            body.nextPos.x,
            body.nextPos.y,
            body.nextPos.z,
            body.radius,
            body.height,
            body.nextSpeed,
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
        // Cylinder
        cx: Float,
        cy: Float,
        cz: Float,
        r: Float,
        h: Float,
        speed: Point3f,
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
        vertical = false

        val cuboidHalfSizeX = cuboidSizeX / 2.0f
        val cuboidHalfSizeY = cuboidSizeY / 2.0f
        val cuboidHalfSizeZ = cuboidSizeZ / 2.0f
        val cylinderHalfHeight = h / 2.0f

        val minTop = min(cuboidCentreZ + cuboidHalfSizeZ, cz + cylinderHalfHeight)
        val maxBottom = max(cuboidCentreZ - cuboidHalfSizeZ, cz - cylinderHalfHeight)
        val overlapHeight = minTop - maxBottom

        if (overlapHeight <= 0) {
            return false // the cylinder is completely above or below the brick
        }

        val overlapZ = maxBottom + overlapHeight * 0.5f

        val isCircleContainedInCuboid2D = (cuboidCentreX - cuboidHalfSizeX <= cx - r) &&
            (cuboidCentreX + cuboidHalfSizeX >= cx + r) &&
            (cuboidCentreY - cuboidHalfSizeY <= cy - r) &&
            (cuboidCentreY + cuboidHalfSizeY >= cy + r)

        if (isCircleContainedInCuboid2D) {
            // None of the 2D sides are going to see a hit. Since we already know that there is a z overlap, this must
            // be a vertical hit!
            vertical = true
            if (cz < overlapZ) {
                // The cylinder's top collided with the brick's bottom!
                hitPt.set(cx, cy, cuboidCentreZ - cuboidHalfSizeZ)
                hitNormal.set(Direction.downVector)
            } else {
                // The cylinder's bottom collided with the brick's top!
                hitPt.set(cx, cy, cuboidCentreZ + cuboidHalfSizeZ)
                hitNormal.set(Direction.upVector)
            }
            return true
        }

        val rsqr = r * r
        var result = ResultState.NO_HIT

        fun check2DSide(normal: Point2f): Boolean {
            // Find the centre on the face described by the normal, and subtract it from the cylinder's centre.
            faceCentreToCylinderCentre2D.set(
                cx - (cuboidCentreX + normal.x * cuboidHalfSizeX),
                cy - (cuboidCentreY + normal.y * cuboidHalfSizeY),
            )

            // Compute the projected distance between the cylinder's centre and the face.
            val d = faceCentreToCylinderCentre2D.dotProduct(normal)

            // If d > r, the circle is on the front side of the face, and is farther than r away. This means that the
            // cylinder cannot possibly collide with any of the other faces either.
            if (d > r) return false

            // If d >= -r, the cylinder collides with our face.
            if (d >= -r) {
                // Compute the closest point on the plane of the cuboid face at the z of the centre of z-overlap.
                closestPtOnPlane.set(
                    cx - d * normal.x,
                    cy - d * normal.y,
                    overlapZ,
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

                    if (closestPtOnPlane.x < cuboidCentreX - cuboidHalfSizeX) {
                        tentative = hasValidNeighbour()
                    } else if (closestPtOnPlane.x > cuboidCentreX + cuboidHalfSizeX) {
                        tentative = hasValidNeighbour()
                    } else if (closestPtOnPlane.y < cuboidCentreY - cuboidHalfSizeY) {
                        tentative = hasValidNeighbour()
                    } else if (closestPtOnPlane.y > cuboidCentreY + cuboidHalfSizeY) {
                        tentative = hasValidNeighbour()
                    } else if (closestPtOnPlane.z < cuboidCentreZ - cuboidHalfSizeZ) {
                        tentative = hasValidNeighbour()
                    } else if (closestPtOnPlane.z > cuboidCentreZ + cuboidHalfSizeZ) {
                        tentative = hasValidNeighbour()
                    } else {
                        tentative = false
                    }
                }

                // Clamp the point to the extent of the cuboid.
                closestPtOnPlane.set(
                    clamp(closestPtOnPlane.x, cuboidCentreX - cuboidHalfSizeX, cuboidCentreX + cuboidHalfSizeX),
                    clamp(closestPtOnPlane.y, cuboidCentreY - cuboidHalfSizeY, cuboidCentreY + cuboidHalfSizeY),
                    clamp(closestPtOnPlane.z, cuboidCentreZ - cuboidHalfSizeZ, cuboidCentreZ + cuboidHalfSizeZ),
                )

                // Check if the clamped 2D point actually collides with the cylinder's 2D circle.
                val dx = closestPtOnPlane.x - cx
                val dy = closestPtOnPlane.y - cy
                val dsqr = dx * dx + dy * dy

                if (dsqr <= rsqr) {
                    // If this hit is "tentative", take it only if the hitPt we've found so far is also still tentative.
                    if (tentative) {
                        if (result != ResultState.HIT) {
                            result = ResultState.TENTATIVE
                            hitPt.set(closestPtOnPlane)
                            hitNormal.set(normal.x, normal.y, 0.0f)
                        }
                    } else {
                        result = ResultState.HIT
                        hitPt.set(closestPtOnPlane)
                        hitNormal.set(normal.x, normal.y, 0.0f)
                    }
                }
            }

            // If d <= -r, the sphere is completely on the other side of this face. But it remains unclear whether it
            // is completely on the other side of the brick, or if it collides with another face of the brick. We return
            // true here to indicate that the check needs to go on.
            return true
        }

        if (!check2DSide(Direction.northVector2D)) return false
        if (!check2DSide(Direction.eastVector2D)) return false
        if (!check2DSide(Direction.southVector2D)) return false
        if (!check2DSide(Direction.westVector2D)) return false
        if (result == ResultState.NO_HIT) return false

        if (overlapHeight < VERTICAL_HIT_OVERLAP_HEIGHT_THRESHOLD) {
            if (cz < overlapZ) {
                if (speed.z > 0.0f) {
                    // The cylinder's top collided with the brick's bottom!
                    vertical = true
                    hitPt.z = cuboidCentreZ - cuboidHalfSizeZ
                    hitNormal.set(Direction.downVector)
                }
            } else {
                if (speed.z < 0.0f) {
                    // The cylinder's bottom collided with the brick's top!
                    vertical = true
                    hitPt.z = cuboidCentreZ + cuboidHalfSizeZ
                    hitNormal.set(Direction.upVector)
                }
            }
        }

        return true
    }

    private fun bounce(body: FixedCylinderBody) {
        if (vertical) {
            if (hitNormal.z < 0.0f) {
                // The hitPt.z is the brick's bottom
                body.nextPos.z = hitPt.z - body.height / 2 - EPSILON
            } else {
                // The hitPt.z is the brick's top
                body.nextPos.z = hitPt.z + body.height / 2 + EPSILON
            }
        } else {
            // The hitPt.xy is somewhere on the XY boundary of the brick
            val dx = body.nextPos.x - hitPt.x
            val dy = body.nextPos.y - hitPt.y
            val distance = sqrt(dx * dx + dy * dy)

            if (distance > EPSILON) {
                body.nextPos.x = hitPt.x + (body.radius + EPSILON) * dx / distance
                body.nextPos.y = hitPt.y + (body.radius + EPSILON) * dy / distance
            }
        }

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

    companion object {
        private const val VERTICAL_HIT_OVERLAP_HEIGHT_THRESHOLD = 0.05f
    }
}
