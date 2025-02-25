package ch.digorydoo.titanium.import_asset.collada.data

import ch.digorydoo.kutils.string.indentLines

class MeshTechCommonAccessor {
    var source = ""
    var count = 0
    var stride = 0
    val params = mutableListOf<MeshAccessorParam>()

    override fun toString() =
        indentLines(
            arrayOf(
                "MeshTechCommonAccessor {",
                "source = \"$source\"",
                "count = $count",
                "stride = $stride",
                "params = [${indentLines(params.joinToString(", "))}]",
                "}",
            )
        )
}
