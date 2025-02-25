package ch.digorydoo.titanium.import_asset.collada.reader

import ch.digorydoo.titanium.import_asset.XMLTreeReader
import ch.digorydoo.titanium.import_asset.collada.data.CameraExtra
import org.w3c.dom.Element

class CameraExtraReader(node: Element): XMLTreeReader(node) {
    fun read(): CameraExtra {
        val ce = CameraExtra()

        checkAttributes(arrayOf())

        forEachChild { child ->
            when (child.nodeName) {
                "technique" -> {
                    require(ce.technique == null) { "<technique> is not expected to appear more than once!" }
                    ce.technique = CameraExtraTechniqueReader(child).read()
                }
                else -> throw Exception("Unexpected tag: ${child.nodeName}")
            }
        }

        return ce
    }
}
