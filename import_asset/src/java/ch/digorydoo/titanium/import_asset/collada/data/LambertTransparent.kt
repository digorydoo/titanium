package ch.digorydoo.titanium.import_asset.collada.data

import ch.digorydoo.kutils.string.indentLines

class LambertTransparent {
    enum class Opaque { A_ONE }

    var color: Color? = null
    var opaque: Opaque? = null

    override fun toString() =
        indentLines(
            arrayOf(
                "LambertTransparent {",
                "color = $color",
                "opaque = $opaque",
                "}",
            )
        )
}
