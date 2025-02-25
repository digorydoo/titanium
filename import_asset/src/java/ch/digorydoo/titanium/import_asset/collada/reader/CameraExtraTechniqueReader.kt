package ch.digorydoo.titanium.import_asset.collada.reader

import ch.digorydoo.titanium.import_asset.XMLTreeReader
import ch.digorydoo.titanium.import_asset.collada.data.CameraExtraTechnique
import org.w3c.dom.Element

class CameraExtraTechniqueReader(node: Element): XMLTreeReader(node) {
    fun read(): CameraExtraTechnique {
        val cet = CameraExtraTechnique()

        checkAttributes(arrayOf("profile"))
        cet.profile = getMandatoryAttr("profile")

        forEachChild { child ->
            when (child.nodeName) {
                "shiftx" -> {
                    require(cet.shiftx == null) { "<shiftx> not expected to appear more than once" }
                    cet.shiftx = SidValueReader(child).read()
                }
                "shifty" -> {
                    require(cet.shifty == null) { "<shifty> not expected to appear more than once" }
                    cet.shifty = SidValueReader(child).read()
                }
                "dof_distance" -> {
                    require(cet.dofDistance == null) { "<dofDistance> not expected to appear more than once" }
                    cet.dofDistance = SidValueReader(child).read()
                }
                else -> throw Exception("Unexpected tag: ${child.nodeName}")
            }
        }

        return cet
    }
}
