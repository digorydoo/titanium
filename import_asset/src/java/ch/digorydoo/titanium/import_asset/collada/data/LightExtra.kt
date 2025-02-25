package ch.digorydoo.titanium.import_asset.collada.data

import ch.digorydoo.kutils.string.indentLines

class LightExtra {
    var technique: LightExtraTechnique? = null

    override fun toString() =
        indentLines(
            arrayOf(
                "LightExtra {",
                "technique = ${indentLines("$technique")}",
                "}",
            )
        )
}
