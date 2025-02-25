package ch.digorydoo.titanium.import_asset.collada.data

import ch.digorydoo.kutils.string.indentLines

class BindMaterial {
    var techCommon: BindMaterialTechCommon? = null

    override fun toString() =
        indentLines(
            arrayOf(
                "BindMaterial {",
                "techCommon = ${indentLines("$techCommon")}",
                "}",
            )
        )
}
