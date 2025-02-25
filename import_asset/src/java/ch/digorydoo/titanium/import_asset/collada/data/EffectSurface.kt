package ch.digorydoo.titanium.import_asset.collada.data

import ch.digorydoo.kutils.string.indentLines

class EffectSurface {
    var type = ""
    var initFrom: SidValue? = null

    override fun toString() =
        indentLines(
            arrayOf(
                "EffectSurface {",
                "type = \"$type\"",
                "initFrom = ${indentLines("$initFrom")}",
                "}",
            )
        )
}
