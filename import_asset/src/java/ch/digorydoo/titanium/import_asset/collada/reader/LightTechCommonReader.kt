package ch.digorydoo.titanium.import_asset.collada.reader

import ch.digorydoo.titanium.import_asset.XMLTreeReader
import ch.digorydoo.titanium.import_asset.collada.data.LightTechCommon
import org.w3c.dom.Element

class LightTechCommonReader(node: Element): XMLTreeReader(node) {
    fun read(): LightTechCommon {
        val ltc = LightTechCommon()

        checkAttributes(arrayOf())

        forEachChild { child ->
            when (child.nodeName) {
                "directional" -> {
                    require(ltc.directional == null) { "<directional> not expected to occur more than once!" }
                    ltc.directional = LightDirectionalReader(child).read()
                }
                "point" -> {
                    require(ltc.point == null) { "<point> not expected to occur more than once!" }
                    ltc.point = LightTechPointReader(child).read()
                }
                else -> throw Exception("Unexpected tag: ${child.nodeName}")
            }
        }

        return ltc
    }
}
