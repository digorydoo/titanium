package ch.digorydoo.titanium.import_asset.collada.data

import ch.digorydoo.kutils.string.indentLines

class NodeExtra {
    var technique: NodeExtraTechnique? = null

    override fun toString() =
        indentLines(
            arrayOf(
                "NodeExtra {",
                "technique = ${indentLines("$technique")}",
                "}",
            )
        )
}
