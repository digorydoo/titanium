package ch.digorydoo.titanium.import_asset.collada.data

import ch.digorydoo.kutils.string.indentLines

class Light {
    var id = ""
    var name = ""
    var techCommon: LightTechCommon? = null
    var extra: LightExtra? = null

    override fun toString() =
        indentLines(
            arrayOf(
                "Light {",
                "id = \"$id\"",
                "name = \"$name\"",
                "techCommon = ${indentLines("$techCommon")}",
                "extra = ${indentLines("$extra")}",
                "}",
            )
        )
}
