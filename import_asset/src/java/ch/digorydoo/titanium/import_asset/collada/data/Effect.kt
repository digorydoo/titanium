package ch.digorydoo.titanium.import_asset.collada.data

import ch.digorydoo.kutils.string.indentLines

class Effect {
    var id = ""
    var profileCommon: EffectProfileCommon? = null

    override fun toString() =
        indentLines(
            arrayOf(
                "Effect {",
                "id = \"$id\"",
                "profileCommon = ${indentLines("$profileCommon")}",
                "}",
            )
        )
}
