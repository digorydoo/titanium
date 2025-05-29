package ch.digorydoo.titanium.engine.physics

import ch.digorydoo.kutils.point.Point3i
import ch.digorydoo.titanium.engine.brick.BrickFaceCoveringRetriever
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.gel.GraphicElement
import ch.digorydoo.titanium.engine.physics.collision_strategy.*
import ch.digorydoo.titanium.engine.physics.helper.HitResult
import ch.digorydoo.titanium.engine.physics.helper.MutableHitResult
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedCuboidBody
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedCylinderBody
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedSphereBody
import ch.digorydoo.titanium.engine.physics.rigid_body.RigidBody

internal class CollisionStrategiesWrapper {
    private val sphereVsSphere = CollideSphereVsSphere()
    private val sphereVsCylinder = CollideSphereVsCylinder()
    private val sphereVsCuboid = CollideSphereVsCuboid()
    private val cylinderVsCylinder = CollideCylinderVsCylinder()
    private val cylinderVsCuboid = CollideCylinderVsCuboid()

    private var cachedBrickFaceCoveringRetriever: BrickFaceCoveringRetriever? = null

    private val brickFaceCoveringRetriever
        get() = cachedBrickFaceCoveringRetriever?.takeIf { it.volume == App.bricks }
            ?: BrickFaceCoveringRetriever(App.bricks).also { cachedBrickFaceCoveringRetriever = it }

    fun checkPosBeforeCollisions(body1: RigidBody, body2: RigidBody): Boolean {
        fun <B1: RigidBody, B2: RigidBody> call(strategy: CollisionStrategy<B1, B2>, b1: B1, b2: B2): Boolean {
            strategy.configure(body1IsBrick = false, body2IsBrick = false, null, null)
            val p1 = b1.pos
            val p2 = b2.pos
            return strategy.check(b1, p1.x, p1.y, p1.z, b2, p2.x, p2.y, p2.z, null)
        }

        return when (body1) {
            is FixedSphereBody -> when (body2) {
                is FixedSphereBody -> call(sphereVsSphere, body1, body2)
                is FixedCylinderBody -> call(sphereVsCylinder, body1, body2)
                is FixedCuboidBody -> call(sphereVsCuboid, body1, body2)
            }
            is FixedCylinderBody -> when (body2) {
                is FixedSphereBody -> call(sphereVsCylinder, body2, body1)
                is FixedCylinderBody -> call(cylinderVsCylinder, body1, body2)
                is FixedCuboidBody -> call(cylinderVsCuboid, body1, body2)
            }
            is FixedCuboidBody -> when (body2) {
                is FixedSphereBody -> call(sphereVsCuboid, body2, body1)
                is FixedCylinderBody -> call(cylinderVsCuboid, body2, body1)
                is FixedCuboidBody -> throw NotImplementedError() // FIXME
            }
        }
    }

    fun checkNextPos(gel1: GraphicElement, gel2: GraphicElement, hit: MutableHitResult, separate: Boolean): Boolean {
        val body1 = gel1.body ?: return false
        val body2 = gel2.body ?: return false

        fun <B1: RigidBody, B2: RigidBody> call(strategy: CollisionStrategy<B1, B2>, b1: B1, b2: B2): Boolean {
            strategy.configure(body1IsBrick = false, body2IsBrick = false, null, null)
            val p1 = b1.nextPos
            val p2 = b2.nextPos

            if (strategy.check(b1, p1.x, p1.y, p1.z, b2, p2.x, p2.y, p2.z, hit)) {
                if (separate) strategy.separate(b1, b2, hit)
                return true
            } else {
                return false
            }
        }

        return when (body1) {
            is FixedSphereBody -> when (body2) {
                is FixedSphereBody -> call(sphereVsSphere, body1, body2)
                is FixedCylinderBody -> call(sphereVsCylinder, body1, body2)
                is FixedCuboidBody -> call(sphereVsCuboid, body1, body2)
            }
            is FixedCylinderBody -> when (body2) {
                is FixedSphereBody -> call(sphereVsCylinder, body2, body1)
                is FixedCylinderBody -> call(cylinderVsCylinder, body1, body2)
                is FixedCuboidBody -> call(cylinderVsCuboid, body1, body2)
            }
            is FixedCuboidBody -> when (body2) {
                is FixedSphereBody -> call(sphereVsCuboid, body2, body1)
                is FixedCylinderBody -> call(cylinderVsCuboid, body2, body1)
                is FixedCuboidBody -> throw NotImplementedError() // FIXME
            }
        }
    }

