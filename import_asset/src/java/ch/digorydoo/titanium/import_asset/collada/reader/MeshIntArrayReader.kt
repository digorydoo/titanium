package ch.digorydoo.titanium.import_asset.collada.reader

import ch.digorydoo.titanium.import_asset.XMLTreeReader
import ch.digorydoo.titanium.import_asset.collada.data.MeshIntArray
import org.w3c.dom.Element

class MeshIntArrayReader(node: Element): XMLTreeReader(node) {
    fun read(): MeshIntArray {
        checkAttributes(arrayOf())
        requireChildless()

        val values = getValuesList()
            .map { it.toIntOrNull() ?: throw Exception("MeshIntArray: Value is not an int: $it") }

        return MeshIntArray(values.toIntArray())
    }
}
