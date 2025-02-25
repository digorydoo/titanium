package ch.digorydoo.titanium.import_asset.collada.data

import ch.digorydoo.kutils.string.indentLines

class Animation {
    var id = ""
    var name = ""

    override fun toString() =
        indentLines(
            arrayOf(
                "Animation {",
                "id = \"$id\"",
                "name = \"$name\"",
                "}",
            )
        )
}
