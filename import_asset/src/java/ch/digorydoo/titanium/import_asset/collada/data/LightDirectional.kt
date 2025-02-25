package ch.digorydoo.titanium.import_asset.collada.data

import ch.digorydoo.kutils.string.indentLines

class LightDirectional {
    var color: Color? = null

    override fun toString() =
        indentLines(
            arrayOf(
                "LightDirectional {",
                "color = ${indentLines("$color")}",
                "}",
            )
        )
}
