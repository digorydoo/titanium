package ch.digorydoo.titanium.engine.physics

import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.BuildConfig
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.gel.GraphicElement
import ch.digorydoo.titanium.engine.gel.MutableGelSet
import ch.digorydoo.titanium.engine.physics.rigid_body.RigidBody.Companion.LARGE_MASS
import kotlin.random.Random

class CollisionManager {
    private val handler = CollisionHandler()
    private val collisions = CollisionSet()
    private val involvedGels = MutableGelSet()
    private val moreInvolvedGels = MutableGelSet()
    private val hit = MutableHitResult()
    private var collisionTicket = 0L

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

        collisions.forEach { handler.bounceAndNotifyGels(it) }
    }

    private fun handlePrimaryCollisions() {
        val bricks = App.content.bricks
        val content = App.content
        val ticket = collisionTicket

        content.forEachIndexedGelInCollidableLayer { i, gel1 ->
            val body1 = gel1.body
            val pos1 = body1?.nextPos ?: gel1.pos // encounter does not need a body; collision does
            val encounterRadius1 = gel1.encounterRadius
            val enclosingRadius1 = body1?.enclosingRadius ?: 0.0f

            val vicinity1 = gel1.vicinity
            vicinity1.clearIfOutdated(ticket)

            val gel1CanCollide = gel1.canCollideWithGels()
            val gel1CanEncounter = gel1.canEncounterOrBeFound()

            if (gel1CanCollide || gel1CanEncounter) {
                // We start here from i + 1, because gel1 has already been checked against gels with lower indices.
                content.forEachIndexedGelInCollidableLayer(i + 1) { _, gel2 ->
                    val gel2CanCollide = gel2.canCollideWithGels()
                    val gel2CanEncounter = gel2.canEncounterOrBeFound()

                    if (gel2CanCollide || gel2CanEncounter) {
                        val body2 = gel2.body
                        val pos2 = body2?.nextPos ?: gel2.pos
                        val enclosingRadius2 = body2?.enclosingRadius ?: 0.0f
                        val dx = pos1.x - pos2.x
                        val dy = pos1.y - pos2.y
                        val dz = pos1.z - pos2.z
                        val dsqr = dx * dx + dy * dy + dz * dz

                        if (gel1CanEncounter && gel2CanEncounter) {
                            if (encounterRadius1 > 0.0f) {
                                val maxDist = encounterRadius1 + enclosingRadius2

                                if (dsqr <= maxDist * maxDist) {
                                    gel1.onEncounter(gel2)
                                }
                            }

                            val encounterRadius2 = gel2.encounterRadius

                            if (encounterRadius2 > 0.0f) {
                                val maxDist = encounterRadius2 + enclosingRadius1

                                if (dsqr <= maxDist * maxDist) {
                                    gel2.onEncounter(gel1)
                                }
                            }
                        }

                        if (gel1CanCollide && gel2CanCollide) {
                            if (!BuildConfig.isProduction()) {
                                require(gel1.body != null) { "Gel without body returned true from canCollide: $gel1" }
                                require(gel2.body != null) { "Gel without body returned true from canCollide: $gel2" }
                            }

                            val maxDist = enclosingRadius1 + enclosingRadius2 + 2 * COLLISION_VICINITY

                            if (dsqr <= maxDist * maxDist) {
                                val vicinity2 = gel2.vicinity
                                vicinity2.clearIfOutdated(ticket)

                                vicinity1.add(gel2) // MutableGelSet never adds duplicates
                                vicinity2.add(gel1)

                                try {
                                    if (handler.checkAndSeparate(gel1, gel2, collisions, hit)) {
                                        involvedGels.add(gel1)
                                        involvedGels.add(gel2)
                                    }
                                } catch (e: CollisionError) {
                                    Log.info(TAG, "The following problem was caught during primary collision phase")
                                    recoverFrom(e)
                                    involvedGels.add(gel1)
                                    involvedGels.add(gel2)
                                }
                            }
                        }
                    }
                }
            }

            if (bricks != null && gel1.canCollideWithBricks()) {
                try {
                    if (handler.checkAndSeparate(gel1, bricks, collisions, hit)) {
                        involvedGels.add(gel1)
                    }
                } catch (e: CollisionError) {
                    Log.info(TAG, "The following problem was caught during primary collision phase")
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

                                if (handler.checkAndSeparate(gel1, gel2, collisions, hit)) {
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
                                if (handler.checkAndSeparate(gel1, bricks, collisions, hit)) {
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
                    } catch (e: CollisionError) {
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
            } catch (e: CollisionError) {
                Log.info(TAG, "Iteration #$outerIteration aborted")
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

    private fun randomizeNextPos(gel: GraphicElement, rndRange: Float) {
        val body = gel.body ?: return
        if (body.mass >= LARGE_MASS) return

        body.nextPos.apply {
            x += rndRange * (1.0f - 2.0f * Random.nextFloat())
            y += rndRange * (1.0f - 2.0f * Random.nextFloat())
            z += rndRange * (1.0f - 2.0f * Random.nextFloat())
        }
    }

    private fun recoverFrom(e: CollisionError) {
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

                if (e.collideAtOrigPos) {
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

                if (e.collideAtOrigPos) {
                    msg += " They collide at their orig pos already, so it seems we had to force them apart."
                    // don't reset nextPos
                } else {
                    msg += " Setting the first body's nextPos to where it was before the problem occurred."
                    e.body.nextPos.set(e.nextPosBefore)
                }

                Log.warn(TAG, msg)
            }
            is SeparationFailed -> {
                Log.error(
                    TAG,
                    arrayOf(
                        "Failed to properly separate the bodies",
                        "   body1=${e.body1}",
                        "      pos ${e.body1.pos}",
                        "      nextPos before sep ${e.nextPos1Before}",
                        "      nextPos after sep ${e.body1.nextPos}",
                        "      speed ${e.body1.speed}",
                        "      nextSpeed ${e.body1.nextSpeed}",
                        "      speedBeforeCollisions ${e.body1.speedBeforeCollisions}",
                        "   body2=${e.body2}",
                        "      pos ${e.body2.pos}",
                        "      nextPos before sep ${e.nextPos2Before}",
                        "      nextPos after sep ${e.body2.nextPos}",
                        "      speed ${e.body2.speed}",
                        "      nextSpeed ${e.body2.nextSpeed}",
                        "      speedBeforeCollisions ${e.body2.speedBeforeCollisions}",
                    ).joinToString("\n")
                )
            }
        }
    }

    companion object {
        private val TAG = Log.Tag("CollisionManager")
        const val COLLISION_VICINITY = 0.35f
        private const val MAX_NUM_OUTER_ITERATIONS = 100 // should never be as high as this
        private const val MAX_NUM_OUTER_ITERATIONS_WITHOUT_WARNING = 40
        private const val MAX_NUM_INNER_ITERATIONS = 42
        private const val MAX_NUM_INNER_ITERATIONS_WITHOUT_WARNING = 20
        private const val RANDOMIZE_NEXTPOS = 0.00000000001f // 1 mm/100000000, will be ~0.01m on the 30th iteration
    }
}
