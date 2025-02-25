package ch.digorydoo.titanium.import_asset.collada.data

import ch.digorydoo.kutils.string.indentLines

class Matrix {
    var sid = ""
    var floatArray: FloatArray? = null

    override fun toString() =
        indentLines(
            arrayOf(
                "Matrix {",
                "sid = \"$sid\"",
                "floatArray.size = ${floatArray?.size}",
                "}",
            )
        )
}
