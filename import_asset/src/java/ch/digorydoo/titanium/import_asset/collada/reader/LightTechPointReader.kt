package ch.digorydoo.titanium.import_asset.collada.reader

import ch.digorydoo.titanium.import_asset.XMLTreeReader
import ch.digorydoo.titanium.import_asset.collada.data.LightTechPoint
import org.w3c.dom.Element

class LightTechPointReader(node: Element): XMLTreeReader(node) {
    fun read(): LightTechPoint {
        val ltp = LightTechPoint()

        checkAttributes(arrayOf())

        forEachChild { child ->
            when (child.nodeName) {
                "color" -> {
                    require(ltp.color == null) { "<color> cannot occur more than once!" }
                    ltp.color = ColorReader(child).read()
                }
                "constant_attenuation" -> ltp.constantAttenuation = getChildFloatValue(child)
                "linear_attenuation" -> ltp.linearAttenuation = getChildFloatValue(child)
                "quadratic_attenuation" -> ltp.quadraticAttenuation = getChildFloatValue(child)
                else -> throw Exception("Unexpected tag: ${child.nodeName}")
            }
        }

        return ltp
    }
}
