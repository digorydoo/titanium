package ch.digorydoo.titanium.import_asset.collada.data

import ch.digorydoo.kutils.string.indentLines

class LambertColor {
    var color: Color? = null

    override fun toString() =
        indentLines(
            arrayOf(
                "LambertColor {",
                "color = ${indentLines("$color")}",
                "}",
            )
        )
}
