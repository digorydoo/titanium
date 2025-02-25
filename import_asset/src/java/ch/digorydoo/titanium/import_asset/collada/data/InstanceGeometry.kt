package ch.digorydoo.titanium.import_asset.collada.data

import ch.digorydoo.kutils.string.indentLines

class InstanceGeometry {
    var url = ""
    var name = ""
    var bindMaterial: BindMaterial? = null

    override fun toString() =
        indentLines(
            arrayOf(
                "InstanceGeometry {",
                "url = \"$url\"",
                "name = \"$name\"",
                "bindMaterial = ${indentLines("$bindMaterial")}",
                "}",
            )
        )
}
