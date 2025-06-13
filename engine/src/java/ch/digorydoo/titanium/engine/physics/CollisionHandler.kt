package ch.digorydoo.titanium.engine.physics

import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.kutils.point.MutablePoint3i
import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.kutils.point.Point3i
import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.BuildConfig
import ch.digorydoo.titanium.engine.brick.Brick
import ch.digorydoo.titanium.engine.brick.BrickFaceCoveringRetriever
import ch.digorydoo.titanium.engine.brick.BrickMaterial
import ch.digorydoo.titanium.engine.brick.BrickShape
import ch.digorydoo.titanium.engine.brick.BrickVolume
import ch.digorydoo.titanium.engine.brick.BrickVolume.Companion.WORLD_BRICK_SIZE
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.gel.GraphicElement
import ch.digorydoo.titanium.engine.physics.CollisionManager.Companion.COLLISION_VICINITY
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedCuboidBody
import ch.digorydoo.titanium.engine.physics.rigid_body.RigidBody
import ch.digorydoo.titanium.engine.physics.rigid_body.RigidBody.Companion.LARGE_MASS
import ch.digorydoo.titanium.engine.physics.strategy.CollisionStrategiesWrapper
import kotlin.math.abs
import kotlin.math.max

internal class CollisionHandler {
    private val strategies = CollisionStrategiesWrapper()

    private val hitNormal21 = MutablePoint3f()
    private val brick = Brick()
    private val brickCoords = MutablePoint3i()
    private val tmpPos1 = MutablePoint3f()
    private val tmpPos2 = MutablePoint3f()

    private var cachedBrickFaceCoveringRetriever: BrickFaceCoveringRetriever? = null

    private val brickFaceCoveringRetriever
        get() = cachedBrickFaceCoveringRetriever?.takeIf { it.volume == App.bricks }
            ?: BrickFaceCoveringRetriever(App.bricks).also { cachedBrickFaceCoveringRetriever = it }

