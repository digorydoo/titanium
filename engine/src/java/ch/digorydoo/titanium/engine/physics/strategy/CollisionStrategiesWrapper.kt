package ch.digorydoo.titanium.engine.physics.strategy

import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.kutils.point.Point3i
import ch.digorydoo.titanium.engine.brick.IBrickFaceCoveringRetriever
import ch.digorydoo.titanium.engine.physics.HitResult
import ch.digorydoo.titanium.engine.physics.MutableHitResult
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedCapsuleBody
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedCuboidBody
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedCylinderBody
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedSphereBody
import ch.digorydoo.titanium.engine.physics.rigid_body.RigidBody
import ch.digorydoo.titanium.engine.physics.strategy.capsule_vs_capsule.BounceCapsuleVsCapsule
import ch.digorydoo.titanium.engine.physics.strategy.capsule_vs_capsule.CheckCapsuleVsCapsule
import ch.digorydoo.titanium.engine.physics.strategy.capsule_vs_capsule.SeparateCapsuleVsCapsule
import ch.digorydoo.titanium.engine.physics.strategy.capsule_vs_cuboid.BounceCapsuleVsCuboid
import ch.digorydoo.titanium.engine.physics.strategy.capsule_vs_cuboid.CheckCapsuleVsCuboid
import ch.digorydoo.titanium.engine.physics.strategy.capsule_vs_cuboid.SeparateCapsuleVsCuboid
import ch.digorydoo.titanium.engine.physics.strategy.cylinder_vs_capsule.BounceCylinderVsCapsule
import ch.digorydoo.titanium.engine.physics.strategy.cylinder_vs_capsule.CheckCylinderVsCapsule
import ch.digorydoo.titanium.engine.physics.strategy.cylinder_vs_capsule.SeparateCylinderVsCapsule
import ch.digorydoo.titanium.engine.physics.strategy.cylinder_vs_cuboid.BounceCylinderVsCuboid
import ch.digorydoo.titanium.engine.physics.strategy.cylinder_vs_cuboid.CheckCylinderVsCuboid
import ch.digorydoo.titanium.engine.physics.strategy.cylinder_vs_cuboid.SeparateCylinderVsCuboid
import ch.digorydoo.titanium.engine.physics.strategy.cylinder_vs_cylinder.BounceCylinderVsCylinder
import ch.digorydoo.titanium.engine.physics.strategy.cylinder_vs_cylinder.CheckCylinderVsCylinder
import ch.digorydoo.titanium.engine.physics.strategy.cylinder_vs_cylinder.SeparateCylinderVsCylinder
import ch.digorydoo.titanium.engine.physics.strategy.sphere_vs_capsule.BounceSphereVsCapsule
import ch.digorydoo.titanium.engine.physics.strategy.sphere_vs_capsule.CheckSphereVsCapsule
import ch.digorydoo.titanium.engine.physics.strategy.sphere_vs_capsule.SeparateSphereVsCapsule
import ch.digorydoo.titanium.engine.physics.strategy.sphere_vs_cuboid.BounceSphereVsCuboid
import ch.digorydoo.titanium.engine.physics.strategy.sphere_vs_cuboid.CheckSphereVsCuboid
import ch.digorydoo.titanium.engine.physics.strategy.sphere_vs_cuboid.SeparateSphereVsCuboid
import ch.digorydoo.titanium.engine.physics.strategy.sphere_vs_cylinder.BounceSphereVsCylinder
import ch.digorydoo.titanium.engine.physics.strategy.sphere_vs_cylinder.CheckSphereVsCylinder
import ch.digorydoo.titanium.engine.physics.strategy.sphere_vs_cylinder.SeparateSphereVsCylinder
import ch.digorydoo.titanium.engine.physics.strategy.sphere_vs_sphere.BounceSphereVsSphere
import ch.digorydoo.titanium.engine.physics.strategy.sphere_vs_sphere.CheckSphereVsSphere
import ch.digorydoo.titanium.engine.physics.strategy.sphere_vs_sphere.SeparateSphereVsSphere

