package ch.digorydoo.titanium.engine.physics.strategy

import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.kutils.point.Point3i
import ch.digorydoo.titanium.engine.brick.IBrickFaceCoveringRetriever
import ch.digorydoo.titanium.engine.physics.MutableHitResult
import ch.digorydoo.titanium.engine.physics.rigid_body.RigidBody

internal abstract class CollisionCheckStrategy<B1: RigidBody, B2: RigidBody> {
    abstract fun check(
        body1: B1,
        centreX1: Float,
        centreY1: Float,
        centreZ1: Float,
        body1IsBrick: Boolean,
        body2: B2,
        centreX2: Float,
        centreY2: Float,
        centreZ2: Float,
        body2IsBrick: Boolean,
        bfcr: IBrickFaceCoveringRetriever?,
        brickCoords: Point3i?,
        outHit: MutableHitResult?,
    ): Boolean

    fun check(
        body1: B1,
        centre1: Point3f,
        body1IsBrick: Boolean,
        body2: B2,
        centre2: Point3f,
        body2IsBrick: Boolean,
        bfcr: IBrickFaceCoveringRetriever?,
        brickCoords: Point3i?,
        outHit: MutableHitResult?,
    ) = check(
        body1,
        centre1.x,
        centre1.y,
        centre1.z,
        body1IsBrick,
        body2,
        centre2.x,
        centre2.y,
        centre2.z,
        body2IsBrick,
        bfcr,
        brickCoords,
        outHit,
    )

    fun check(body1: B1, centre1: Point3f, body2: B2, centre2: Point3f, outHit: MutableHitResult? = null) =
        check(
            body1,
            centre1.x,
            centre1.y,
            centre1.z,
            body1IsBrick = false,
            body2,
            centre2.x,
            centre2.y,
            centre2.z,
            body2IsBrick = false,
            bfcr = null,
            brickCoords = null,
            outHit,
        )
}
