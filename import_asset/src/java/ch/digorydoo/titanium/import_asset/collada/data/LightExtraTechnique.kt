package ch.digorydoo.titanium.import_asset.collada.data

import ch.digorydoo.kutils.string.indentLines

class LightExtraTechnique {
    var profile = ""
    var type: SidValue? = null
    var flag: SidValue? = null
    var mode: SidValue? = null
    var gamma: SidValue? = null
    var red: SidValue? = null
    var green: SidValue? = null
    var blue: SidValue? = null
    var shadowR: SidValue? = null
    var shadowG: SidValue? = null
    var shadowB: SidValue? = null
    var energy: SidValue? = null
    var dist: SidValue? = null
    var spotsize: SidValue? = null
    var spotblend: SidValue? = null
    var att1: SidValue? = null
    var att2: SidValue? = null
    var falloffType: SidValue? = null
    var clipStart: SidValue? = null
    var clipEnd: SidValue? = null
    var bias: SidValue? = null
    var soft: SidValue? = null
    var bufSize: SidValue? = null
    var samp: SidValue? = null
    var buffers: SidValue? = null
    var areaShape: SidValue? = null
    var areaSize: SidValue? = null
    var areaSizeY: SidValue? = null
    var areaSizeZ: SidValue? = null

    override fun toString() =
        indentLines(
            arrayOf(
                "LightExtraTechnique {",
                "profile = \"$profile\"",
                "type = ${indentLines("$type")}",
                "flag = ${indentLines("$flag")}",
                "mode = ${indentLines("$mode")}",
                "gamma = ${indentLines("$gamma")}",
                "red = ${indentLines("$red")}",
                "green = ${indentLines("$green")}",
                "blue = ${indentLines("$blue")}",
                "shadow_r = ${indentLines("$shadowR")}",
                "shadow_g = ${indentLines("$shadowG")}",
                "shadow_b = ${indentLines("$shadowB")}",
                "energy = ${indentLines("$energy")}",
                "dist = ${indentLines("$dist")}",
                "spotsize = ${indentLines("$spotsize")}",
                "spotblend = ${indentLines("$spotblend")}",
                "att1 = ${indentLines("$att1")}",
                "att2 = ${indentLines("$att2")}",
                "falloff_type = ${indentLines("$falloffType")}",
                "clipsta = ${indentLines("$clipStart")}",
                "clipend = ${indentLines("$clipEnd")}",
                "bias = ${indentLines("$bias")}",
                "soft = ${indentLines("$soft")}",
                "bufsize = ${indentLines("$bufSize")}",
                "samp = ${indentLines("$samp")}",
                "buffers = ${indentLines("$buffers")}",
                "area_shape = ${indentLines("$areaShape")}",
                "area_size = ${indentLines("$areaSize")}",
                "area_sizey = ${indentLines("$areaSizeY")}",
                "area_sizez = ${indentLines("$areaSizeZ")}",
                "}",
            )
        )
}
