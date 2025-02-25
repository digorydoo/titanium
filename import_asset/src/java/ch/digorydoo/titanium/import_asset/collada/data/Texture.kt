package ch.digorydoo.titanium.import_asset.collada.data

import ch.digorydoo.kutils.string.indentLines

class Texture {
    var texName = ""
    var texCoordName = ""

    override fun toString() =
        indentLines(
            arrayOf(
                "Texture {",
                "texName = \"$texName\"",
                "texCoordName = \"$texCoordName\"",
                "}",
            )
        )
}