internal class CollisionStrategiesWrapper {
    private val chkCapsuleVsCapsule = CheckCapsuleVsCapsule()
    private val chkCapsuleVsCuboid = CheckCapsuleVsCuboid()
    private val chkCylinderVsCapsule = CheckCylinderVsCapsule()
    private val chkCylinderVsCuboid = CheckCylinderVsCuboid()
    private val chkCylinderVsCylinder = CheckCylinderVsCylinder()
    private val chkSphereVsCapsule = CheckSphereVsCapsule()
    private val chkSphereVsCuboid = CheckSphereVsCuboid()
    private val chkSphereVsCylinder = CheckSphereVsCylinder()
    private val chkSphereVsSphere = CheckSphereVsSphere()

    private val sepCapsuleVsCapsule = SeparateCapsuleVsCapsule()
    private val sepCapsuleVsCuboid = SeparateCapsuleVsCuboid()
    private val sepCylinderVsCapsule = SeparateCylinderVsCapsule()
    private val sepCylinderVsCuboid = SeparateCylinderVsCuboid()
    private val sepCylinderVsCylinder = SeparateCylinderVsCylinder()
    private val sepSphereVsCapsule = SeparateSphereVsCapsule()
    private val sepSphereVsCuboid = SeparateSphereVsCuboid()
    private val sepSphereVsCylinder = SeparateSphereVsCylinder()
    private val sepSphereVsSphere = SeparateSphereVsSphere()

    private val bncCapsuleVsCapsule = BounceCapsuleVsCapsule()
    private val bncCapsuleVsCuboid = BounceCapsuleVsCuboid()
    private val bncCylinderVsCapsule = BounceCylinderVsCapsule()
    private val bncCylinderVsCuboid = BounceCylinderVsCuboid()
    private val bncCylinderVsCylinder = BounceCylinderVsCylinder()
    private val bncSphereVsCapsule = BounceSphereVsCapsule()
    private val bncSphereVsCuboid = BounceSphereVsCuboid()
    private val bncSphereVsCylinder = BounceSphereVsCylinder()
    private val bncSphereVsSphere = BounceSphereVsSphere()

    private val tmpHit = MutableHitResult()

    fun check(
        body1: RigidBody,
        centre1: Point3f,
        body2: RigidBody,
        centre2: Point3f,
        outHit: MutableHitResult? = null,
    ): Boolean {
        fun <B1: RigidBody, B2: RigidBody> call(
            strategy: CollisionCheckStrategy<B1, B2>,
            b1: B1,
            b2: B2,
            swap: Boolean = false,
        ): Boolean {
            val collide = if (swap) {
                strategy.check(b1, centre2, b2, centre1, outHit)
            } else {
                strategy.check(b1, centre1, b2, centre2, outHit)
            }
            if (swap) outHit?.swapPointOfView()
            return collide
        }

        return when (body1) {
            is FixedSphereBody -> when (body2) {
                is FixedSphereBody -> call(chkSphereVsSphere, body1, body2)
                is FixedCylinderBody -> call(chkSphereVsCylinder, body1, body2)
                is FixedCuboidBody -> call(chkSphereVsCuboid, body1, body2)
                is FixedCapsuleBody -> call(chkSphereVsCapsule, body1, body2)
            }
            is FixedCylinderBody -> when (body2) {
                is FixedSphereBody -> call(chkSphereVsCylinder, body2, body1, swap = true)
                is FixedCylinderBody -> call(chkCylinderVsCylinder, body1, body2)
                is FixedCuboidBody -> call(chkCylinderVsCuboid, body1, body2)
                is FixedCapsuleBody -> call(chkCylinderVsCapsule, body1, body2)
            }
            is FixedCuboidBody -> when (body2) {
                is FixedSphereBody -> call(chkSphereVsCuboid, body2, body1, swap = true)
                is FixedCylinderBody -> call(chkCylinderVsCuboid, body2, body1, swap = true)
                is FixedCuboidBody -> throw NotImplementedError() // FIXME
                is FixedCapsuleBody -> call(chkCapsuleVsCuboid, body2, body1, swap = true)
            }
            is FixedCapsuleBody -> when (body2) {
                is FixedSphereBody -> call(chkSphereVsCapsule, body2, body1, swap = true)
                is FixedCylinderBody -> call(chkCylinderVsCapsule, body2, body1, swap = true)
                is FixedCuboidBody -> call(chkCapsuleVsCuboid, body1, body2)
                is FixedCapsuleBody -> call(chkCapsuleVsCapsule, body1, body2)
            }
        }
    }

