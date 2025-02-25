package ch.digorydoo.titanium.import_asset.collada.reader

import ch.digorydoo.titanium.import_asset.XMLTreeReader
import ch.digorydoo.titanium.import_asset.collada.data.Skin
import org.w3c.dom.Element

class SkinReader(node: Element): XMLTreeReader(node) {
    fun read(): Skin {
        val skin = Skin()

        checkAttributes(arrayOf("source"))
        skin.source = getMandatoryAttr("source")

        forEachChild { child ->
            when (child.nodeName) {
                "bind_shape_matrix" -> {
                    require(skin.bindShapeMatrix == null) { "<bind_shape_matrix> cannot appear more than once!" }
                    skin.bindShapeMatrix = MatrixReader(child).read()
                }
                "joints" -> {
                    require(skin.joints == null) { "<joints> cannot appear more than once!" }
                    skin.joints = SkinJointsReader(child).read()
                }
                "vertex_weights" -> {
                    require(skin.vertexWeights == null) { "<vertex_weights> cannot appear more than once!" }
                    skin.vertexWeights = VertexWeightsReader(child).read()
                }
                "source" -> skin.sources.add(SkinSourceReader(child).read())
                else -> throw Exception("Unexpected tag: ${child.nodeName}")
            }
        }

        return skin
    }
}
