package ch.digorydoo.titanium.import_asset.collada.reader

import ch.digorydoo.titanium.import_asset.XMLTreeReader
import ch.digorydoo.titanium.import_asset.collada.data.NodeExtraTechnique
import org.w3c.dom.Element

class NodeExtraTechniqueReader(node: Element): XMLTreeReader(node) {
    fun read(): NodeExtraTechnique {
        val result = NodeExtraTechnique()

        checkAttributes(arrayOf("profile"))
        result.profile = getMandatoryAttr("profile")

        forEachChild { child ->
            when (child.nodeName) {
                "layer" -> {
                    require(result.layer == null) { "<layer> cannot appear more than once!" }
                    result.layer = SidValueReader(child).read()
                }
                "roll" -> {
                    require(result.roll == null) { "<roll> cannot appear more than once!" }
                    result.roll = SidValueReader(child).read()
                }
                "tip_x" -> {
                    require(result.tipX == null) { "<tip_x> cannot appear more than once!" }
                    result.tipX = SidValueReader(child).read()
                }
                "tip_y" -> {
                    require(result.tipY == null) { "<tip_y> cannot appear more than once!" }
                    result.tipY = SidValueReader(child).read()
                }
                "tip_z" -> {
                    require(result.tipZ == null) { "<tip_z> cannot appear more than once!" }
                    result.tipZ = SidValueReader(child).read()
                }
                else -> throw Exception("Unexpected tag: ${child.nodeName}")
            }
        }

        return result
    }
}
