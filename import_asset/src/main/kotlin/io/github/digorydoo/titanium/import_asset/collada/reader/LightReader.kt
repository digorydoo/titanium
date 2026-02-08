package io.github.digorydoo.titanium.import_asset.collada.reader

import io.github.digorydoo.titanium.import_asset.XMLTreeReader
import io.github.digorydoo.titanium.import_asset.collada.data.Light
import org.w3c.dom.Element

class LightReader(node: Element): XMLTreeReader(node) {
    fun read(): Light {
        val light = Light()

        checkAttributes(arrayOf("id", "name"))
        light.id = getMandatoryAttr("id")
        light.name = getMandatoryAttr("name")

        forEachChild { child ->
            when (child.nodeName) {
                "technique_common" -> {
                    require(light.techCommon == null) { "<technique_common> cannot appear more than once!" }
                    light.techCommon = LightTechCommonReader(child).read()
                }
                "extra" -> {
                    require(light.extra == null) { "<extra> cannot appear more than once!" }
                    light.extra = LightExtraReader(child).read()
                }
                else -> throw Exception("Unexpected tag: ${child.nodeName}")
            }
        }

        return light
    }
}
