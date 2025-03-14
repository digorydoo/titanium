package ch.digorydoo.titanium.engine.physics

import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.engine.brick.BrickVolume
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.gel.GraphicElement
import ch.digorydoo.titanium.engine.physics.collide_bricks.CollideCylinderVsBrick
import ch.digorydoo.titanium.engine.physics.collide_bricks.CollideSphereVsBrick
import ch.digorydoo.titanium.engine.physics.collide_regular.CollideCylinderVsCylinder
import ch.digorydoo.titanium.engine.physics.collide_regular.CollideSphereVsCuboid
import ch.digorydoo.titanium.engine.physics.collide_regular.CollideSphereVsCylinder
import ch.digorydoo.titanium.engine.physics.collide_regular.CollideSphereVsSphere
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedCuboidBody
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedCylinderBody
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedSphereBody
import ch.digorydoo.titanium.engine.physics.rigid_body.RigidBody.Companion.LARGE_MASS

class CollisionManager {
    private var collisionTicket = 0L
    private val hit = MutableHitResult()

    private val sphereVsSphere = CollideSphereVsSphere()
    private val sphereVsCylinder = CollideSphereVsCylinder()
    private val cylinderVsCylinder = CollideCylinderVsCylinder()
    private val sphereVsCuboid = CollideSphereVsCuboid()

    private val sphereVsBrick = CollideSphereVsBrick()
    private val cylinderVsBrick = CollideCylinderVsBrick()

