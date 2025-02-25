package ch.digorydoo.titanium.import_asset.collada.data

import ch.digorydoo.kutils.string.indentLines

class MeshTriangles {
    var count = 0
    val input = mutableListOf<MeshInput>()
    var p: MeshIntArray? = null
    var material = ""

    override fun toString() =
        indentLines(
            arrayOf(
                "MeshTriangles {",
                "count = $count",
                "input = [${indentLines(input.joinToString(", "))}]",
                "p = ${indentLines("$p")}",
                "material = \"$material\"",
                "}",
            )
        )
}
