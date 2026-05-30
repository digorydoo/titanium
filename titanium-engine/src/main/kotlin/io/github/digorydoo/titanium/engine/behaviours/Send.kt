package io.github.digorydoo.titanium.engine.behaviours

import ch.digorydoo.kutils.logging.Log
import ch.digorydoo.kutils.vector.MutableVector3f
import ch.digorydoo.kutils.vector.Vector2f
import ch.digorydoo.kutils.vector.Vector3f
import io.github.digorydoo.titanium.engine.gel.Behaviour
import io.github.digorydoo.titanium.engine.gel.GraphicElement
import kotlin.math.sqrt

class Send(private val gel: GraphicElement, private val delegate: Delegate): Behaviour {
    interface Delegate {
        fun onReachTarget() {}
    }

    var active = false; private set
    private var path: List<Vector3f>? = null
    private var pathIdx = -1
    private var immediateTarget: Vector3f? = null
    private var ignoreZ = false
    private var accel = 0.0f
    private var maxSpeed = 0.0f
    private var sqrDistanceToStop = 0.0f
    private var origCollisionsWithGelsSuppressed = false
    private var onReachTarget: (() -> Unit)? = null
    private val direction = MutableVector3f() // direction towards immediateTarget; valid after first step

    fun sendTo(
        target: Vector2f,
        accel: Float = DEFAULT_ACCEL,
        maxSpeed: Float = DEFAULT_MAX_SPEED,
        stopWhenDistanceAtMost: Float = DEFAULT_STOP_DISTANCE,
        suppressCollisionsWithGels: Boolean = false,
        onReachTarget: (() -> Unit)? = null,
    ) {
        start(
            path = null,
            immediateTarget = Vector3f(target.x, target.y, 0.0f),
            ignoreZ = true,
            accel = accel,
            maxSpeed = maxSpeed,
            stopWhenDistanceAtMost = stopWhenDistanceAtMost,
            suppressCollisionsWithGels = suppressCollisionsWithGels,
            onReachTarget = onReachTarget,
        )
    }

    fun sendTo(
        target: Vector3f,
        ignoreZ: Boolean = false,
        accel: Float = DEFAULT_ACCEL,
        maxSpeed: Float = DEFAULT_MAX_SPEED,
        stopWhenDistanceAtMost: Float = DEFAULT_STOP_DISTANCE,
        suppressCollisionsWithGels: Boolean = false,
        onReachTarget: (() -> Unit)? = null,
    ) {
        start(
            path = null,
            immediateTarget = target,
            ignoreZ = ignoreZ,
            accel = accel,
            maxSpeed = maxSpeed,
            stopWhenDistanceAtMost = stopWhenDistanceAtMost,
            suppressCollisionsWithGels = suppressCollisionsWithGels,
            onReachTarget = onReachTarget,
        )
    }

    fun sendAlong(
        path: List<Vector2f>,
        accel: Float = DEFAULT_ACCEL,
        maxSpeed: Float = DEFAULT_MAX_SPEED,
        stopWhenDistanceAtMost: Float = DEFAULT_STOP_DISTANCE,
        suppressCollisionsWithGels: Boolean = false,
        onReachTarget: (() -> Unit)? = null,
    ) {
        start(
            path = path.map { Vector3f(it.x, it.y, 0.0f) },
            immediateTarget = null,
            ignoreZ = true,
            accel = accel,
            maxSpeed = maxSpeed,
            stopWhenDistanceAtMost = stopWhenDistanceAtMost,
            suppressCollisionsWithGels = suppressCollisionsWithGels,
            onReachTarget = onReachTarget,
        )
    }

    fun sendAlong(
        path: List<Vector3f>,
        ignoreZ: Boolean = false,
        accel: Float = DEFAULT_ACCEL,
        maxSpeed: Float = DEFAULT_MAX_SPEED,
        stopWhenDistanceAtMost: Float = DEFAULT_STOP_DISTANCE,
        suppressCollisionsWithGels: Boolean = false,
        onReachTarget: (() -> Unit)? = null,
    ) {
        start(
            path = path,
            immediateTarget = null,
            ignoreZ = ignoreZ,
            accel = accel,
            maxSpeed = maxSpeed,
            stopWhenDistanceAtMost = stopWhenDistanceAtMost,
            suppressCollisionsWithGels = suppressCollisionsWithGels,
            onReachTarget = onReachTarget,
        )
    }

