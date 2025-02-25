package ch.digorydoo.titanium.import_asset.collada.data

import ch.digorydoo.kutils.string.indentLines

class EffectNewParam {
    var sid = ""
    var surface2D: EffectSurface? = null
    var sampler2D: EffectSampler2D? = null

    override fun toString() =
        indentLines(
            arrayOf(
                "EffectNewParam {",
                "sid = \"$sid\"",
                "surface2D = ${indentLines("$surface2D")}",
                "sampler2D = ${indentLines("$sampler2D")}",
                "}",
            )
        )
}
