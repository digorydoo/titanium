package ch.digorydoo.titanium.import_asset.collada.data

import ch.digorydoo.kutils.string.indentLines

class SkinTechCommonAccessor {
    var source = ""
    var count = 0
    var stride = 0
    var param: MeshAccessorParam? = null

    override fun toString() =
        indentLines(
            arrayOf(
                "SkinTechCommonAccessor {",
                "source = \"$source\"",
                "count = $count",
                "stride = $stride",
                "param = ${indentLines("$param")}",
                "}",
            )
        )
}
