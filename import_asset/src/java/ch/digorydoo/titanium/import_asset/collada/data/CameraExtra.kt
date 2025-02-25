package ch.digorydoo.titanium.import_asset.collada.data

import ch.digorydoo.kutils.string.indentLines

class CameraExtra {
    var technique: CameraExtraTechnique? = null

    override fun toString() =
        indentLines(
            arrayOf(
                "CameraExtra {",
                "technique = ${indentLines("$technique")}",
                "}",
            )
        )
}
