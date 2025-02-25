package ch.digorydoo.titanium.import_asset.collada.data

import ch.digorydoo.kutils.string.indentLines

class SkinTechCommon {
    var accessor: SkinTechCommonAccessor? = null

    override fun toString() =
        indentLines(
            arrayOf(
                "SkinTechCommon {",
                "accessor = ${indentLines("$accessor")}",
                "}",
            )
        )
}
