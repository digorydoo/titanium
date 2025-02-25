package ch.digorydoo.titanium.import_asset.collada.reader

import ch.digorydoo.titanium.import_asset.XMLTreeReader
import ch.digorydoo.titanium.import_asset.collada.data.EffectSurface
import org.w3c.dom.Element

class EffectSurfaceReader(node: Element): XMLTreeReader(node) {
    fun read(): EffectSurface {
        val result = EffectSurface()

        checkAttributes(arrayOf("type"))
        result.type = getMandatoryAttr("type")

        forEachChild { child ->
            when (child.nodeName) {
                "init_from" -> {
                    require(result.initFrom == null) { "<init_from> cannot appear more than once!" }
                    result.initFrom = SidValueReader(child).read()
                }
                else -> throw Exception("Unexpected tag: ${child.nodeName}")
            }
        }

        return result
    }
}
