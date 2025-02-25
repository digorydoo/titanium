package ch.digorydoo.titanium.import_asset.collada.data

import ch.digorydoo.kutils.string.indentLines

class VertexWeights {
    var count = 0
    var jointInput: MeshInput? = null
    var weightInput: MeshInput? = null
    val vcount = mutableListOf<Int>()
    val vertices = mutableListOf<Int>()

    override fun toString() =
        indentLines(
            arrayOf(
                "VertexWeights {",
                "count = $count",
                "jointInput = ${indentLines("$jointInput")}",
                "weightInput = ${indentLines("$weightInput")}",
                "vcount.size = ${vcount.size}",
                "vertices.size = ${vertices.size}",
                "}",
            )
        )
}
