package ch.digorydoo.titanium.engine.physics

import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.engine.brick.Brick
import ch.digorydoo.titanium.engine.brick.BrickMaterial
import ch.digorydoo.titanium.engine.brick.BrickShape
import ch.digorydoo.titanium.engine.brick.BrickVolume
import ch.digorydoo.titanium.engine.brick.BrickVolume.Companion.WORLD_BRICK_SIZE
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.gel.GraphicElement
import ch.digorydoo.titanium.engine.physics.collision_strategy.CollideCylinderVsCuboid
import ch.digorydoo.titanium.engine.physics.collision_strategy.CollideCylinderVsCylinder
import ch.digorydoo.titanium.engine.physics.collision_strategy.CollideSphereVsCuboid
import ch.digorydoo.titanium.engine.physics.collision_strategy.CollideSphereVsCylinder
import ch.digorydoo.titanium.engine.physics.collision_strategy.CollideSphereVsSphere
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedCuboidBody
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedCylinderBody
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedSphereBody
import ch.digorydoo.titanium.engine.physics.rigid_body.RigidBody
import ch.digorydoo.titanium.engine.physics.rigid_body.RigidBody.Companion.LARGE_MASS

class CollisionManager {
    private var collisionTicket = 0L
    private val hit = MutableHitResult()
    private val hitNormal21 = MutablePoint3f()
    private val brick = Brick()
    private val brickWorldCoords = MutablePoint3f()

