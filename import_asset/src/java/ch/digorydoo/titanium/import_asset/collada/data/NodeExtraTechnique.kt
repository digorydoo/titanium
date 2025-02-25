package ch.digorydoo.titanium.import_asset.collada.data

import ch.digorydoo.kutils.string.indentLines

class NodeExtraTechnique {
    var profile = ""
    var layer: SidValue? = null
    var roll: SidValue? = null
    var tipX: SidValue? = null
    var tipY: SidValue? = null
    var tipZ: SidValue? = null

    override fun toString() =
        indentLines(
            arrayOf(
                "NodeExtraTechnique {",
                "profile = \"$profile\"",
                "layer = ${indentLines("$layer")}",
                "roll = ${indentLines("$roll")}",
                "tipX = ${indentLines("$tipX")}",
                "tipY = ${indentLines("$tipY")}",
                "tipZ = ${indentLines("$tipZ")}",
                "}",
            )
        )
}
