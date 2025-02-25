package ch.digorydoo.titanium.import_asset.collada.data

import ch.digorydoo.kutils.string.indentLines

class BindVertexInput {
    var semantic = ""
    var inputSemantic = ""
    var inputSet = 0

    override fun toString() =
        indentLines(
            arrayOf(
                "BindVertexInput {",
                "semantic = \"$semantic\"",
                "inputSemantic = \"$inputSemantic\"",
                "inputSet = $inputSet",
                "}",
            )
        )
}
