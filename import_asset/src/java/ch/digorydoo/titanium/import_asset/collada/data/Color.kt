package ch.digorydoo.titanium.import_asset.collada.data

import ch.digorydoo.kutils.string.indentLines

class Color {
    var sid = ""
    var red = 0.0f
    var green = 0.0f
    var blue = 0.0f
    var alpha: Float? = null

    override fun toString() =
        indentLines(
            arrayOf(
                "Color {",
                "sid = \"$sid\"",
                "red = $red",
                "green = $green",
                "blue = $blue",
                "alpha = $alpha",
                "}",
            )
        )
}
