package ch.digorydoo.titanium.import_asset.collada.data

import ch.digorydoo.kutils.string.indentLines

class Contributor {
    var author = ""
    var authoringTool = ""

    override fun toString() =
        indentLines(
            arrayOf(
                "Contributor {",
                "author = \"${author}\"",
                "authoringTool = \"${authoringTool}\"",
                "}"
            )
        )
}
