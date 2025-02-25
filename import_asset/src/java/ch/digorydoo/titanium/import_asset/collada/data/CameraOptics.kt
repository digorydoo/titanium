package ch.digorydoo.titanium.import_asset.collada.data

import ch.digorydoo.kutils.string.indentLines

class CameraOptics {
    var techCommon: CameraOpticsTechCommon? = null

    override fun toString() =
        indentLines(
            arrayOf(
                "CameraOptics {",
                "techCommon = ${indentLines("$techCommon")}",
                "}",
            )
        )
}