    fun check(
        gelBody: RigidBody,
        gelCentre: Point3f,
        brickBody: RigidBody,
        brickCentre: Point3f,
        bfcr: IBrickFaceCoveringRetriever,
        brickCoords: Point3i,
        outHit: MutableHitResult? = null,
    ): Boolean {
        fun <B1: RigidBody, B2: RigidBody> call(
            strategy: CollisionCheckStrategy<B1, B2>,
            b1: B1,
            b2: B2,
            swap: Boolean = false,
        ): Boolean {
            val collide = if (swap) {
                strategy.check(b1, brickCentre, true, b2, gelCentre, false, bfcr, brickCoords, outHit)
            } else {
                strategy.check(b1, gelCentre, false, b2, brickCentre, true, bfcr, brickCoords, outHit)
            }
            if (swap) outHit?.swapPointOfView()
            return collide
        }

        return when (gelBody) {
            is FixedSphereBody -> when (brickBody) {
                is FixedSphereBody -> call(chkSphereVsSphere, gelBody, brickBody)
                is FixedCylinderBody -> call(chkSphereVsCylinder, gelBody, brickBody)
                is FixedCuboidBody -> call(chkSphereVsCuboid, gelBody, brickBody)
                is FixedCapsuleBody -> call(chkSphereVsCapsule, gelBody, brickBody)
            }
            is FixedCylinderBody -> when (brickBody) {
                is FixedSphereBody -> call(chkSphereVsCylinder, brickBody, gelBody, swap = true)
                is FixedCylinderBody -> call(chkCylinderVsCylinder, gelBody, brickBody)
                is FixedCuboidBody -> call(chkCylinderVsCuboid, gelBody, brickBody)
                is FixedCapsuleBody -> call(chkCylinderVsCapsule, gelBody, brickBody)
            }
            is FixedCuboidBody -> when (brickBody) {
                is FixedSphereBody -> call(chkSphereVsCuboid, brickBody, gelBody, swap = true)
                is FixedCylinderBody -> call(chkCylinderVsCuboid, brickBody, gelBody, swap = true)
                is FixedCuboidBody -> throw NotImplementedError() // FIXME
                is FixedCapsuleBody -> call(chkCapsuleVsCuboid, brickBody, gelBody, swap = true)
            }
            is FixedCapsuleBody -> when (brickBody) {
                is FixedSphereBody -> call(chkSphereVsCapsule, brickBody, gelBody, swap = true)
                is FixedCylinderBody -> call(chkCylinderVsCapsule, brickBody, gelBody, swap = true)
                is FixedCuboidBody -> call(chkCapsuleVsCuboid, gelBody, brickBody)
                is FixedCapsuleBody -> call(chkCapsuleVsCapsule, gelBody, brickBody)
            }
        }
    }

