package ch.digorydoo.titanium.import_asset.collada.data

import ch.digorydoo.kutils.string.indentLines

class Skin {
    var source = "" // #id of Mesh
    val sources = mutableListOf<SkinSource>()
    var bindShapeMatrix: Matrix? = null
    var joints: SkinJoints? = null
    var vertexWeights: VertexWeights? = null

    override fun toString() =
        indentLines(
            arrayOf(
                "Skin {",
                "source = \"$source\"",
                "sources = [${indentLines(sources.joinToString(", "))}]",
                "bindShapeMatrix = ${indentLines("$bindShapeMatrix")}",
                "joints = ${indentLines("$joints")}",
                "vertexWeights = ${indentLines("$vertexWeights")}",
                "}",
            )
        )
}
