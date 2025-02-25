package ch.digorydoo.titanium.import_asset.collada.data

import ch.digorydoo.kutils.string.indentLines

class LambertDiffuse {
    var color: Color? = null
    var texture: Texture? = null

    override fun toString() =
        indentLines(
            arrayOf(
                "LambertDiffuse {",
                "color = ${indentLines("$color")}",
                "texture = ${indentLines("$texture")}",
                "}",
            )
        )
}
