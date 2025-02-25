package ch.digorydoo.titanium.import_asset.collada.reader

import ch.digorydoo.titanium.import_asset.XMLTreeReader
import ch.digorydoo.titanium.import_asset.collada.data.CameraOpticsTechCommon
import org.w3c.dom.Element

class CameraOpticsTechCommonReader(node: Element): XMLTreeReader(node) {
    fun read(): CameraOpticsTechCommon {
        val cotc = CameraOpticsTechCommon()

        checkAttributes(arrayOf())

        forEachChild { child ->
            when (child.nodeName) {
                "perspective" -> {
                    require(cotc.perspective == null) { "<perspective> is not expected to appear more than once" }
                    cotc.perspective = CameraPerspectiveReader(child).read()
                }
                else -> throw Exception("Unexpected tag: ${child.nodeName}")
            }
        }

        return cotc
    }
}
