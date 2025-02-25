package ch.digorydoo.titanium.import_asset.collada.data

import ch.digorydoo.kutils.string.indentLines

class MeshFloatArray(val id: String, val floatArray: FloatArray) {
    override fun toString() =
        indentLines(
            arrayOf(
                "MeshFloatArray {",
                "id = \"$id\"",
                "size = ${floatArray.size}",
                "}",
            )
        )
}
