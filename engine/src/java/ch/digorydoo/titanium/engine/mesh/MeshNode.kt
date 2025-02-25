package ch.digorydoo.titanium.engine.mesh

import ch.digorydoo.kutils.matrix.Matrix4f
import ch.digorydoo.kutils.matrix.MutableMatrix4f
import ch.digorydoo.kutils.string.indentLines
import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.engine.texture.Texture

class MeshNode(
    val id: String,
    val tex: Texture?,
    val transform: Matrix4f?, // transformation as loaded from mesh file
    val geometry: Geometry?, // multiple nodes may point to the same geometry
    val children: List<MeshNode>?,
) {
    val combinedTransform = MutableMatrix4f() // written during rendering
    val finalTransform = MutableMatrix4f() // dito

    fun find(childId: String): MeshNode? {
        if (childId.isEmpty()) return null
        if (childId == id) return this

        var found: MeshNode? = null

        children?.forEach { child ->
            val foundHere = child.find(childId)

            if (foundHere != null) {
                if (found != null) {
                    Log.warn("Mesh node id is not unique in subtree: $childId")
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
                "tex = $tex",
                "transform = \n${indentLines("$transform", 2, false)}",
                "geometry = ${indentLines("$geometry")}",
                "children = [${indentLines(children?.joinToString("\n") ?: "")}]",
                "}",
            )
        )
}
