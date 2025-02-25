package ch.digorydoo.titanium.import_asset.collada.reader

import ch.digorydoo.titanium.import_asset.XMLTreeReader
import ch.digorydoo.titanium.import_asset.collada.data.MeshVertices
import org.w3c.dom.Element

class MeshVerticesReader(node: Element): XMLTreeReader(node) {
    fun read(): MeshVertices {
        val vertices = MeshVertices()

        checkAttributes(arrayOf("id"))
        vertices.id = getMandatoryAttr("id")

        forEachChild { child ->
            when (child.nodeName) {
                "input" -> vertices.input.add(MeshInputReader(child).read())
                else -> throw Exception("Unexpected tag: ${child.nodeName}")
            }
        }

        return vertices
    }
}
