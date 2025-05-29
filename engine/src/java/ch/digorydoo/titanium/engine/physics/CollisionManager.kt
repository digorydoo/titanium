package ch.digorydoo.titanium.engine.physics

import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.kutils.point.MutablePoint3i
import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.kutils.point.Point3i
import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.BuildConfig
import ch.digorydoo.titanium.engine.brick.Brick
import ch.digorydoo.titanium.engine.brick.BrickMaterial
import ch.digorydoo.titanium.engine.brick.BrickShape
import ch.digorydoo.titanium.engine.brick.BrickVolume
import ch.digorydoo.titanium.engine.brick.BrickVolume.Companion.WORLD_BRICK_SIZE
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.gel.GraphicElement
import ch.digorydoo.titanium.engine.gel.MutableGelSet
import ch.digorydoo.titanium.engine.physics.helper.BrickCollision
import ch.digorydoo.titanium.engine.physics.helper.Collision
import ch.digorydoo.titanium.engine.physics.helper.CollisionSet
import ch.digorydoo.titanium.engine.physics.helper.GelCollision
import ch.digorydoo.titanium.engine.physics.helper.MutableHitResult
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedCuboidBody
import ch.digorydoo.titanium.engine.physics.rigid_body.RigidBody
import ch.digorydoo.titanium.engine.physics.rigid_body.RigidBody.Companion.LARGE_MASS
import kotlin.math.abs
import kotlin.math.max
import kotlin.random.Random

class CollisionManager {
    private sealed class AbortIterationException: Exception()

    private class GelCrashedInInnerLoopError(val gel: GraphicElement, val wrappedError: Exception):
        AbortIterationException()

    private class BodyWasPushedTooFarFromItsOrigPos(val body: RigidBody, val distance: Float, val other: RigidBody):
        AbortIterationException()

    private class BodyWasPushedTooFarRelatively(
        val body: RigidBody,
        val distance: Float,
        val nextPosBefore: Point3f,
        val other: RigidBody,
    ): AbortIterationException()

    private var collisionTicket = 0L
    private val hit = MutableHitResult()
    private val hitNormal21 = MutablePoint3f()
    private val brick = Brick()
    private val brickCoords = MutablePoint3i()
    private val tmpPos1 = MutablePoint3f()
    private val tmpPos2 = MutablePoint3f()
    private val strategies = CollisionStrategiesWrapper()
    private val collisions = CollisionSet()
    private val involvedGels = MutableGelSet()
    private val moreInvolvedGels = MutableGelSet()

    fun handleCollisions() {
        ++collisionTicket // get a fresh ticket
        collisions.clear()
        involvedGels.clear()
        moreInvolvedGels.clear()

        handlePrimaryCollisions()

        if (involvedGels.isNotEmpty()) {
            // The separation of the bodies may have led to secondary collisions.
            handleSecondaryCollisions()
        }

        collisions.forEach { computeNextSpeedAndNotifyGels(it) }

        if (!BuildConfig.isProduction()) {
            sanityCheckAfterAllCollisions()
        }
    }

    private fun handlePrimaryCollisions() {
        val bricks = App.content.bricks
        val content = App.content
        val ticket = collisionTicket

        content.forEachIndexedGelInCollidableLayer { i, gel1 ->
            val vicinity1 = gel1.vicinity
            vicinity1.clearIfOutdated(ticket)

            if (gel1.canCollideWithGels()) {
                // We start here from i + 1, because gel1 has already been checked against gels with lower indices.
                content.forEachIndexedGelInCollidableLayer(i + 1) { _, gel2 ->
                    if (gel2.canCollideWithGels()) {
                        if (areGelsWithinCollisionRange(gel1, gel2)) {
                            val vicinity2 = gel2.vicinity
                            vicinity2.clearIfOutdated(ticket)

                            vicinity1.add(gel2) // MutableGelSet never adds duplicates
                            vicinity2.add(gel1)

                            try {
                                if (checkAndSeparate(gel1, gel2, hit)) {
                                    involvedGels.add(gel1)
                                    involvedGels.add(gel2)
                                }
                            } catch (e: AbortIterationException) {
                                Log.warn(TAG, "The following problem was caught during primary collision phase")
                                recoverFrom(e)
                                involvedGels.add(gel1)
                                involvedGels.add(gel2)
                            }
                        }
                    }
                }
            }

            if (bricks != null && gel1.canCollideWithBricks()) {
                try {
                    if (checkAndSeparate(gel1, bricks)) {
                        involvedGels.add(gel1)
                    }
                } catch (e: AbortIterationException) {
                    Log.warn(TAG, "The following problem was caught during primary collision phase")
                    recoverFrom(e)
                    involvedGels.add(gel1)
                }
            }
        }
    }

