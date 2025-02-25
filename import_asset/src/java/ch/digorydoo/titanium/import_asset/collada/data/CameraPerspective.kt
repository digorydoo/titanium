package ch.digorydoo.titanium.import_asset.collada.data

import ch.digorydoo.kutils.string.indentLines

class CameraPerspective {
    var xfov = SidValue()
    var aspectRatio = ""
    var znear = SidValue()
    var zfar = SidValue()

    override fun toString() =
        indentLines(
            arrayOf(
                "CameraPerspective {",
                "xfov = ${indentLines("$xfov")}",
                "aspectRatio = $aspectRatio",
                "znear = ${indentLines("$znear")}",
                "zfar = ${indentLines("$zfar")}",
                "}",
            )
        )
}
