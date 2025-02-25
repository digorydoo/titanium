package ch.digorydoo.titanium.import_asset.collada.reader

import ch.digorydoo.titanium.import_asset.XMLTreeReader
import ch.digorydoo.titanium.import_asset.collada.data.Geometry
import org.w3c.dom.Element

class GeometryReader(node: Element): XMLTreeReader(node) {
    fun read(): Geometry {
        val geometry = Geometry()

        checkAttributes(arrayOf("id", "name"))
        geometry.id = getMandatoryAttr("id")
        geometry.name = getMandatoryAttr("name")

        forEachChild { child ->
            when (child.nodeName) {
                "mesh" -> {
                    require(geometry.mesh == null) { "<mesh> is not expected to occur more than once!" }
                    geometry.mesh = MeshReader(child).read()
                }
                else -> throw Exception("Unexpected tag: ${child.nodeName}")
            }
        }

        return geometry
    }
}
