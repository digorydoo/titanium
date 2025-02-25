package ch.digorydoo.titanium.import_asset.collada.reader

import ch.digorydoo.titanium.import_asset.XMLTreeReader
import ch.digorydoo.titanium.import_asset.collada.data.EffectProfileCommonTech
import org.w3c.dom.Element

class EffectProfileCommonTechReader(node: Element): XMLTreeReader(node) {
    fun read(): EffectProfileCommonTech {
        val technique = EffectProfileCommonTech()

        checkAttributes(arrayOf("sid"))
        technique.sid = getMandatoryAttr("sid")

        forEachChild { child ->
            when (child.nodeName) {
                "lambert" -> {
                    require(technique.lambert == null) { "<lambert> cannot appear more than once!" }
                    technique.lambert = EffectProfileLambertReader(child).read()
                }
                else -> throw Exception("Unexpected tag: ${child.nodeName}")
            }
        }

        return technique
    }
}