    fun handleCollisions() {
        val bricks = App.content.bricks ?: return // should only happen at scene loading time
        val ticket = ++collisionTicket // get a fresh ticket
        val content = App.content

        content.forEachIndexedGelInCollidableLayer { i, gel1 ->
            try {
                val vicinity1 = gel1.vicinity
                vicinity1.clearIfOutdated(ticket)

                if (gel1.canCollide()) {
                    var anyCollisions = false

                    // We start here from i + 1, because gel1 has already been checked against gels with lower indices.
                    content.forEachIndexedGelInCollidableLayer(i + 1) { _, gel2 ->
                        if (gel2.canCollide()) {
                            if (areGelsWithinCollisionRange(gel1, gel2)) {
                                gel2.vicinity.apply {
                                    clearIfOutdated(ticket)
                                    add(gel1)
                                }
                                vicinity1.add(gel2)

                                if (handleWithinCollisionRange(gel1, gel2)) {
                                    anyCollisions = true
                                }
                            }
                        }
                    }

                    if (handle(gel1, bricks)) {
                        anyCollisions = true
                    }

                    if (anyCollisions) {
                        // The separation of the bodies may have led to secondary collisions with other bodies.
                        anyCollisions = false

                        vicinity1.forEach { gel2 ->
                            if (handleWithinCollisionRange(gel1, gel2)) {
                                anyCollisions = true
                            }
                        }

                        if (handle(gel1, bricks)) {
                            anyCollisions = true
                        }

                        if (anyCollisions) {
                            // Again, the separation may have led to tertiary collisions. This is the last call!
                            anyCollisions = false
                            var stuckWith: GraphicElement? = null

                            vicinity1.forEach { gel2 ->
                                if (handleWithinCollisionRange(gel1, gel2)) {
                                    anyCollisions = true
                                    stuckWith = gel2
                                }
                            }

                            if (handle(gel1, bricks)) {
                                anyCollisions = true
                            }

                            if (anyCollisions) {
                                // There were indeed tertiary collisions. This usually indicates the gel is stuck!
                                if (stuckWith != null) {
                                    Log.warn("Tertiary collision between $gel1 and $stuckWith!")
                                } else {
                                    Log.warn("Tertiary collision between $gel1 and a brick!")
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.error("Gel $gel1 crashed during collision detection!\n${e.stackTraceToString()}")
                gel1.setZombie()
            }
        }
    }

    private fun areGelsWithinCollisionRange(gel1: GraphicElement, gel2: GraphicElement): Boolean {
        val b1 = gel1.body ?: return false
        val b2 = gel2.body ?: return false

        val zOffset1 = when (b1) {
            is FixedSphereBody -> b1.zOffset
            is FixedCylinderBody -> b1.zOffset
            is FixedCuboidBody -> 0.0f
        }
        val zOffset2 = when (b2) {
            is FixedSphereBody -> b2.zOffset
            is FixedCylinderBody -> b2.zOffset
            is FixedCuboidBody -> 0.0f
        }

        val dx = b1.nextPos.x - b2.nextPos.x
        val dy = b1.nextPos.y - b2.nextPos.y
        val dz = (b1.nextPos.z + zOffset1) - (b2.nextPos.z + zOffset2)
        val dsqr = dx * dx + dy * dy + dz * dz
        val maxDist = b1.collisionRadius + b2.collisionRadius
        return dsqr <= maxDist * maxDist
    }

    private fun handleWithinCollisionRange(gel1: GraphicElement, gel2: GraphicElement): Boolean {
        val body1 = gel1.body ?: return false
        val body2 = gel2.body ?: return false

        // We know that gel1 and gel2 are within collision range, so it should be efficient enough to call
        // shouldBounceOnCollision even though we don't know yet if they actually collide. A gel that is not solid
        // returns false here, so we should only bounce if both return true.
        val bounce = gel1.shouldBounceOnCollision(gel2) && gel2.shouldBounceOnCollision(gel1)

        val didCollide = when (body1) {
            is FixedSphereBody -> when (body2) {
                is FixedSphereBody -> sphereVsSphere.checkAndBounceIfNeeded(body1, body2, bounce, hit)
                is FixedCylinderBody -> sphereVsCylinder.checkAndBounceIfNeeded(body1, body2, bounce, hit)
                is FixedCuboidBody -> sphereVsCuboid.checkAndBounceIfNeeded(body1, body2, bounce, hit)
            }
            is FixedCylinderBody -> when (body2) {
                is FixedSphereBody -> sphereVsCylinder.checkAndBounceIfNeeded(body2, body1, bounce, hit)
                is FixedCylinderBody -> cylinderVsCylinder.checkAndBounceIfNeeded(body1, body2, bounce, hit)
                is FixedCuboidBody -> throw NotImplementedError() // FIXME
            }
            is FixedCuboidBody -> when (body2) {
                is FixedSphereBody -> sphereVsCuboid.checkAndBounceIfNeeded(body2, body1, bounce, hit)
                is FixedCylinderBody -> throw NotImplementedError() // FIXME
                is FixedCuboidBody -> throw NotImplementedError() // FIXME
            }
        }

        if (!didCollide) return false
        gel1.didCollide(gel2, myHit = hit.area1, otherHit = hit.area2, hitPt = hit.hitPt)
        gel2.didCollide(gel1, myHit = hit.area2, otherHit = hit.area1, hitPt = hit.hitPt)
        return true
    }

    private fun handle(gel: GraphicElement, brickVolume: BrickVolume): Boolean {
        val body = gel.body ?: return false
        if (body.mass >= LARGE_MASS) return false
        var collision = false

        when (body) {
            is FixedSphereBody -> {
                sphereVsBrick.checkNextPos(body, brickVolume) { brick, hitPt, hitNormal, bounce ->
                    collision = true
                    gel.didCollide(brick, hitPt, hitNormal)
                    bounce()
                }
            }
            is FixedCylinderBody -> {
                cylinderVsBrick.checkNextPos(body, brickVolume) { brick, hitPt, hitNormal, bounce ->
                    collision = true
                    gel.didCollide(brick, hitPt, hitNormal)
                    bounce()
                }
            }
            is FixedCuboidBody -> throw NotImplementedError() // FIXME
        }

        return collision
    }
}