    private val sphereVsSphere = CollideSphereVsSphere()
    private val sphereVsCylinder = CollideSphereVsCylinder()
    private val sphereVsCuboid = CollideSphereVsCuboid()
    private val cylinderVsCylinder = CollideCylinderVsCylinder()
    private val cylinderVsCuboid = CollideCylinderVsCuboid()

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
                                    Log.warn(TAG, "Tertiary collision between $gel1 and $stuckWith!")
                                } else {
                                    Log.warn(TAG, "Tertiary collision between $gel1 and a brick!")
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.error(TAG, "Gel $gel1 crashed during collision detection!\n${e.stackTraceToString()}")
                gel1.setZombie()
            }
        }
    }

    private fun areGelsWithinCollisionRange(gel1: GraphicElement, gel2: GraphicElement): Boolean {
        val b1 = gel1.body ?: return false
        val b2 = gel2.body ?: return false

        val dx = b1.nextPos.x - b2.nextPos.x
        val dy = b1.nextPos.y - b2.nextPos.y
        val dz = b1.nextPos.z - b2.nextPos.z
        val dsqr = dx * dx + dy * dy + dz * dz
        val maxDist = b1.enclosingRadius + b2.enclosingRadius + 2 * COLLISION_VICINITY
        return dsqr <= maxDist * maxDist
    }

    private fun handleWithinCollisionRange(gel1: GraphicElement, gel2: GraphicElement): Boolean {
        val body1 = gel1.body ?: return false
        val body2 = gel2.body ?: return false

        // We know that gel1 and gel2 are within collision range, so it should be efficient enough to call
        // shouldBounceOnCollision even though we don't know yet if they actually collide. A gel that is not solid
        // returns false here, so we should only bounce if both return true.
        val bounce = gel1.shouldBounceOnCollision(gel2) && gel2.shouldBounceOnCollision(gel1)

        val didCollide = checkAndBounceIfNeeded(body1, body2, bounce) // sets hit on collision
        if (!didCollide) return false

        val n12 = hit.hitNormal12
        hitNormal21.set(-n12.x, -n12.y, -n12.z)

        gel1.didCollide(gel2, myHit = hit.area1, otherHit = hit.area2, hitPt = hit.hitPt, normalTowardsMe = hitNormal21)
        gel2.didCollide(gel1, myHit = hit.area2, otherHit = hit.area1, hitPt = hit.hitPt, normalTowardsMe = n12)
        return true
    }

    private fun checkAndBounceIfNeeded(body1: RigidBody, body2: RigidBody, bounce: Boolean) =
        when (body1) {
            is FixedSphereBody -> when (body2) {
                is FixedSphereBody -> sphereVsSphere.checkAndBounceIfNeeded(body1, body2, bounce, hit)
                is FixedCylinderBody -> sphereVsCylinder.checkAndBounceIfNeeded(body1, body2, bounce, hit)
                is FixedCuboidBody -> sphereVsCuboid.checkAndBounceIfNeeded(body1, body2, bounce, hit)
            }
            is FixedCylinderBody -> when (body2) {
                is FixedSphereBody -> sphereVsCylinder.checkAndBounceIfNeeded(body2, body1, bounce, hit)
                is FixedCylinderBody -> cylinderVsCylinder.checkAndBounceIfNeeded(body1, body2, bounce, hit)
                is FixedCuboidBody -> cylinderVsCuboid.checkAndBounceIfNeeded(body1, body2, bounce, hit)
            }
            is FixedCuboidBody -> when (body2) {
                is FixedSphereBody -> sphereVsCuboid.checkAndBounceIfNeeded(body2, body1, bounce, hit)
                is FixedCylinderBody -> cylinderVsCuboid.checkAndBounceIfNeeded(body2, body1, bounce, hit)
                is FixedCuboidBody -> throw NotImplementedError() // FIXME
            }
        }

    private fun handle(gel: GraphicElement, brickVolume: BrickVolume): Boolean {
        val body = gel.body ?: return false
        if (body.mass >= LARGE_MASS) return false

        val rangeCentreX = body.nextPos.x
        val rangeCentreY = body.nextPos.y
        val rangeCentreZ = body.nextPos.z
        val rangeRadius = body.enclosingRadius + COLLISION_VICINITY

        val minBrickX: Int = ((rangeCentreX - rangeRadius) / WORLD_BRICK_SIZE).toInt()
        val minBrickY: Int = ((rangeCentreY - rangeRadius) / WORLD_BRICK_SIZE).toInt()
        val minBrickZ: Int = ((rangeCentreZ - rangeRadius) / WORLD_BRICK_SIZE).toInt()

        val maxBrickX: Int = ((rangeCentreX + rangeRadius) / WORLD_BRICK_SIZE).toInt()
        val maxBrickY: Int = ((rangeCentreY + rangeRadius) / WORLD_BRICK_SIZE).toInt()
        val maxBrickZ: Int = ((rangeCentreZ + rangeRadius) / WORLD_BRICK_SIZE).toInt()

        var anyCollisions = false

        for (brickX in minBrickX .. maxBrickX) {
            for (brickY in minBrickY .. maxBrickY) {
                for (brickZ in minBrickZ .. maxBrickZ) {
                    brickVolume.getAtBrickCoord(brickX, brickY, brickZ, brick, outWorldCoords = brickWorldCoords)

                    getRigidBody(brick.shape, brick.material, brickWorldCoords)?.let {
                        if (handleWithinCollisionRange(gel, it, brick.shape, brick.material)) {
                            anyCollisions = true
                        }
                    }
                }
            }
        }

        return anyCollisions
    }

    private fun handleWithinCollisionRange(
        gel: GraphicElement,
        brickBody: RigidBody,
        shape: BrickShape,
        material: BrickMaterial,
    ): Boolean {
        val bodyOfGel = gel.body ?: return false

        val bounce = gel.shouldBounceOnCollision(shape)
        val didCollide = checkAndBounceIfNeeded(bodyOfGel, brickBody, bounce) // sets hit on collision
        if (!didCollide) return false

        val n12 = hit.hitNormal12
        hitNormal21.set(-n12.x, -n12.y, -n12.z)

        gel.didCollide(
            shape,
            material,
            myHit = hit.area1,
            otherHit = hit.area2,
            hitPt = hit.hitPt,
            normalTowardsMe = hitNormal21
        )
        return true
    }

    private fun getRigidBody(shape: BrickShape, material: BrickMaterial, brickWorldCoords: Point3f): RigidBody? {
        if (shape == BrickShape.NONE) return null
        return FixedCuboidBody(
            "Brick($shape, $material, $brickWorldCoords)",
            initialPos = Point3f.zero,
            mass = LARGE_MASS,
            elasticity = material.elasticity,
            friction = material.friction,
            gravity = false,
            sizeX = 0.0f,
            sizeY = 0.0f,
            sizeZ = 0.0f,
        ).apply {
            val x = brickWorldCoords.x + 0.5f * WORLD_BRICK_SIZE
            val y = brickWorldCoords.y + 0.5f * WORLD_BRICK_SIZE
            val z = brickWorldCoords.z + 0.5f * WORLD_BRICK_SIZE
            pos.set(x, y, z)
            nextPos.set(x, y, z)
            setSize(WORLD_BRICK_SIZE, WORLD_BRICK_SIZE, WORLD_BRICK_SIZE)
        }
    }

    companion object {
        private val TAG = Log.Tag("CollisionManager")
        const val COLLISION_VICINITY = 0.25f
    }
}
