package ch.digorydoo.titanium.engine.gel

import ch.digorydoo.kutils.math.toDegrees
import ch.digorydoo.kutils.math.toRadians
import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.gel.GelLayer.LayerKind
import ch.digorydoo.titanium.engine.ui.choice.BoolChoice
import ch.digorydoo.titanium.engine.ui.choice.Choice
import ch.digorydoo.titanium.engine.ui.choice.FloatChoice
import kotlin.math.sqrt

abstract class SpawnPt private constructor(
    val id: String, // may be empty
    val spawnObjTypeAsString: String,
    val pos: MutablePoint3f,
    var rotation: Float,
    canCollide: Boolean, // affects both the GelLayer and GraphicElement::canCollide
    private var autoSpawn: Boolean, // true = spawn gel when pt is close to camera
    private var autoDespawn: Boolean, // true = gel will be removed when it's too far from camera
    private var maxCameraSqrDistForAutoSpawn: Double, // also affects min distance for despawn
    private var preventRespawnWhenClose: Boolean, // true = do not respawn if camera is close to spawn pt
) {
    constructor(raw: Map<String, String>): this(
        id = raw["id"] ?: "",
        spawnObjTypeAsString = raw["spawnObjType"]!!,
        pos = MutablePoint3f(
            raw["x"]?.toFloat() ?: 0.0f,
            raw["y"]?.toFloat() ?: 0.0f,
            raw["z"]?.toFloat() ?: 0.0f,
        ),
        rotation = raw["rotation"]?.toFloat() ?: 0.0f,
        canCollide = raw["canCollide"]?.toBoolean() ?: true,
        autoSpawn = raw["autoSpawn"]?.toBoolean() ?: true,
        autoDespawn = raw["autoDespawn"]?.toBoolean() ?: true,
        maxCameraSqrDistForAutoSpawn = raw["maxCamDistForSpawn"]?.toDouble()?.let { it * it } ?: (100.0 * 100.0),
        preventRespawnWhenClose = raw["preventRespawnWhenClose"]?.toBoolean() ?: false,
    )

    var canCollide = canCollide; private set
    private var spawnCount = 0
    var sessionTimeWhenSpawned = Float.NaN; private set
    private var suppressRespawnUntilSessionTime = Float.NaN

    open fun serialize(): MutableMap<String, String> {
        val result = mutableMapOf<String, String>()
        // spawnObjType is not serialized; GelListFileWriter treats this specially
        if (id.isNotEmpty()) result["id"] = id
        result["x"] = pos.x.toString()
        result["y"] = pos.y.toString()
        result["z"] = pos.z.toString()
        result["rotation"] = rotation.toString()
        result["canCollide"] = "$canCollide"
        result["autoSpawn"] = "$autoSpawn"
        result["autoDespawn"] = "$autoDespawn"
        result["maxCamDistForSpawn"] = sqrt(maxCameraSqrDistForAutoSpawn).toString()
        result["preventRespawnWhenClose"] = "$preventRespawnWhenClose"
        return result
    }

    // The position is treated especially (see SpawnPtMenu)
    open fun getEditorChoices(onChange: () -> Unit): MutableList<Choice> =
        mutableListOf(
            FloatChoice(
                "Rotation",
                initialValue = rotation.toDegrees(),
                step = 45.0f,
                smallStep = 5.0f,
                modulo = 360f
            ) {
                rotation = it.toRadians()
                onChange()
            },
            BoolChoice("Auto spawn", initialValue = autoSpawn) {
                autoSpawn = it
                onChange()
            },
            BoolChoice("Auto despawn", initialValue = autoDespawn) {
                autoDespawn = it
                onChange()
            },
            FloatChoice(
                "Camera distance for spawn/despawn",
                initialValue = sqrt(maxCameraSqrDistForAutoSpawn).toFloat(),
                step = 5.0f,
                smallStep = 1.0f,
            ) {
                maxCameraSqrDistForAutoSpawn = it.toDouble() * it
                onChange()
            },
            BoolChoice("Prevent respawn when close", initialValue = preventRespawnWhenClose) {
                preventRespawnWhenClose = it
                onChange()
            },
            BoolChoice("Can collide", initialValue = canCollide) {
                canCollide = it
                onChange()
            }
        )

    protected abstract fun createGel(): GraphicElement

    fun spawn(): GraphicElement {
        val gel = createGel()

        val layer = if (canCollide) LayerKind.MAIN_COLLIDABLE else LayerKind.MAIN_NON_COLLIDABLE
        App.content.add(gel, layer)

        // The spawnCount can become > 1 if spawnMgr.despawn is followed by a call to spawn. Despawn only marks the
        // gel as a zombie, and the spawnCount is decremented when didRemoveGel is called. Also, multiple calls to
        // spawn is not prevented and may have some use (e.g. a cannon emitting cannonballs).
        spawnCount++
        sessionTimeWhenSpawned = App.time.sessionTime
        suppressRespawnUntilSessionTime = Float.NaN
        return gel
    }

    fun shouldSpawn(): Boolean {
        if (!autoSpawn) {
            return false // automatic spawning is disabled
        }

        if (spawnCount > 0) {
            return false // the gel is still alive
        }

        val cameraSqrDistance = pos.sqrDistanceTo(App.camera.sourcePos)

        if (cameraSqrDistance > maxCameraSqrDistForAutoSpawn) {
            return false // spawn pt is too far away
        }

        if (!suppressRespawnUntilSessionTime.isNaN()) {
            if (App.time.sessionTime < suppressRespawnUntilSessionTime) {
                return false // respawning is suppressed
            } else if (preventRespawnWhenClose && cameraSqrDistance < MIN_CAMERA_SQRDISTANCE_FOR_RESPAWN) {
                // It does not look good if an enemy respawns right in front of the camera. We wait until the camera
                // has moved away from the spawn point. Just wait a second, then try again!
                suppressRespawnUntilSessionTime = App.time.sessionTime + 1.0f
                return false
            } else {
                suppressRespawnUntilSessionTime = Float.NaN
            }
        }

        if (!sessionTimeWhenSpawned.isNaN()) {
            val secondsSinceSpawned = App.time.sessionTime - sessionTimeWhenSpawned

            if (secondsSinceSpawned < MIN_SECONDS_UNTIL_RESPAWN) {
                return false // need to wait
            }
        }

        return true
    }

    // Gels may call this on their spawnPt to prevent respawning for a certain amount of time, e.g. when an enemy is
    // defeated. If called multiple times, the longest duration will be used.
    @Suppress("unused")
    fun suppressRespawn(durationInSeconds: Float) {
        val until = App.time.sessionTime + durationInSeconds

        if (suppressRespawnUntilSessionTime.isNaN() || suppressRespawnUntilSessionTime < until) {
            suppressRespawnUntilSessionTime = until
        }
    }

    fun canAutoDespawn(sqrDistanceToCamera: Double): Boolean {
        if (!autoDespawn) return false

        val minCameraSqrDistForDespawn = maxCameraSqrDistForAutoSpawn + SPAWN_DESPAWN_SQRDISTANCE
        if (sqrDistanceToCamera <= minCameraSqrDistForDespawn) return false

        if (!sessionTimeWhenSpawned.isNaN()) {
            // Constantly despawning and respawning may impact performance when spawning involves loading
            // objects. That's why we keep the gel alive a little longer.
            val secondsSinceSpawned = App.time.sessionTime - sessionTimeWhenSpawned

            if (secondsSinceSpawned < MIN_SECONDS_UNTIL_DESPAWN) {
                return false
            }
        }

        return true
    }

    fun didRemoveGel() {
        require(spawnCount > 0) { "Spawn count out of sync: $spawnCount" }
        spawnCount--
        Log.info(TAG, "didRemoveGel: SpawnPt $spawnObjTypeAsString: spawnCount=$spawnCount")
    }

    override fun toString() =
        "SpawnPt(id=$id, type=$spawnObjTypeAsString)"

    companion object {
        private val TAG = Log.Tag("SpawnPt")

        private const val MIN_SECONDS_UNTIL_DESPAWN = 10.0f
        private const val MIN_SECONDS_UNTIL_RESPAWN = 1.0f
        private const val SPAWN_DESPAWN_SQRDISTANCE = 5.0 * 5.0 // sqr distance from auto-spawn max to despawn min
        private const val MIN_CAMERA_SQRDISTANCE_FOR_RESPAWN = 30.0 * 30.0 // threshold of preventRespawnWhenClose
    }
}
