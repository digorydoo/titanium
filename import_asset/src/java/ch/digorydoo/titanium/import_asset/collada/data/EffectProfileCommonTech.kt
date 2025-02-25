package ch.digorydoo.titanium.import_asset.collada.data

import ch.digorydoo.kutils.string.indentLines

class EffectProfileCommonTech {
    var sid = ""
    var lambert: EffectProfileLambert? = null

    override fun toString() =
        indentLines(
            arrayOf(
                "EffectProfileCommonTech {",
                "sid = \"$sid\"",
                "lambert = ${indentLines("$lambert")}",
                "}",
            )
        )
}
