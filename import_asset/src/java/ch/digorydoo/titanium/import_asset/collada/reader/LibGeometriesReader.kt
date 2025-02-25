package ch.digorydoo.titanium.import_asset.collada.reader

import ch.digorydoo.titanium.import_asset.XMLTreeReader
import ch.digorydoo.titanium.import_asset.collada.data.Geometry
import org.w3c.dom.Element

class LibGeometriesReader(node: Element): XMLTreeReader(node) {
    fun read(): List<Geometry> {
        val geometries = mutableListOf<Geometry>()
        checkAttributes(arrayOf())

        forEachChild { child ->
            when (child.nodeName) {
                "geometry" -> geometries.add(GeometryReader(child).read())
                else -> throw Exception("Unexpected tag: ${child.nodeName}")
            }
        }

        return geometries
    }
}
