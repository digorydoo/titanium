package ch.digorydoo.titanium.import_asset.collada.data

import ch.digorydoo.kutils.string.indentLines

class Material {
    var id = ""
    var name = ""
    var instanceEffect: Instance? = null

    override fun toString() =
        indentLines(
            arrayOf(
                "Material {",
                "id = \"$id\"",
                "name = \"$name\"",
                "instanceEffect = ${indentLines("$instanceEffect")}",
                "}",
            )
        )
}
