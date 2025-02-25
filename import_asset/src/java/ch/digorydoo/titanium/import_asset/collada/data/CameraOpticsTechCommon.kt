package ch.digorydoo.titanium.import_asset.collada.data

import ch.digorydoo.kutils.string.indentLines

class CameraOpticsTechCommon {
    var perspective: CameraPerspective? = null

    override fun toString() =
        indentLines(
            arrayOf(
                "CameraOpticsTechCommon {",
                "perspective = ${indentLines("$perspective")}",
                "}",
            )
        )
}
