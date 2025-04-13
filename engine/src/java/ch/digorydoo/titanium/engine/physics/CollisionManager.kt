package ch.digorydoo.titanium.engine.physics

import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.kutils.point.MutablePoint3i
import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.engine.brick.Brick
import ch.digorydoo.titanium.engine.brick.BrickFaceCoveringRetriever
import ch.digorydoo.titanium.engine.brick.BrickMaterial
import ch.digorydoo.titanium.engine.brick.BrickShape
import ch.digorydoo.titanium.engine.brick.BrickVolume
import ch.digorydoo.titanium.engine.brick.BrickVolume.Companion.WORLD_BRICK_SIZE
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.gel.GraphicElement
import ch.digorydoo.titanium.engine.physics.collision_strategy.*
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedCuboidBody
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedCylinderBody
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedSphereBody
import ch.digorydoo.titanium.engine.physics.rigid_body.RigidBody
import ch.digorydoo.titanium.engine.physics.rigid_body.RigidBody.Companion.LARGE_MASS
import kotlin.math.abs
import kotlin.math.max
import kotlin.random.Random

class CollisionManager {
    private var collisionTicket = 0L
    private val hit = MutableHitResult()
    private val hitNormal21 = MutablePoint3f()
    private val brick = Brick()
    private val brickCoords = MutablePoint3i()
    private val brickWorldCoords = MutablePoint3f()
    private val tmpPos1 = MutablePoint3f()
    private val tmpPos2 = MutablePoint3f()

    private val sphereVsSphere = CollideSphereVsSphere()
    private val sphereVsCylinder = CollideSphereVsCylinder()
    private val sphereVsCuboid = CollideSphereVsCuboid()
    private val cylinderVsCylinder = CollideCylinderVsCylinder()
    private val cylinderVsCuboid = CollideCylinderVsCuboid()

    private var brickFaceCoveringRetriever: BrickFaceCoveringRetriever? = null

    private val involvedGels = mutableSetOf<GraphicElement>() // FIXME inefficient
    private val moreInvolvedGels = mutableSetOf<GraphicElement>()

