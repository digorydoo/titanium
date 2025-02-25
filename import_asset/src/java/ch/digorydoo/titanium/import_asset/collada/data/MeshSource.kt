package ch.digorydoo.titanium.import_asset.collada.data

import ch.digorydoo.kutils.string.indentLines

class MeshSource {
    var id = ""
    var meshFloatArray: MeshFloatArray? = null
    var techCommon: MeshTechCommon? = null

    override fun toString() =
        indentLines(
            arrayOf(
                "MeshSource {",
                "id = \"$id\"",
                "meshFloatArray = ${indentLines("$meshFloatArray")}",
                "techCommon = ${indentLines("$techCommon")}",
                "}",
            )
        )
}
