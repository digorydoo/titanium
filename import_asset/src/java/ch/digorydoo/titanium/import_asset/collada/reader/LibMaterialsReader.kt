package ch.digorydoo.titanium.import_asset.collada.reader

import ch.digorydoo.titanium.import_asset.XMLTreeReader
import ch.digorydoo.titanium.import_asset.collada.data.Material
import org.w3c.dom.Element

class LibMaterialsReader(node: Element): XMLTreeReader(node) {
    fun read(): List<Material> {
        val materials = mutableListOf<Material>()
        checkAttributes(arrayOf())

        forEachChild { child ->
            when (child.nodeName) {
                "material" -> materials.add(MaterialReader(child).read())
                else -> throw Exception("Unexpected tag: ${child.nodeName}")
            }
        }

        return materials
    }
}
