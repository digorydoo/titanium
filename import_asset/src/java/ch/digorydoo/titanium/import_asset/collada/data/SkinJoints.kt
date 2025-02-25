package ch.digorydoo.titanium.import_asset.collada.data

import ch.digorydoo.kutils.string.indentLines

class SkinJoints {
    var jointInput: MeshInput? = null
    var invBindMatrixInput: MeshInput? = null

    override fun toString() =
        indentLines(
            arrayOf(
                "SkinJoints {",
                "jointInput = ${indentLines("$jointInput")}",
                "invBindMatrixInput = ${indentLines("$invBindMatrixInput")}",
                "}",
            )
        )
}
