package io.github.digorydoo.titanium.import_asset.collada.reader

import io.github.digorydoo.titanium.import_asset.XMLTreeReader
import io.github.digorydoo.titanium.import_asset.collada.data.Effect
import org.w3c.dom.Element

class EffectReader(node: Element): XMLTreeReader(node) {
    fun read(): Effect {
        val effect = Effect()

        checkAttributes(arrayOf("id"))
        effect.id = getMandatoryAttr("id")

        forEachChild { child ->
            when (child.nodeName) {
                "profile_COMMON" -> {
                    require(effect.profileCommon == null) { "<profile_COMMON> cannot appear more than once!" }
                    effect.profileCommon = EffectProfileCommonReader(child).read()
                }
                else -> throw Exception("Unexpected tag: ${child.nodeName}")
            }
        }

        return effect
    }
}
