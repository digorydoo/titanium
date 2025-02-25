package ch.digorydoo.titanium.import_asset.collada.data

import ch.digorydoo.kutils.string.indentLines

class MeshTechCommon {
    var accessor: MeshTechCommonAccessor? = null

    override fun toString() =
        indentLines(
            arrayOf(
                "MeshTechCommon {",
                "accessor = ${indentLines("$accessor")}",
                "}",
            )
        )
}
