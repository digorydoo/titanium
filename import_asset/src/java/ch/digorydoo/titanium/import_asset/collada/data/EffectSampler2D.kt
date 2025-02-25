package ch.digorydoo.titanium.import_asset.collada.data

import ch.digorydoo.kutils.string.indentLines

class EffectSampler2D {
    var source: SidValue? = null

    override fun toString() =
        indentLines(
            arrayOf(
                "EffectSampler2D {",
                "source = ${indentLines("$source")}",
                "}",
            )
        )
}
