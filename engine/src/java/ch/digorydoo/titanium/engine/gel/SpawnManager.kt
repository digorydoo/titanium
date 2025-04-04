package ch.digorydoo.titanium.engine.gel

import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.kutils.string.generateId
import ch.digorydoo.kutils.string.initCap
import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.file.GelListFileReader
import ch.digorydoo.titanium.engine.file.GelListFileWriter
import kotlin.math.max

abstract class SpawnManager {
    class SpawnPtAndDistance(val spawnPt: SpawnPt, val spawnPtIdx: Int, val distance: Double)

    abstract val spawnObjTypeList: List<String>

    private val spawnPts = mutableListOf<SpawnPt>()

    val numSpawnPts get() = spawnPts.size

    fun add(rawSpawnPt: Map<String, String>) {
        spawnPts.add(App.factory.createSpawnPt(rawSpawnPt))
    }

    fun spawnPtAt(index: Int) =
        spawnPts.getOrNull(index)

    fun findSpawnPt(id: String) = when {
        id.isEmpty() -> null // there may be ad-hoc spawn pts with empty id, and we disallow finding them like this
        else -> spawnPts.find { it.id == id }
    }

    fun generateUniqueId(spawnObjType: String): String {
        // The random part should come first to improve performance when looking for a specific id.

        val suffix = initCap(spawnObjType, forceRestLowercase = true)
            .filter { it in 'a' .. 'z' || it in 'A' .. 'Z' || it in '0' .. '9' }
            .take(10)

        val prefixLen = max(0, 10 - suffix.length) + 3 // 3 <= prefixLen <= 13 since suffix.length <= 10
        var attempt = 20

        while (attempt > 0) {
            attempt--
            // generateId does not use numbers for the first character
            val id = generateId(prefixLen) + suffix
            if (findSpawnPt(id) == null) return id
        }

        // This is very unlikely, since generateId uses many characters
        throw Exception("Failed to generate unique id for spawn pt within a reasonable number of attempts")
    }

    fun despawn(pt: SpawnPt) {
        App.content.forEachGelInMainLayerIncludingNew { gel ->
            if (gel.spawnPt == pt) {
                gel.setZombie()
            }
        }
    }

    fun despawnAndRemove(pt: SpawnPt) {
        if (!spawnPts.remove(pt)) {
            Log.warn(TAG, "Spawn point to be removed was not found in the list of spawn points!")
        }

        despawn(pt)
    }

    fun clear() {
        spawnPts.clear()
    }

    fun load(gelListFileName: String) {
        spawnPts.clear()
        GelListFileReader.readFile(gelListFileName, spawnPts)
    }

    fun save(gelListFileName: String) {
        GelListFileWriter.writeFile(gelListFileName, spawnPts)
    }

    // Called on each frame
    fun spawnGels() {
        spawnPts.forEach {
            try {
                if (it.shouldSpawn()) {
                    val dist = it.pos.distanceTo(App.camera.sourcePos)
                    Log.info(TAG, "Spawning $it, spawnPt distance=${dist.toInt()}m")
                    it.spawn()
                }
            } catch (e: Exception) {
                Log.error(TAG, "Failed to spawn $it: ${e.message}\n${e.stackTraceToString()}")
            }
        }
    }

    fun indexOfClosestSpawnPt(pt: Point3f): Int {
        var resultIdx = -1
        var closestSqrDist = Double.MAX_VALUE

        spawnPts.forEachIndexed { i, spawnPt ->
            val sd = spawnPt.pos.sqrDistanceTo(pt)

            if (sd < closestSqrDist) {
                resultIdx = i
                closestSqrDist = sd
            }
        }

        return resultIdx
    }

    fun findClosestSpawnPts(fromPt: Point3f, maxCount: Int = Int.MAX_VALUE): List<SpawnPtAndDistance> {
        val result = mutableListOf<SpawnPtAndDistance>()

        for ((spawnPtIdx, pt) in spawnPts.withIndex()) {
            val d = fromPt.distanceTo(pt.pos)
            val idx = result.indexOfFirst { it.distance > d }

            // If result has a size of maxCount already, add the element only if its index is smaller

            if (result.size < maxCount || idx < maxCount - 1) {
                val spad = SpawnPtAndDistance(pt, spawnPtIdx, d)

                if (idx < 0) {
                    result.add(spad)
                } else {
                    result.add(idx, spad)
                }
            }
        }

        return result.take(maxCount)
    }

    companion object {
        private val TAG = Log.Tag("SpawnManager")
    }
}
