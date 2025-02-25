package ch.digorydoo.titanium.import_asset

import ch.digorydoo.kutils.string.lpad
import ch.digorydoo.kutils.string.toDelimited
import ch.digorydoo.titanium.engine.mesh.MeshMaterial

abstract class WriterStats {
    abstract val numBytes: Int
    abstract val numGeometries: Int
    abstract val numPt3f: Int
    abstract val numPt2f: Int

    var numGeometriesReused = 0
    var numNodes = 0
    var numPositions = 0
    var numNormals = 0
    var numTexCoords = 0
    var numMatrices = 0

    private val materialsUsed = mutableMapOf<MeshMaterial, Int>()

    fun didUseMaterial(mat: MeshMaterial) {
        val count = materialsUsed[mat] ?: 0
        materialsUsed[mat] = count + 1
    }

    fun clear() {
        numGeometriesReused = 0
        numNodes = 0
        numPositions = 0
        numNormals = 0
        numTexCoords = 0
        numMatrices = 0
        materialsUsed.clear()
    }

    private fun format(i: Int) =
        lpad(i.toDelimited(), 8)

    fun minimal() =
        "${numBytes.toDelimited()} bytes"

    fun basicStats(): String {
        val stats = arrayOf(
            "${format(numBytes)} bytes for",
            "${format(numGeometries)} geometries of which",
            "${format(numGeometriesReused)} were reused,",
            "${format(numNodes)} nodes,",
            "${format(numPositions)} positions,",
            "${format(numNormals)} normals,",
            "${format(numTexCoords)} texCoords,",
            "${format(numMatrices)} matrices,",
            "${format(numPt3f)} distinct Point3f,",
            "${format(numPt2f)} distinct Point2f",
        ).joinToString("\n")
        return "Statistics:\n$stats"
    }

    fun materialStats(): String {
        val stats = if (materialsUsed.isEmpty()) {
            "none"
        } else {
            "\n" + materialsUsed.entries.joinToString("\n") { (mat, count) ->
                "${format(count)} ${mat.name}"
            }
        }
        return "Materials: $stats"
    }
}
