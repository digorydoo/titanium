package ch.digorydoo.titanium.import_asset.collada.data

import ch.digorydoo.kutils.string.indentLines

class LightTechCommon {
    var directional: LightDirectional? = null
    var point: LightTechPoint? = null

    override fun toString() =
        indentLines(
            arrayOf(
                "LightTechCommon {",
                "directional = ${indentLines("$point")}",
                "point = ${indentLines("$point")}",
                "}",
            )
        )
}
