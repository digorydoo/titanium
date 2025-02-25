package ch.digorydoo.titanium.import_asset.collada.reader

import ch.digorydoo.titanium.import_asset.XMLTreeReader
import ch.digorydoo.titanium.import_asset.collada.data.CameraOptics
import org.w3c.dom.Element

class CameraOpticsReader(node: Element): XMLTreeReader(node) {
    fun read(): CameraOptics {
        val optics = CameraOptics()

        checkAttributes(arrayOf())

        forEachChild { child ->
            when (child.nodeName) {
                "technique_common" -> {
                    require(optics.techCommon == null) { "<technique_common> not expected to appear more than once!" }
                    optics.techCommon = CameraOpticsTechCommonReader(child).read()
                }
                else -> throw Exception("Unexpected tag: ${child.nodeName}")
            }
        }

        return optics
    }
}
