package io.github.digorydoo.titanium.engine.mesh

import ch.digorydoo.kutils.logging.Log
import ch.digorydoo.kutils.matrix.Matrix4f
import ch.digorydoo.kutils.matrix.MutableMatrix4f
import ch.digorydoo.kutils.string.indentLines

class MeshNode(
    val id: String,
    val localTransform: Matrix4f?,
    val geometry: MeshGeometry?, // multiple nodes may point to the same geometry
    val children: List<MeshNode>?,
    val skeletonId: Int, // if > 0, this node is a joint of this skeleton
    val jointIdx: Int, // if > 0, this is the index of the joint
) {
    val worldTransform = MutableMatrix4f() // written during rendering

    fun find(childId: String): MeshNode? {
        if (childId.isEmpty()) return null
        if (childId == id) return this

        var found: MeshNode? = null

        children?.forEach { child ->
            val foundHere = child.find(childId)

            if (foundHere != null) {
                if (found != null) {
                    Log.warn(TAG, "Mesh node id is not unique in subtree: $childId")
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
                "MeshNode {",
                "id = \"$id\"",
                "transform = \n${indentLines("$localTransform", 2, false)}",
                "geometry = ${indentLines("$geometry")}",
                "skeletonId=$skeletonId",
                "jointIdx=$jointIdx",
                "children = [${indentLines(children?.joinToString("\n") ?: "")}]",
                "}",
            )
        )

    companion object {
        private val TAG = Log.Tag("MeshNode")
    }
}
