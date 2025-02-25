package ch.digorydoo.titanium.import_asset.collada.data

import ch.digorydoo.kutils.string.indentLines

class Image {
    var id = ""
    var name = ""
    var path = ""

    override fun toString() =
        indentLines(
            arrayOf(
                "Image {",
                "id = \"$id\"",
                "name = \"$name\"",
                "path = \"$path\"",
                "}",
            )
        )
}
