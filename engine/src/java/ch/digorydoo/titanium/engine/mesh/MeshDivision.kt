package ch.digorydoo.titanium.engine.mesh

import ch.digorydoo.kutils.string.indentLines
import ch.digorydoo.kutils.utils.Log

class MeshDivision(
    val nodes: List<MeshNode>,
    var material: MeshMaterial, // must be a var, see CursorGel
) {
    /**
     * Because MeshDivision is a separation of mesh data by material, the same childId may reappear among divisions.
     * Within a division, the childId should be unique; if it is not, the original Blender file uses the same name for
     * multiple parts (which can easily happen).
     */
    fun find(childId: String): MeshNode? {
        if (childId.isEmpty()) return null

        var found: MeshNode? = null

        nodes.forEach { node ->
            val foundHere = node.find(childId)

            if (foundHere != null) {
                if (found != null) {
                    Log.warn(TAG, "Mesh node id is not unique in division: $childId")
                } else {
                    found = foundHere
                }
            }
        }

        return found
    }

    override fun toString() =
        indentLines(
            arrayOf(
                "MeshDivision {",
                "nodes = [${indentLines(nodes.joinToString(", "))}]",
                "material = $material",
                "}",
            )
        )

    companion object {
        private val TAG = Log.Tag("MeshDivision")
    }
}
