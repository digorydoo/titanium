package ch.digorydoo.titanium.import_asset.collada.reader

import ch.digorydoo.titanium.import_asset.XMLTreeReader
import ch.digorydoo.titanium.import_asset.collada.data.MeshSource
import org.w3c.dom.Element

class MeshSourceReader(node: Element): XMLTreeReader(node) {
    fun read(): MeshSource {
        val source = MeshSource()

        checkAttributes(arrayOf("id"))
        source.id = getMandatoryAttr("id")

        forEachChild { child ->
            when (child.nodeName) {
                "float_array" -> {
                    require(source.meshFloatArray == null) { "<float_array> not expected to occur more than once!" }
                    source.meshFloatArray = MeshFloatArrayReader(child).read()
                }
                "technique_common" -> {
                    require(source.techCommon == null) { "<technique_common> not expected to occur more than once!" }
                    source.techCommon = MeshTechCommonReader(child).read()
                }
                else -> throw Exception("Unexpected tag: ${child.nodeName}")
            }
        }

        return source
    }
}