    private fun start(
        path: List<Vector3f>?,
        immediateTarget: Vector3f?,
        ignoreZ: Boolean,
        accel: Float,
        maxSpeed: Float,
        stopWhenDistanceAtMost: Float,
        suppressCollisionsWithGels: Boolean,
        onReachTarget: (() -> Unit)?,
    ) {
        if (active) {
            Log.info(TAG, "Gel $gel is already being sent somewhere, forcing new path")
            stop()
        } else {
            Log.info(TAG, "Gel $gel path walk started")
        }

        if (path != null) {
            require(immediateTarget == null) // internal error if this fails; only one of them can be set

            val actualPath = path.takeIf { it.isNotEmpty() }
                ?: run {
                    Log.warn(TAG, "Gel $gel is being sent along an empty path")
                    listOf(gel.body?.pos ?: gel.pos)
                }

            this.path = actualPath
            this.pathIdx = 0
            this.immediateTarget = actualPath.first()
        } else if (immediateTarget != null) {
            this.path = null
            this.pathIdx = -1
            this.immediateTarget = immediateTarget
        } else {
            throw Error("Either path or immediateTarget must be set")
        }

        this.ignoreZ = ignoreZ
        this.accel = accel
        this.maxSpeed = maxSpeed
        this.sqrDistanceToStop = stopWhenDistanceAtMost * stopWhenDistanceAtMost
        this.onReachTarget = onReachTarget
        origCollisionsWithGelsSuppressed = gel.collisionsWithGelsSuppressed

        if (suppressCollisionsWithGels) {
            gel.collisionsWithGelsSuppressed = true
        }

        active = true
    }

    private fun stop() {
        if (!active) return

        Log.info(TAG, "Gel $gel path walk stopped")
        active = false
        path = null
        pathIdx = -1
        immediateTarget = null
        onReachTarget = null
        gel.collisionsWithGelsSuppressed = origCollisionsWithGelsSuppressed
    }

    private fun targetReached() {
        Log.info(TAG, "Gel $gel reached target")
        delegate.onReachTarget() // inform gel through delegate
        onReachTarget?.invoke() // inform the caller
        stop()
    }

    fun animate() {
        // Future improvement: Compute position where we expect the gel to be on the next step; then compare actual
        // position with expected position.
        // - If difference is too large, gel is probably being pushed by something else. Recompute immediateTarget by
        //   finding the closest path segment and then taking its forward vertex.
        // - If the gel has hardly moved, something might be blocking its way. Call delegate to tell the gel to
        //   recompute its path.
        // For now, let's keep this simple.

        if (!active) return

        val body = gel.body ?: run {
            Log.error(TAG, "Gel $gel doesn't have a body")
            stop()
            return
        }

        var target = immediateTarget ?: run {
            Log.error(TAG, "No immediate target for path of gel $gel")
            stop()
            return
        }

        val path = path
        val pos = body.pos
        var dx: Float
        var dy: Float
        var dz: Float
        var dsqr: Float

        do {
            dx = target.x - pos.x
            dy = target.y - pos.y
            dz = if (ignoreZ) 0.0f else target.z - pos.z
            dsqr = (dx * dx) + (dy * dy) + (dz * dz)

            if (dsqr > sqrDistanceToStop) {
                break // immediate target not reached yet
            }

            target = path?.getOrNull(++pathIdx) ?: run {
                targetReached()
                return
            }

            Log.info(TAG, "Gel $gel reached immediate target, next target on path is [$pathIdx]: $target")
            immediateTarget = target
        } while (true)

        val distance = sqrt(dsqr)
        direction.set(dx / distance, dy / distance, dz / distance) // normalized
        val speedAlongDirection = body.speed.dotProduct(direction)

        if (speedAlongDirection >= maxSpeed) {
            // Not applying any additional force.
            return
        }

        val amountOfForce = (gel.body?.mass ?: 1.0f) * accel
        body.addForce(amountOfForce * direction.x, amountOfForce * direction.y, amountOfForce * direction.z)
    }

    companion object {
        private val TAG = Log.Tag("Send")
        private const val DEFAULT_ACCEL = 30.0f // m/s^2
        private const val DEFAULT_MAX_SPEED = 3.0f // m/s
        private const val DEFAULT_STOP_DISTANCE = 0.1f // m
    }
}
