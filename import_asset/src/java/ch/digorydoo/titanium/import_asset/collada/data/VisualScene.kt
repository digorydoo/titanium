package ch.digorydoo.titanium.import_asset.collada.data

import ch.digorydoo.kutils.string.indentLines

class VisualScene {
    var id = ""
    var name = ""
    val nodes = mutableListOf<VisualSceneNode>()

    override fun toString() =
        indentLines(
            arrayOf(
                "VisualScene {",
                "id = \"$id\"",
                "name = \"$name\"",
                "nodes = [${indentLines(nodes.joinToString(", "))}]",
                "}",
            )
        )
}
