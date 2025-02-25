package ch.digorydoo.titanium.import_asset.collada.reader

import ch.digorydoo.titanium.import_asset.XMLTreeReader
import ch.digorydoo.titanium.import_asset.collada.data.MeshTriangles
import org.w3c.dom.Element

class MeshTrianglesReader(node: Element): XMLTreeReader(node) {
    fun read(): MeshTriangles {
        val triangles = MeshTriangles()

        checkAttributes(arrayOf("count", "material"))
        triangles.count = getMandatoryIntAttr("count")
        triangles.material = getOptionalAttr("material") ?: ""

        forEachChild { child ->
            when (child.nodeName) {
                "input" -> triangles.input.add(MeshInputReader(child).read())
                "p" -> {
                    require(triangles.p == null) { "<p> is not expected to appear more than once!" }
                    triangles.p = MeshIntArrayReader(child).read()
                }
                else -> throw Exception("Unexpected tag: ${child.nodeName}")
            }
        }

        return triangles
    }
}