    fun computeNextSpeed(gel1: GraphicElement, gel2: GraphicElement, hit: HitResult) {
        val body1 = gel1.body ?: return
        val body2 = gel2.body ?: return

        fun <B1: RigidBody, B2: RigidBody> call(strategy: CollisionStrategy<B1, B2>, b1: B1, b2: B2) {
            strategy.configure(body1IsBrick = false, body2IsBrick = false, null, null)
            strategy.computeNextSpeed(b1, b2, hit)
        }

        return when (body1) {
            is FixedSphereBody -> when (body2) {
                is FixedSphereBody -> call(sphereVsSphere, body1, body2)
                is FixedCylinderBody -> call(sphereVsCylinder, body1, body2)
                is FixedCuboidBody -> call(sphereVsCuboid, body1, body2)
            }
            is FixedCylinderBody -> when (body2) {
                is FixedSphereBody -> call(sphereVsCylinder, body2, body1)
                is FixedCylinderBody -> call(cylinderVsCylinder, body1, body2)
                is FixedCuboidBody -> call(cylinderVsCuboid, body1, body2)
            }
            is FixedCuboidBody -> when (body2) {
                is FixedSphereBody -> call(sphereVsCuboid, body2, body1)
                is FixedCylinderBody -> call(cylinderVsCuboid, body2, body1)
                is FixedCuboidBody -> throw NotImplementedError() // FIXME
            }
        }
    }

    fun checkNextPos(
        gel: GraphicElement,
        brickBody: RigidBody,
        brickCoords: Point3i,
        hit: MutableHitResult,
        separate: Boolean,
    ): Boolean {
        val gelBody = gel.body ?: return false

        fun <B1: RigidBody, B2: RigidBody> call(
            strategy: CollisionStrategy<B1, B2>,
            b1: B1,
            b2: B2,
            b1IsBrick: Boolean,
        ): Boolean {
            strategy.configure(
                body1IsBrick = b1IsBrick,
                body2IsBrick = !b1IsBrick,
                brickFaceCoveringRetriever,
                brickCoords
            )
            val p1 = b1.nextPos
            val p2 = b2.nextPos

            if (strategy.check(b1, p1.x, p1.y, p1.z, b2, p2.x, p2.y, p2.z, hit)) {
                if (separate) strategy.separate(b1, b2, hit)
                return true
            } else {
                return false
            }
        }

        return when (gelBody) {
            is FixedSphereBody -> when (brickBody) {
                is FixedSphereBody -> call(sphereVsSphere, gelBody, brickBody, b1IsBrick = false)
                is FixedCylinderBody -> call(sphereVsCylinder, gelBody, brickBody, b1IsBrick = false)
                is FixedCuboidBody -> call(sphereVsCuboid, gelBody, brickBody, b1IsBrick = false)
            }
            is FixedCylinderBody -> when (brickBody) {
                is FixedSphereBody -> call(sphereVsCylinder, brickBody, gelBody, b1IsBrick = true)
                is FixedCylinderBody -> call(cylinderVsCylinder, gelBody, brickBody, b1IsBrick = false)
                is FixedCuboidBody -> call(cylinderVsCuboid, gelBody, brickBody, b1IsBrick = false)
            }
            is FixedCuboidBody -> when (brickBody) {
                is FixedSphereBody -> call(sphereVsCuboid, brickBody, gelBody, b1IsBrick = true)
                is FixedCylinderBody -> call(cylinderVsCuboid, brickBody, gelBody, b1IsBrick = true)
                is FixedCuboidBody -> throw NotImplementedError() // FIXME
            }
        }
    }

    fun computeNextSpeed(gel: GraphicElement, brickBody: RigidBody, brickCoords: Point3i, hit: HitResult) {
        val gelBody = gel.body ?: return

        fun <B1: RigidBody, B2: RigidBody> call(
            strategy: CollisionStrategy<B1, B2>,
            b1: B1,
            b2: B2,
            b1IsBrick: Boolean,
        ) {
            strategy.configure(
                body1IsBrick = b1IsBrick,
                body2IsBrick = !b1IsBrick,
                brickFaceCoveringRetriever,
                brickCoords
            )
            strategy.computeNextSpeed(b1, b2, hit)
        }

        return when (gelBody) {
            is FixedSphereBody -> when (brickBody) {
                is FixedSphereBody -> call(sphereVsSphere, gelBody, brickBody, b1IsBrick = false)
                is FixedCylinderBody -> call(sphereVsCylinder, gelBody, brickBody, b1IsBrick = false)
                is FixedCuboidBody -> call(sphereVsCuboid, gelBody, brickBody, b1IsBrick = false)
            }
            is FixedCylinderBody -> when (brickBody) {
                is FixedSphereBody -> call(sphereVsCylinder, brickBody, gelBody, b1IsBrick = true)
                is FixedCylinderBody -> call(cylinderVsCylinder, gelBody, brickBody, b1IsBrick = false)
                is FixedCuboidBody -> call(cylinderVsCuboid, gelBody, brickBody, b1IsBrick = false)
            }
            is FixedCuboidBody -> when (brickBody) {
                is FixedSphereBody -> call(sphereVsCuboid, brickBody, gelBody, b1IsBrick = true)
                is FixedCylinderBody -> call(cylinderVsCuboid, brickBody, gelBody, b1IsBrick = true)
                is FixedCuboidBody -> throw NotImplementedError() // FIXME
            }
        }
    }
}