    fun checkAndSeparate(
        gel1: GraphicElement,
        gel2: GraphicElement,
        collisions: CollisionSet,
        hit: MutableHitResult,
    ): Boolean {
        val body1 = gel1.body ?: return false
        val body2 = gel2.body ?: return false

        if (!gel1.shouldBounceOnCollision() || !gel2.shouldBounceOnCollision()) {
            // Simply check for a collision when one of the gels does not want bouncing, e.g. fire vs. player.
            if (strategies.check(body1, body1.nextPos, body2, body2.nextPos, hit)) {
                collisions.add(gel1, gel2, hit)
                return true
            }
        } else {
            tmpPos1.set(body1.nextPos)
            tmpPos2.set(body2.nextPos)

            if (strategies.check(body1, body1.nextPos, body2, body2.nextPos, hit)) {
                strategies.separate(body1, body2, hit)

                if (!BuildConfig.isProduction()) {
                    // Check that bodies no longer collide
                    if (strategies.check(body1, body1.nextPos, body2, body2.nextPos)) {
                        throw SeparationFailed(body1, Point3f(tmpPos1), body2, Point3f(tmpPos2))
                    }
                }

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
        collisions: CollisionSet,
        hit: MutableHitResult,
    ): Boolean {
        val gelBody = gel.body ?: return false
        val bfcr = brickFaceCoveringRetriever

        if (!gel.shouldBounceOnCollision()) {
            // Simply check for a collision when the gel does not want bouncing, e.g. fog vs. brick.
            if (strategies.check(gelBody, gelBody.nextPos, brickBody, brickBody.nextPos, bfcr, brickCoords, hit)) {
                collisions.add(gel, brickCoords, brickShape, brickMaterial, hit)
                return true
            }
        } else {
            tmpPos1.set(gelBody.nextPos)
            tmpPos2.set(brickBody.nextPos)

            if (strategies.check(gelBody, gelBody.nextPos, brickBody, brickBody.nextPos, bfcr, brickCoords, hit)) {
                strategies.separate(gelBody, brickBody, hit)

                if (!BuildConfig.isProduction()) {
                    val stillCollide =
                        strategies.check(gelBody, gelBody.nextPos, brickBody, brickBody.nextPos, bfcr, brickCoords)

                    if (stillCollide) {
                        throw SeparationFailed(gelBody, Point3f(tmpPos1), brickBody, Point3f(tmpPos2))
                    }
                }

                abortIfPushedTooFar(gelBody, tmpPos1, brickBody, tmpPos2)
                collisions.add(gel, brickCoords, brickShape, brickMaterial, hit)
                return true
            }
        }

        return false
    }

    fun checkAndSeparate(
        gel: GraphicElement,
        brickVolume: BrickVolume,
        collisions: CollisionSet,
        hit: MutableHitResult,
    ): Boolean {
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
                        if (checkAndSeparate(gel, it, brickCoords, shape, material, collisions, hit)) {
                            anyCollisions = true
                        }
                    }
                }
            }
        }

        return anyCollisions
    }

    fun bounceAndNotifyGels(collision: Collision) {
        val n12 = collision.hitNormal12
        hitNormal21.set(-n12.x, -n12.y, -n12.z)

        when (collision) {
            is GelCollision -> {
                val gel1 = collision.gel1
                val gel2 = collision.gel2
                val body1 = gel1.body!!
                val body2 = gel2.body!!

                strategies.bounce(body1, body2, collision)

                try {
                    gel1.onCollide(
                        gel2,
                        myHit = collision.area1,
                        otherHit = collision.area2,
                        hitPt = collision.hitPt,
                        normalTowardsMe = hitNormal21
                    )
                } catch (e: Exception) {
                    Log.error(
                        TAG,
                        "Gel $gel1 crashed in didCollide on colliding with $gel2\n" +
                            "area1=${collision.area1}, area2=${collision.area2}\n" +
                            "msg=${e.message}\n" +
                            e.stackTraceToString()
                    )
                    gel1.setZombie()
                }

                try {
                    gel2.onCollide(
                        gel1,
                        myHit = collision.area2,
                        otherHit = collision.area1,
                        hitPt = collision.hitPt,
                        normalTowardsMe = n12
                    )
                } catch (e: Exception) {
                    Log.error(
                        TAG,
                        "Gel $gel2 crashed in didCollide on colliding with $gel1\n" +
                            "area2=${collision.area2}, area1=${collision.area1}\n" +
                            "msg=${e.message}\n" +
                            e.stackTraceToString()
                    )
                    gel2.setZombie()
                }
            }
            is BrickCollision -> {
                val gel = collision.gel
                val gelBody = gel.body!!
                val brickBody = getRigidBody(collision.brickCoords, collision.shape, collision.material)!!
                strategies.bounce(gelBody, brickBody, collision)

                try {
                    gel.onCollide(
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
            val collideAtOrigPos = strategies.check(body1, body1.pos, body2, body2.pos)
            throw BodyWasPushedTooFarFromItsOrigPos(body1, distanceToPos1, body2, collideAtOrigPos)
        }

        val distanceToPos2 = run {
            val dx = abs(body2.nextPos.x - body2.pos.x)
            val dy = abs(body2.nextPos.y - body2.pos.y)
            val dz = abs(body2.nextPos.z - body2.pos.z)
            max(dx, max(dy, dz))
        }

        if (distanceToPos2 > MAX_PUSH_DISTANCE) {
            val collideAtOrigPos = strategies.check(body1, body1.pos, body2, body2.pos)
            throw BodyWasPushedTooFarFromItsOrigPos(body2, distanceToPos2, body1, collideAtOrigPos)
        }

        val distanceToBefore1 = run {
            val dx = abs(body1.nextPos.x - nextPos1Before.x)
            val dy = abs(body1.nextPos.y - nextPos1Before.y)
            val dz = abs(body1.nextPos.z - nextPos1Before.z)
            max(dx, max(dy, dz))
        }

        if (distanceToBefore1 > body2.enclosingRadius) {
            val collideAtOrigPos = strategies.check(body1, body1.pos, body2, body2.pos)
            throw BodyWasPushedTooFarRelatively(body1, distanceToBefore1, nextPos1Before, body2, collideAtOrigPos)
        }

        val distanceToBefore2 = run {
            val dx = abs(body2.nextPos.x - nextPos2Before.x)
            val dy = abs(body2.nextPos.y - nextPos2Before.y)
            val dz = abs(body2.nextPos.z - nextPos2Before.z)
            max(dx, max(dy, dz))
        }

        if (distanceToBefore2 > body1.enclosingRadius) {
            val collideAtOrigPos = strategies.check(body1, body1.pos, body2, body2.pos)
            throw BodyWasPushedTooFarRelatively(body2, distanceToBefore2, nextPos2Before, body1, collideAtOrigPos)
        }
    }

    companion object {
        private val TAG = Log.Tag("CollisionHandler")
        private const val MAX_PUSH_DISTANCE = 0.42f
    }
}
