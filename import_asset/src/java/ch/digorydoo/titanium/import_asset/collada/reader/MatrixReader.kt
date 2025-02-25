package ch.digorydoo.titanium.import_asset.collada.reader

import ch.digorydoo.titanium.import_asset.XMLTreeReader
import ch.digorydoo.titanium.import_asset.collada.data.Matrix
import org.w3c.dom.Element
import java.util.Collections.swap

class MatrixReader(node: Element): XMLTreeReader(node) {
    fun read(): Matrix {
        val matrix = Matrix()

        checkAttributes(arrayOf("sid"))
        matrix.sid = getOptionalAttr("sid") ?: ""

        val values = getValuesList()
            .map { it.toFloatOrNull() ?: throw Exception("Matrix value is not a float: $it") }

        require(values.size == 16) { "Matrix is not a mat4: size=${values.size}" }

        // Transpose matrix to get the ordering compatible with Matrix4f
        swap(values, 1, 4)
        swap(values, 2, 8)
        swap(values, 3, 12)
        swap(values, 6, 9)
        swap(values, 7, 13)
        swap(values, 11, 14)

        matrix.floatArray = values.toFloatArray()

        requireChildless()
        return matrix
    }
}
