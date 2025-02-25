package ch.digorydoo.titanium.import_asset.collada.reader

import ch.digorydoo.titanium.import_asset.XMLTreeReader
import ch.digorydoo.titanium.import_asset.collada.data.SkinSource
import org.w3c.dom.Element

class SkinSourceReader(node: Element): XMLTreeReader(node) {
    fun read(): SkinSource {
        val result = SkinSource()

        checkAttributes(arrayOf("id"))
        result.id = getMandatoryAttr("id")

        forEachChild { child ->
            when (child.nodeName) {
                "Name_array" -> {
                    require(result.nameArray == null) { "<Name_array> cannot appear more than once!" }
                    result.nameArray = NameArrayReader(child).read()
                }
                "float_array" -> {
                    require(result.floatArray == null) { "<float_array> cannot appear more than once!" }
                    result.floatArray = MeshFloatArrayReader(child).read()
                }
                "technique_common" -> {
                    require(result.techCommon == null) { "<technique_common> cannot appear more than once!" }
                    result.techCommon = SkinTechCommonReader(child).read()
                }
                else -> throw Exception("Unexpected tag: ${child.nodeName}")
            }
        }

        return result
    }
}