    fun handleCollisions() {
        val bricks = App.content.bricks ?: return // should only happen at scene loading time
        val content = App.content
        val ticket = ++collisionTicket // get a fresh ticket

        involvedGels.clear()
        moreInvolvedGels.clear()

        content.forEachIndexedGelInCollidableLayer { i, gel1 ->
            val vicinity1 = gel1.vicinity
            vicinity1.clearIfOutdated(ticket)

            if (gel1.canCollide()) {
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
                                involvedGels.add(gel1)
                                involvedGels.add(gel2)
                            }
                        }
                    }
                }

                if (handle(gel1, bricks)) {
                    involvedGels.add(gel1)
                }
            }
        }

        if (involvedGels.isNotEmpty()) {
            // The separation of the bodies may have led to secondary collisions with other bodies.
            var outerIteration = 1

            do {
                var needsOuterRetry = false

                involvedGels.forEach { gel1 ->
                    var innerIteration = 1
                    var rndRange = RANDOMIZE_NEXTPOS
                    val body1 = gel1.body

                    do {
                        var anyCollisions = false
                        var fastestStillInvolved = gel1
                        var speedOfFastest = body1?.speedBeforeCollisions?.maxAbsComponent() ?: 0.0f

                        gel1.vicinity.forEach { gel2 ->
                            if (handleWithinCollisionRange(gel1, gel2)) {
                                if (!involvedGels.contains(gel2)) moreInvolvedGels.add(gel2)
                                anyCollisions = true
                                needsOuterRetry = true

                                val speed = gel2.body?.speedBeforeCollisions?.maxAbsComponent() ?: 0.0f

                                if (speed > speedOfFastest) {
                                    fastestStillInvolved = gel2
                                    speedOfFastest = speed
                                }

                                if (innerIteration > 1) {
                                    // Slightly randomize the position in an attempt to break circular collisions.
                                    randomizeNextPos(gel2, rndRange) // gel1 is handled further below
                                }
                            }
                        }

                        if (handle(gel1, bricks)) {
                            anyCollisions = true
                            needsOuterRetry = true
                        }

                        if (anyCollisions && innerIteration > 1) {
                            randomizeNextPos(gel1, rndRange)

                            // The fastest gets an extra push, because it's likely that it's outside the congestion.
                            randomizeNextPos(fastestStillInvolved, rndRange * 2.0f)
                        }

                        if (!anyCollisions) break
                        rndRange = rndRange * 2.0f
                    } while (++innerIteration <= MAX_NUM_INNER_ITERATIONS)

                    if (innerIteration > MAX_NUM_INNER_ITERATIONS) {
                        Log.warn(TAG, "Too many inner iterations: $gel1, ${body1?.nextPos}")
                        needsOuterRetry = true
                    } else if (innerIteration > MAX_NUM_INNER_ITERATIONS_WITHOUT_WARNING) {
                        Log.warn(TAG, "Took $innerIteration inner iterations: $gel1, ${body1?.nextPos}")
                    }
                }

                if (moreInvolvedGels.isNotEmpty()) {
                    involvedGels.addAll(moreInvolvedGels)
                    moreInvolvedGels.clear()
                    needsOuterRetry = true
                }

                if (!needsOuterRetry) break
            } while (++outerIteration <= MAX_NUM_OUTER_ITERATIONS)

            if (outerIteration > MAX_NUM_OUTER_ITERATIONS) {
                Log.error(TAG, "Failed to handle collisions within a sane amount of iterations")
            } else if (outerIteration > MAX_NUM_OUTER_ITERATIONS_WITHOUT_WARNING) {
                Log.warn(TAG, "Took $outerIteration OUTER iterations")
            }
        }

        // If the following sanity check fails, then COLLISION_VICINITY is probably too small.
        // FIXME This is inefficient and should be removed later.
        content.forEachIndexedGelInCollidableLayer { i, gel1 ->
            if (gel1.canCollide()) {
                content.forEachIndexedGelInCollidableLayer(i + 1) { _, gel2 ->
                    if (gel2.canCollide()) {
                        if (checkCollideAtNextPos(gel1, gel2)) {
                            Log.error(
                                TAG,
                                arrayOf(
                                    "Gels still collided at their nextPos: $gel1, $gel2",
                                    "   gel1 involved: ${involvedGels.contains(gel1)}",
                                    "   gel2 involved: ${involvedGels.contains(gel2)}",
                                    "   gel1 in vicinity: ${gel2.vicinity.contains(gel1)}",
                                    "   gel2 in vicinity: ${gel1.vicinity.contains(gel2)}",
                                ).joinToString("\n")
                            )
                        }
                    }
                }
            }
        }
    }

    private fun randomizeNextPos(gel: GraphicElement, rndRange: Float) {
        val body = gel.body ?: return
        if (body.mass >= LARGE_MASS) return

        body.nextPos.apply {
            x += rndRange * (1.0f - 2.0f * Random.nextFloat())
            y += rndRange * (1.0f - 2.0f * Random.nextFloat())
            z += rndRange * (1.0f - 2.0f * Random.nextFloat())
        }
    }

    private fun checkCollideAtNextPos(gel1: GraphicElement, gel2: GraphicElement): Boolean {
        val body1 = gel1.body ?: return false
        val body2 = gel2.body ?: return false

        fun <B1: RigidBody, B2: RigidBody> ck(
            strategy: CollisionStrategy<B1, B2>,
            b1: B1,
            b2: B2,
        ): Boolean {
            strategy.configure(body1IsBrick = false, body2IsBrick = false, null, null)
            val p1 = b1.nextPos
            val p2 = b2.nextPos
            return strategy.check(b1, p1.x, p1.y, p1.z, b2, p2.x, p2.y, p2.z, hit)
        }

        val collide = when (body1) {
            is FixedSphereBody -> when (body2) {
                is FixedSphereBody -> ck(sphereVsSphere, body1, body2)
                is FixedCylinderBody -> ck(sphereVsCylinder, body1, body2)
                is FixedCuboidBody -> ck(sphereVsCuboid, body1, body2)
            }
            is FixedCylinderBody -> when (body2) {
                is FixedSphereBody -> ck(sphereVsCylinder, body2, body1)
                is FixedCylinderBody -> ck(cylinderVsCylinder, body1, body2)
                is FixedCuboidBody -> ck(cylinderVsCuboid, body1, body2)
            }
            is FixedCuboidBody -> when (body2) {
                is FixedSphereBody -> ck(sphereVsCuboid, body2, body1)
                is FixedCylinderBody -> ck(cylinderVsCuboid, body2, body1)
                is FixedCuboidBody -> throw NotImplementedError() // FIXME
            }
        }

        return collide
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

        tmpPos1.set(body1.nextPos)
        tmpPos2.set(body2.nextPos)

        // We know that gel1 and gel2 are within collision range, so it should be efficient enough to call
        // shouldBounceOnCollision even though we don't know yet if they actually collide. A gel that is not solid
        // returns false here, so we should only bounce if both return true.
        val bounce = gel1.shouldBounceOnCollision(gel2) && gel2.shouldBounceOnCollision(gel1)

        val didCollide = checkAndBounceIfNeeded(
            body1 = body1,
            body2 = body2,
            body2IsBrick = false,
            bounce = bounce,
        )
        if (!didCollide) return false

        val dx1 = abs(body1.nextPos.x - tmpPos1.x)
        val dy1 = abs(body1.nextPos.y - tmpPos1.y)
        val dz1 = abs(body1.nextPos.z - tmpPos1.z)
        val maxAbs1 = max(dx1, max(dy1, dz1))

        val dx2 = abs(body2.nextPos.x - tmpPos2.x)
        val dy2 = abs(body2.nextPos.y - tmpPos2.y)
        val dz2 = abs(body2.nextPos.z - tmpPos2.z)
        val maxAbs2 = max(dx2, max(dy2, dz2))

        if (maxAbs1 > SEPARATION_CHECK_THRESHOLD || maxAbs2 > SEPARATION_CHECK_THRESHOLD) {
            Log.warn(
                TAG,
                arrayOf(
                    "Gel separation moved bodies too far!",
                    "   $gel1",
                    "      body.pos=${body1.pos}",
                    "      body.speedBeforeC=${body1.speedBeforeCollisions}",
                    "      posBefore=$tmpPos1",
                    "      moved=$maxAbs1",
                    "      body.nextPos=${body1.nextPos}",
                    "   $gel2",
                    "      body.pos=${body2.pos}",
                    "      body.speedBeforeC=${body2.speedBeforeCollisions}",
                    "      posBefore=$tmpPos2",
                    "      moved=$maxAbs2",
                    "      body.nextPos=${body2.nextPos}",
                ).joinToString("\n")
            )
        }

        val n12 = hit.hitNormal12
        hitNormal21.set(-n12.x, -n12.y, -n12.z)

        gel1.didCollide(gel2, myHit = hit.area1, otherHit = hit.area2, hitPt = hit.hitPt, normalTowardsMe = hitNormal21)
        gel2.didCollide(gel1, myHit = hit.area2, otherHit = hit.area1, hitPt = hit.hitPt, normalTowardsMe = n12)
        return true
    }

    private fun checkAndBounceIfNeeded(
        body1: RigidBody,
        body2: RigidBody,
        body2IsBrick: Boolean,
        bounce: Boolean,
    ): Boolean {
        fun <B1: RigidBody, B2: RigidBody> ck(
            strategy: CollisionStrategy<B1, B2>,
            b1: B1,
            b2: B2,
            b1IsBrick: Boolean = false,
            b2IsBrick: Boolean = false,
        ): Boolean {
            val bfcr = brickFaceCoveringRetriever?.takeIf { it.volume == App.bricks }
                ?: BrickFaceCoveringRetriever(App.bricks).also { brickFaceCoveringRetriever = it }

            if (b1IsBrick || b2IsBrick) {
                strategy.configure(body1IsBrick = b1IsBrick, body2IsBrick = b2IsBrick, bfcr, brickCoords)
            } else {
                strategy.configure(body1IsBrick = false, body2IsBrick = false, null, null)
            }
            val p1 = b1.nextPos
            val p2 = b2.nextPos
            val didCollide = strategy.check(b1, p1.x, p1.y, p1.z, b2, p2.x, p2.y, p2.z, hit)
            if (didCollide && bounce) strategy.bounce(b1, b2, hit)
            return didCollide
        }

        return when (body1) {
            is FixedSphereBody -> when (body2) {
                is FixedSphereBody -> ck(sphereVsSphere, body1, body2, b2IsBrick = body2IsBrick)
                is FixedCylinderBody -> ck(sphereVsCylinder, body1, body2, b2IsBrick = body2IsBrick)
                is FixedCuboidBody -> ck(sphereVsCuboid, body1, body2, b2IsBrick = body2IsBrick)
            }
            is FixedCylinderBody -> when (body2) {
                is FixedSphereBody -> ck(sphereVsCylinder, body2, body1, b1IsBrick = body2IsBrick)
                is FixedCylinderBody -> ck(cylinderVsCylinder, body1, body2, b2IsBrick = body2IsBrick)
                is FixedCuboidBody -> ck(cylinderVsCuboid, body1, body2, b2IsBrick = body2IsBrick)
            }
            is FixedCuboidBody -> when (body2) {
                is FixedSphereBody -> ck(sphereVsCuboid, body2, body1, b1IsBrick = body2IsBrick)
                is FixedCylinderBody -> ck(cylinderVsCuboid, body2, body1, b1IsBrick = body2IsBrick)
                is FixedCuboidBody -> throw NotImplementedError() // FIXME
            }
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
                    brickCoords.set(brickX, brickY, brickZ)
                    brickVolume.getAtBrickCoord(brickCoords, brick, outWorldCoords = brickWorldCoords)

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

        tmpPos1.set(bodyOfGel.nextPos)
        tmpPos2.set(brickBody.nextPos)

        val bounce = gel.shouldBounceOnCollision(shape)

        val didCollide = checkAndBounceIfNeeded(
            body1 = bodyOfGel,
            body2 = brickBody,
            body2IsBrick = true,
            bounce = bounce,
        )
        if (!didCollide) return false

        val dx1 = abs(bodyOfGel.nextPos.x - tmpPos1.x)
        val dy1 = abs(bodyOfGel.nextPos.y - tmpPos1.y)
        val dz1 = abs(bodyOfGel.nextPos.z - tmpPos1.z)
        val maxAbs1 = max(dx1, max(dy1, dz1))

        val dx2 = abs(brickBody.nextPos.x - tmpPos2.x)
        val dy2 = abs(brickBody.nextPos.y - tmpPos2.y)
        val dz2 = abs(brickBody.nextPos.z - tmpPos2.z)
        val maxAbs2 = max(dx2, max(dy2, dz2))

        if (maxAbs1 > SEPARATION_CHECK_THRESHOLD || maxAbs2 > 0.0f) {
            Log.warn(
                TAG,
                arrayOf(
                    "Brick separation moved bodies too far!",
                    "   $gel",
                    "      body.pos=${bodyOfGel.pos}",
                    "      body.speedBeforeC=${bodyOfGel.speedBeforeCollisions}",
                    "      posBefore=$tmpPos1",
                    "      moved=$maxAbs1",
                    "      body.nextPos=${bodyOfGel.nextPos}",
                    "   Brick($shape, $material)",
                    "      body.pos=${brickBody.pos}",
                    "      body.speedBeforeC=${brickBody.speedBeforeCollisions}",
                    "      posBefore=$tmpPos2",
                    "      moved=$maxAbs2",
                    "      body.nextPos=${brickBody.nextPos}",
                ).joinToString("\n")
            )
        }

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
        private const val COLLISION_VICINITY = 0.35f
        private const val MAX_NUM_OUTER_ITERATIONS = 60
        private const val MAX_NUM_OUTER_ITERATIONS_WITHOUT_WARNING = 40
        private const val MAX_NUM_INNER_ITERATIONS = 42
        private const val MAX_NUM_INNER_ITERATIONS_WITHOUT_WARNING = 20
        private const val RANDOMIZE_NEXTPOS = 0.00000000001f // 1 mm/100000000, will be ~0.01m on the 30th iteration
        private const val SEPARATION_CHECK_THRESHOLD = 0.2f
    }
}
