package ch.digorydoo.titanium.import_asset.collada.reader

import ch.digorydoo.titanium.import_asset.XMLTreeReader
import ch.digorydoo.titanium.import_asset.collada.data.Mesh
import org.w3c.dom.Element

class MeshReader(node: Element): XMLTreeReader(node) {
    fun read(): Mesh {
        val mesh = Mesh()

        checkAttributes(arrayOf())

        forEachChild { child ->
            when (child.nodeName) {
                "source" -> mesh.sources.add(MeshSourceReader(child).read())
                "vertices" -> {
                    require(mesh.vertices == null) { "<vertices> is not expected to occur more than once!" }
                    mesh.vertices = MeshVerticesReader(child).read()
                }
                "triangles" -> {
                    require(mesh.triangles == null) { "<triangles> is not expected to occur more than once!" }
                    mesh.triangles = MeshTrianglesReader(child).read()
                }
                else -> throw Exception("Unexpected tag: ${child.nodeName}")
            }
        }

        return mesh
    }
}
