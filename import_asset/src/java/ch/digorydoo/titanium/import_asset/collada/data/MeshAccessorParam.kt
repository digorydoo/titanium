package ch.digorydoo.titanium.import_asset.collada.data

import ch.digorydoo.kutils.string.indentLines

class MeshAccessorParam {
    var name = ""
    var type = ""

    override fun toString() =
        indentLines(
            arrayOf(
                "MeshAccessorParam {",
                "name = \"$name\"",
                "type = \"$type\"",
                "}",
            )
        )
}
