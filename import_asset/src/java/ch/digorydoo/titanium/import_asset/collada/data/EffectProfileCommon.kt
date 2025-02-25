package ch.digorydoo.titanium.import_asset.collada.data

import ch.digorydoo.kutils.string.indentLines

class EffectProfileCommon {
    var technique: EffectProfileCommonTech? = null
    val newParams = mutableListOf<EffectNewParam>()

    override fun toString() =
        indentLines(
            arrayOf(
                "EffectProfileCommon {",
                "technique = ${indentLines("$technique")}",
                "newParams = [${indentLines(newParams.joinToString(", "))}]",
                "}",
            )
        )
}
