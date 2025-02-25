package ch.digorydoo.titanium.import_asset.collada.reader

import ch.digorydoo.titanium.import_asset.XMLTreeReader
import ch.digorydoo.titanium.import_asset.collada.data.BindMaterial
import org.w3c.dom.Element

class BindMaterialReader(node: Element): XMLTreeReader(node) {
    fun read(): BindMaterial {
        val bind = BindMaterial()

        checkAttributes(arrayOf())

        forEachChild { child ->
            when (child.nodeName) {
                "technique_common" -> {
                    require(bind.techCommon == null) { "<technique_common> cannot appear more than once!" }
                    bind.techCommon = BindMaterialTechCommonReader(child).read()
                }
                else -> throw Exception("Unexpected tag: ${child.nodeName}")
            }
        }

        return bind
    }
}
