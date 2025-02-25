package ch.digorydoo.titanium.import_asset.collada.data

import ch.digorydoo.kutils.string.indentLines

class SkinSource {
    var id = "" // joint inputs will refer to this id
    var nameArray: NameArray? = null
    var techCommon: SkinTechCommon? = null
    var floatArray: MeshFloatArray? = null

    override fun toString() =
        indentLines(
            arrayOf(
                "SkinSource {",
                "id = \"$id\"",
                "nameArray = ${indentLines("$nameArray")}",
                "techCommon = ${indentLines("$techCommon")}",
                "floatArray = ${indentLines("$floatArray")}",
                "}",
            )
        )
}
