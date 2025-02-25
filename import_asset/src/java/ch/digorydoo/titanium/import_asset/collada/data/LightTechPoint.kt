package ch.digorydoo.titanium.import_asset.collada.data

import ch.digorydoo.kutils.string.indentLines

class LightTechPoint {
    var color: Color? = null
    var constantAttenuation = 0.0f
    var linearAttenuation = 0.0f
    var quadraticAttenuation = 0.0f

    override fun toString() =
        indentLines(
            arrayOf(
                "LightTechPoint {",
                "color = ${indentLines("$color")}",
                "constantAttenuation = $constantAttenuation",
                "linearAttenuation = $linearAttenuation",
                "quadraticAttenuation = $quadraticAttenuation",
                "}",
            )
        )
}
