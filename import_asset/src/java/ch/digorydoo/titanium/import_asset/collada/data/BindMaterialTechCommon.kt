package ch.digorydoo.titanium.import_asset.collada.data

import ch.digorydoo.kutils.string.indentLines

class BindMaterialTechCommon {
    var instanceMaterial: InstanceMaterial? = null

    override fun toString() =
        indentLines(
            arrayOf(
                "BindMaterialTechCommon {",
                "instanceMaterial = ${indentLines("$instanceMaterial")}",
                "}",
            )
        )
}
