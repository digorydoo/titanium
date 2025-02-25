package ch.digorydoo.titanium.import_asset.collada.data

import ch.digorydoo.kutils.string.indentLines

class InstanceMaterial {
    var symbol = ""
    var target = ""
    var bindVertexInput: BindVertexInput? = null

    override fun toString() =
        indentLines(
            arrayOf(
                "InstanceMaterial {",
                "symbol = \"$symbol\"",
                "target = \"$target\"",
                "bindVertexInput = ${indentLines("$bindVertexInput")}",
                "}",
            )
        )
}
