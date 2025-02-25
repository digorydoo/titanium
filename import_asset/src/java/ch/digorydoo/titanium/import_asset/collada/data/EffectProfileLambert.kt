package ch.digorydoo.titanium.import_asset.collada.data

import ch.digorydoo.kutils.string.indentLines

class EffectProfileLambert {
    var diffuse: LambertDiffuse? = null
    var emission: LambertColor? = null
    var indexOfRefraction: LambertValue? = null
    var reflectivity: LambertValue? = null
    var transparent: LambertTransparent? = null

    override fun toString() =
        indentLines(
            arrayOf(
                "EffectProfileLambert {",
                "diffuse = ${indentLines("$diffuse")}",
                "emission = ${indentLines("$emission")}",
                "indexOfRefraction = ${indentLines("$indexOfRefraction")}",
                "reflectivity = ${indentLines("$reflectivity")}",
                "transparent = ${indentLines("$transparent")}",
                "}",
            )
        )
}
