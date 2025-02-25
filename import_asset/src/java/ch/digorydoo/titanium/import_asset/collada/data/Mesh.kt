package ch.digorydoo.titanium.import_asset.collada.data

import ch.digorydoo.kutils.string.indentLines

class Mesh {
    val sources = mutableListOf<MeshSource>()
    var vertices: MeshVertices? = null
    var triangles: MeshTriangles? = null

    override fun toString() =
        indentLines(
            arrayOf(
                "Mesh {",
                "sources = [${indentLines(sources.joinToString(", "))}]",
                "vertices = ${indentLines("$vertices")}",
                "triangles = ${indentLines("$triangles")}",
                "}",
            )
        )
}
