package ch.digorydoo.titanium.import_asset.collada.reader

import ch.digorydoo.titanium.import_asset.XMLTreeReader
import ch.digorydoo.titanium.import_asset.collada.data.MeshFloatArray
import org.w3c.dom.Element

class MeshFloatArrayReader(node: Element): XMLTreeReader(node) {
    fun read(): MeshFloatArray {
        requireChildless()

        checkAttributes(arrayOf("id", "count"))
        val id = getMandatoryAttr("id")
        val count = getMandatoryIntAttr("count")

        val values = getValuesList()
            .map { it.toFloatOrNull() ?: throw Exception("MeshFloatArray id=$id: Value is not a float: $it") }

        require(values.size == count) { "MeshFloatArray id=$id: count=$count, but got ${values.size} values" }

        return MeshFloatArray(id, values.toFloatArray())
    }
}
