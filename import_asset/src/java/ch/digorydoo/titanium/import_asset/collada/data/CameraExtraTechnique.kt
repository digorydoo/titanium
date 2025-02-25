package ch.digorydoo.titanium.import_asset.collada.data

import ch.digorydoo.kutils.string.indentLines

class CameraExtraTechnique {
    var profile = ""
    var shiftx: SidValue? = null
    var shifty: SidValue? = null
    var dofDistance: SidValue? = null

    override fun toString() =
        indentLines(
            arrayOf(
                "CameraExtraTechnique {",
                "profile = \"$profile\"",
                "shiftx = ${indentLines("$shiftx")}",
                "shifty = ${indentLines("$shifty")}",
                "dofDistance = ${indentLines("$dofDistance")}",
                "}",
            )
        )
}