    fun separate(body1: RigidBody, body2: RigidBody, hit: HitResult) {
        fun <B1: RigidBody, B2: RigidBody> call(
            strategy: CollisionSeparateStrategy<B1, B2>,
            b1: B1,
            b2: B2,
            swap: Boolean = false,
        ) {
            strategy.separate(b1, b2, if (swap) tmpHit.apply { setSwapped(hit) } else hit)
        }

        when (body1) {
            is FixedSphereBody -> when (body2) {
                is FixedSphereBody -> call(sepSphereVsSphere, body1, body2)
                is FixedCylinderBody -> call(sepSphereVsCylinder, body1, body2)
                is FixedCuboidBody -> call(sepSphereVsCuboid, body1, body2)
                is FixedCapsuleBody -> call(sepSphereVsCapsule, body1, body2)
            }
            is FixedCylinderBody -> when (body2) {
                is FixedSphereBody -> call(sepSphereVsCylinder, body2, body1, swap = true)
                is FixedCylinderBody -> call(sepCylinderVsCylinder, body1, body2)
                is FixedCuboidBody -> call(sepCylinderVsCuboid, body1, body2)
                is FixedCapsuleBody -> call(sepCylinderVsCapsule, body1, body2)
            }
            is FixedCuboidBody -> when (body2) {
                is FixedSphereBody -> call(sepSphereVsCuboid, body2, body1, swap = true)
                is FixedCylinderBody -> call(sepCylinderVsCuboid, body2, body1, swap = true)
                is FixedCuboidBody -> throw NotImplementedError() // FIXME
                is FixedCapsuleBody -> call(sepCapsuleVsCuboid, body2, body1, swap = true)
            }
            is FixedCapsuleBody -> when (body2) {
                is FixedSphereBody -> call(sepSphereVsCapsule, body2, body1, swap = true)
                is FixedCylinderBody -> call(sepCylinderVsCapsule, body2, body1, swap = true)
                is FixedCuboidBody -> call(sepCapsuleVsCuboid, body1, body2)
                is FixedCapsuleBody -> call(sepCapsuleVsCapsule, body1, body2)
            }
        }
    }

    fun bounce(body1: RigidBody, body2: RigidBody, hit: HitResult) {
        fun <B1: RigidBody, B2: RigidBody> call(
            strategy: CollisionBounceStrategy<B1, B2>,
            b1: B1,
            b2: B2,
            swap: Boolean = false,
        ) {
            strategy.bounce(b1, b2, if (swap) tmpHit.apply { setSwapped(hit) } else hit)
        }

        return when (body1) {
            is FixedSphereBody -> when (body2) {
                is FixedSphereBody -> call(bncSphereVsSphere, body1, body2)
                is FixedCylinderBody -> call(bncSphereVsCylinder, body1, body2)
                is FixedCuboidBody -> call(bncSphereVsCuboid, body1, body2)
                is FixedCapsuleBody -> call(bncSphereVsCapsule, body1, body2)
            }
            is FixedCylinderBody -> when (body2) {
                is FixedSphereBody -> call(bncSphereVsCylinder, body2, body1, swap = true)
                is FixedCylinderBody -> call(bncCylinderVsCylinder, body1, body2)
                is FixedCuboidBody -> call(bncCylinderVsCuboid, body1, body2)
                is FixedCapsuleBody -> call(bncCylinderVsCapsule, body1, body2)
            }
            is FixedCuboidBody -> when (body2) {
                is FixedSphereBody -> call(bncSphereVsCuboid, body2, body1, swap = true)
                is FixedCylinderBody -> call(bncCylinderVsCuboid, body2, body1, swap = true)
                is FixedCuboidBody -> throw NotImplementedError() // FIXME
                is FixedCapsuleBody -> call(bncCapsuleVsCuboid, body2, body1, swap = true)
            }
            is FixedCapsuleBody -> when (body2) {
                is FixedSphereBody -> call(bncSphereVsCapsule, body2, body1, swap = true)
                is FixedCylinderBody -> call(bncCylinderVsCapsule, body2, body1, swap = true)
                is FixedCuboidBody -> call(bncCapsuleVsCuboid, body1, body2)
                is FixedCapsuleBody -> call(bncCapsuleVsCapsule, body1, body2)
            }
        }
    }
}
