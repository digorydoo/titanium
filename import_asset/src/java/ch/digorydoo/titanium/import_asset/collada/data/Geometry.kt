package ch.digorydoo.titanium.import_asset.collada.data

import ch.digorydoo.kutils.string.indentLines

class Geometry {
    var id = ""
    var name = ""
    var mesh: Mesh? = null

    override fun toString() =
        indentLines(
            arrayOf(
                "Geometry {",
                "id = \"$id\"",
                "name = \"$name\"",
                "mesh = ${indentLines("$mesh")}",
                "}",
            )
        )
}
