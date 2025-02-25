package ch.digorydoo.titanium.import_asset.collada.data

import ch.digorydoo.kutils.string.indentLines

class Controller {
    var id = ""
    var name = ""
    var skin: Skin? = null

    override fun toString() =
        indentLines(
            arrayOf(
                "Controller {",
                "id = \"$id\"",
                "name = \"$name\"",
                "skin = ${indentLines("$skin")}",
                "}",
            )
        )
}
