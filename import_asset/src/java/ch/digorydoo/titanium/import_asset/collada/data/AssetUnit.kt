package ch.digorydoo.titanium.import_asset.collada.data

import ch.digorydoo.kutils.string.indentLines

class AssetUnit {
    var name = ""
    var meter = ""

    override fun toString() =
        indentLines(
            arrayOf(
                "AssetUnit {",
                "name: \"$name\"",
                "meter: \"$meter\"",
                "}",
            )
        )
}