    private fun handleSecondaryCollisions() {
        val bricks = App.content.bricks
        var outerIteration = 1

        do {
            var needsOuterRetry = false

            try {
                involvedGels.forEach { gel1 ->
                    try {
                        var innerIteration = 1
                        var rndRange = RANDOMIZE_NEXTPOS
                        val body1 = gel1.body

                        do {
                            var anyCollisions = false
                            var fastestStillInvolved = gel1
                            var speedOfFastest = body1?.speedBeforeCollisions?.maxAbsComponent() ?: 0.0f

                            gel1.vicinity.forEach { gel2 ->
                                if (!BuildConfig.isProduction()) {
                                    // Since gel2 is in the vicinity of gel1, we have already decided that both of them
                                    // can take part in a collision, and so calling canCollideWithGels again is not
                                    // necessary. I check this in development to make sure this is actually true.
                                    require(gel1.canCollideWithGels()) { "gel1 in vicinity, but cannot collide" }
                                    require(gel2.canCollideWithGels()) { "gel2 in vicinity, but cannot collide" }
                                }

                                if (checkAndSeparate(gel1, gel2, hit)) {
                                    if (!involvedGels.contains(gel2)) moreInvolvedGels.add(gel2)

                                    anyCollisions = true
                                    needsOuterRetry = true

                                    val speed = gel2.body?.speedBeforeCollisions?.maxAbsComponent() ?: 0.0f

                                    if (speed > speedOfFastest) {
                                        fastestStillInvolved = gel2
                                        speedOfFastest = speed
                                    }

                                    if (innerIteration > 1) {
                                        // Attempt to improve handling of circular collisions.
                                        randomizeNextPos(gel2, rndRange) // gel1 is handled further below
                                    }
                                }
                            }

                            if (bricks != null && gel1.canCollideWithBricks()) {
                                if (checkAndSeparate(gel1, bricks)) {
                                    anyCollisions = true
                                    needsOuterRetry = true
                                }
                            }

                            if (anyCollisions && innerIteration > 1) {
                                randomizeNextPos(gel1, rndRange)

                                // The fastest gel gets an extra push, because it's likely that it's outside the
                                // congestion.
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
                    } catch (e: AbortIterationException) {
                        // Pass this on to the outer loop.
                        throw e
                    } catch (e: Exception) {
                        // Wrap this in a kind of AbortIterationException so that it's caught from the outer loop.
                        throw GelCrashedInInnerLoopError(gel1, e)
                    }
                }

                if (moreInvolvedGels.isNotEmpty()) {
                    involvedGels.addAll(moreInvolvedGels)
                    moreInvolvedGels.clear()
                    needsOuterRetry = true
                }
            } catch (e: AbortIterationException) {
                Log.warn(TAG, "Iteration #$outerIteration aborted")
                needsOuterRetry = true
                recoverFrom(e)
            }

            if (!needsOuterRetry) break
        } while (++outerIteration <= MAX_NUM_OUTER_ITERATIONS)

        if (outerIteration > MAX_NUM_OUTER_ITERATIONS) {
            Log.error(TAG, "Failed to handle collisions within a sane amount of iterations")
        } else if (outerIteration > MAX_NUM_OUTER_ITERATIONS_WITHOUT_WARNING) {
            Log.warn(TAG, "Took $outerIteration OUTER iterations")
        }
    }

    private fun computeNextSpeedAndNotifyGels(collision: Collision) {
        val n12 = collision.hitNormal12
        hitNormal21.set(-n12.x, -n12.y, -n12.z)

        when (collision) {
            is GelCollision -> {
                val gel1 = collision.gel1
                val gel2 = collision.gel2

                strategies.computeNextSpeed(gel1, gel2, collision)

                try {
                    gel1.didCollide(
                        gel2,
                        myHit = collision.area1,
                        otherHit = collision.area2,
                        hitPt = collision.hitPt,
                        normalTowardsMe = hitNormal21
                    )
                } catch (e: Exception) {
                    Log.error(
                        TAG,
                        "Gel $gel1 crashed in didCollide on colliding with $gel2: " +
                            "${e.message}\n${e.stackTraceToString()}"
                    )
                    gel1.setZombie()
                }

                try {
                    gel2.didCollide(
                        gel1,
                        myHit = collision.area2,
                        otherHit = collision.area1,
                        hitPt = collision.hitPt,
                        normalTowardsMe = n12
                    )
                } catch (e: Exception) {
                    Log.error(
                        TAG,
                        "Gel $gel2 crashed in didCollide on colliding with $gel1: " +
                            "${e.message}\n${e.stackTraceToString()}"
                    )
                    gel2.setZombie()
                }
            }
            is BrickCollision -> {
                val gel = collision.gel
                val brickBody = getRigidBody(collision.brickCoords, collision.shape, collision.material)!!
                strategies.computeNextSpeed(gel, brickBody, collision.brickCoords, collision)

                try {
                    gel.didCollide(
                        collision.shape,
                        collision.material,
                        myHit = collision.area1,
                        otherHit = collision.area2,
                        hitPt = collision.hitPt,
                        normalTowardsMe = hitNormal21
                    )
                } catch (e: Exception) {
                    Log.error(
                        TAG,
                        "Gel $gel crashed in didCollide on colliding with brick ${collision.brickCoords}: " +
                            "${e.message}\n${e.stackTraceToString()}"
                    )
                    gel.setZombie()
                }
            }
        }
    }

    private fun recoverFrom(e: AbortIterationException) {
        when (e) {
            is GelCrashedInInnerLoopError -> {
                val error = e.wrappedError
                val gel = e.gel
                Log.error(TAG, "Gel $gel crash in inner loop: ${error.message}\n${error.stackTraceToString()}")
                gel.setZombie()
                gel.vicinity.clear()
            }
            is BodyWasPushedTooFarFromItsOrigPos -> {
                val body = e.body
                val distance = e.distance
                val other = e.other
                var msg = "Pushed $body too far from its orig pos, distance=$distance, while colliding with $other."

                if (strategies.checkPosBeforeCollisions(e.body, e.other)) {
                    msg += " They collide at their orig pos already, so it seems we had to force them apart."
                    // don't reset nextPos
                } else {
                    msg += " Moving the first body's nextPos halfway towards its orig pos."
                    val pos = body.pos
                    val nextPos = body.nextPos
                    nextPos.set(
                        (pos.x + nextPos.x) * 0.5f,
                        (pos.y + nextPos.y) * 0.5f,
                        (pos.z + nextPos.z) * 0.5f,
                    )
                }

                Log.warn(TAG, msg)
            }
            is BodyWasPushedTooFarRelatively -> {
                val body = e.body
                val distance = e.distance
                val other = e.other
                var msg = "Pushed $body by $distance, which is > enclosing radius ${other.enclosingRadius} of $other."

                if (strategies.checkPosBeforeCollisions(e.body, e.other)) {
                    msg += " They collide at their orig pos already, so it seems we had to force them apart."
                    // don't reset nextPos
                } else {
                    msg += " Setting the first body's nextPos to where it was before the problem occurred."
                    e.body.nextPos.set(e.nextPosBefore)
                }

                Log.warn(TAG, msg)
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

    private fun checkAndSeparate(gel1: GraphicElement, gel2: GraphicElement, hit: MutableHitResult): Boolean {
        val body1 = gel1.body ?: return false
        val body2 = gel2.body ?: return false

        if (!gel1.shouldBounceOnCollision() || !gel2.shouldBounceOnCollision()) {
            // Simply check for a collision when one of the gels does not want bouncing, e.g. fire vs. player.
            if (strategies.checkNextPos(gel1, gel2, hit, separate = false)) {
                collisions.add(gel1, gel2, hit)
                return true
            }
        } else {
            tmpPos1.set(body1.nextPos)
            tmpPos2.set(body2.nextPos)

            if (strategies.checkNextPos(gel1, gel2, hit, separate = true)) {
                abortIfPushedTooFar(body1, tmpPos1, body2, tmpPos2)
                collisions.add(gel1, gel2, hit)
                return true
            }
        }

        return false
    }

    private fun checkAndSeparate(
        gel: GraphicElement,
        brickBody: RigidBody,
        brickCoords: Point3i,
        brickShape: BrickShape,
        brickMaterial: BrickMaterial,
        hit: MutableHitResult,
    ): Boolean {
        val bodyOfGel = gel.body ?: return false

        if (!gel.shouldBounceOnCollision()) {
            // Simply check for a collision when the gel does not want bouncing, e.g. fog vs. brick.
            if (strategies.checkNextPos(gel, brickBody, brickCoords, hit, separate = false)) {
                collisions.add(gel, brickCoords, brickShape, brickMaterial, hit)
                return true
            }
        } else {
            tmpPos1.set(bodyOfGel.nextPos)
            tmpPos2.set(brickBody.nextPos)

            if (strategies.checkNextPos(gel, brickBody, brickCoords, hit, separate = true)) {
                abortIfPushedTooFar(bodyOfGel, tmpPos1, brickBody, tmpPos2)
                collisions.add(gel, brickCoords, brickShape, brickMaterial, hit)
                return true
            }
        }

        return false
    }

    private fun checkAndSeparate(gel: GraphicElement, brickVolume: BrickVolume): Boolean {
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
                    brickVolume.getAtBrickCoord(brickCoords, brick)
                    val shape = brick.shape
                    val material = brick.material

                    getRigidBody(brickCoords, shape, material)?.let {
                        if (checkAndSeparate(gel, it, brickCoords, shape, material, hit)) {
                            anyCollisions = true
                        }
                    }
                }
            }
        }

        return anyCollisions
    }

    // FIXME Do this from BrickModel, reuse instances of RigidBody
    private fun getRigidBody(brickCoords: Point3i, shape: BrickShape, material: BrickMaterial): RigidBody? {
        if (shape == BrickShape.NONE) return null
        return FixedCuboidBody(
            "Brick($shape, $material, $brickCoords)",
            initialPos = Point3f.zero,
            mass = LARGE_MASS,
            elasticity = material.elasticity,
            friction = material.friction,
            gravity = false,
            sizeX = 0.0f,
            sizeY = 0.0f,
            sizeZ = 0.0f,
        ).apply {
            val x = (brickCoords.x + 0.5f) * WORLD_BRICK_SIZE
            val y = (brickCoords.y + 0.5f) * WORLD_BRICK_SIZE
            val z = (brickCoords.z + 0.5f) * WORLD_BRICK_SIZE
            pos.set(x, y, z)
            nextPos.set(x, y, z)
            setSize(WORLD_BRICK_SIZE, WORLD_BRICK_SIZE, WORLD_BRICK_SIZE)
        }
    }

    private fun abortIfPushedTooFar(
        body1: RigidBody,
        nextPos1Before: Point3f,
        body2: RigidBody,
        nextPos2Before: Point3f,
    ) {
        val distanceToPos1 = run {
            val dx = abs(body1.nextPos.x - body1.pos.x)
            val dy = abs(body1.nextPos.y - body1.pos.y)
            val dz = abs(body1.nextPos.z - body1.pos.z)
            max(dx, max(dy, dz))
        }

        if (distanceToPos1 > MAX_PUSH_DISTANCE) {
            throw BodyWasPushedTooFarFromItsOrigPos(body1, distanceToPos1, body2)
        }

        val distanceToPos2 = run {
            val dx = abs(body2.nextPos.x - body2.pos.x)
            val dy = abs(body2.nextPos.y - body2.pos.y)
            val dz = abs(body2.nextPos.z - body2.pos.z)
            max(dx, max(dy, dz))
        }

        if (distanceToPos2 > MAX_PUSH_DISTANCE) {
            throw BodyWasPushedTooFarFromItsOrigPos(body2, distanceToPos2, body1)
        }

        val distanceToBefore1 = run {
            val dx = abs(body1.nextPos.x - nextPos1Before.x)
            val dy = abs(body1.nextPos.y - nextPos1Before.y)
            val dz = abs(body1.nextPos.z - nextPos1Before.z)
            max(dx, max(dy, dz))
        }

        if (distanceToBefore1 > body2.enclosingRadius) {
            throw BodyWasPushedTooFarRelatively(body1, distanceToBefore1, nextPos1Before, body2)
        }

        val distanceToBefore2 = run {
            val dx = abs(body2.nextPos.x - nextPos2Before.x)
            val dy = abs(body2.nextPos.y - nextPos2Before.y)
            val dz = abs(body2.nextPos.z - nextPos2Before.z)
            max(dx, max(dy, dz))
        }

        if (distanceToBefore2 > body1.enclosingRadius) {
            throw BodyWasPushedTooFarRelatively(body2, distanceToBefore2, nextPos2Before, body1)
        }
    }

    private fun sanityCheckAfterAllCollisions() {
        require(!BuildConfig.isProduction())

        val content = App.content
        content.forEachIndexedGelInCollidableLayer { i, gel1 ->
            if (gel1.canCollideWithGels()) {
                // If the following sanity check fails, then COLLISION_VICINITY is probably too small.
                content.forEachIndexedGelInCollidableLayer(i + 1) { _, gel2 ->
                    if (gel2.canCollideWithGels()) {
                        if (strategies.checkNextPos(gel1, gel2, hit, separate = false)) {
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

    companion object {
        private val TAG = Log.Tag("CollisionManager")
        private const val COLLISION_VICINITY = 0.35f
        private const val MAX_NUM_OUTER_ITERATIONS = 100 // should never be as high as this
        private const val MAX_NUM_OUTER_ITERATIONS_WITHOUT_WARNING = 40
        private const val MAX_NUM_INNER_ITERATIONS = 42
        private const val MAX_NUM_INNER_ITERATIONS_WITHOUT_WARNING = 20
        private const val MAX_PUSH_DISTANCE = 0.42f
        private const val RANDOMIZE_NEXTPOS = 0.00000000001f // 1 mm/100000000, will be ~0.01m on the 30th iteration
    }
}
