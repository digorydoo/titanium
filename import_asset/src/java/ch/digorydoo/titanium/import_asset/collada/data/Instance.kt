package ch.digorydoo.titanium.import_asset.collada.data

import ch.digorydoo.kutils.string.indentLines

class Instance {
    var url = ""
    var name = ""

    override fun toString() =
        indentLines(
            arrayOf(
                "Instance {",
                "url = \"$url\"",
                "name = \"$name\"",
                "}",
            )
        )
}
