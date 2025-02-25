package ch.digorydoo.titanium.import_asset.collada.data

import ch.digorydoo.kutils.string.indentLines

class SidValue {
    var sid = ""
    var type = ""
    var value = ""

    val floatValue: Float
        get() = value.toFloatOrNull() ?: throw Exception("SidValue(sid=$sid): Value is not a float: $value")

    override fun toString() =
        indentLines(
            arrayOf(
                "SidValue {",
                "sid = \"$sid\"",
                "type = \"$type\"",
                "value = \"$value\"",
                "}"
            )
        )
}
