package ch.digorydoo.titanium.import_asset.collada.data

import ch.digorydoo.kutils.string.indentLines

class MeshInput {
    var semantic = ""
    var source = ""
    var offset = 0
    var set = 0

    override fun toString() =
        indentLines(
            arrayOf(
                "MeshInput {",
                "semantic = \"$semantic\"",
                "source = \"$source\"",
                "offset = $offset",
                "set = $set",
                "}",
            )
        )
}
