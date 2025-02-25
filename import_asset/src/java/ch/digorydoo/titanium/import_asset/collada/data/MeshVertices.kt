package ch.digorydoo.titanium.import_asset.collada.data

import ch.digorydoo.kutils.string.indentLines

class MeshVertices {
    var id = ""
    val input = mutableListOf<MeshInput>()

    override fun toString() =
        indentLines(
            arrayOf(
                "MeshVertices {",
                "id = \"$id\"",
                "input = [${indentLines(input.joinToString(", "))}]",
                "}"
            )
        )
}
