package ch.digorydoo.titanium.import_asset.collada.data

import ch.digorydoo.kutils.string.indentLines

class LambertValue {
    var value = 0.0f

    override fun toString() =
        indentLines(
            arrayOf(
                "LambertValue {",
                "value = $value",
                "}",
            )
        )
}
