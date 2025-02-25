package ch.digorydoo.titanium.import_asset.collada.reader

import ch.digorydoo.titanium.import_asset.XMLTreeReader
import ch.digorydoo.titanium.import_asset.collada.data.EffectProfileCommon
import org.w3c.dom.Element

class EffectProfileCommonReader(node: Element): XMLTreeReader(node) {
    fun read(): EffectProfileCommon {
        val result = EffectProfileCommon()
        checkAttributes(arrayOf())

        forEachChild { child ->
            when (child.nodeName) {
                "newparam" -> result.newParams.add(EffectNewParamReader(child).read())
                "technique" -> {
                    require(result.technique == null) { "<technique> cannot appear more than once!" }
                    result.technique = EffectProfileCommonTechReader(child).read()
                }
                else -> throw Exception("Unexpected tag: ${child.nodeName}")
            }
        }

        return result
    }
}
