package ch.digorydoo.titanium.import_asset.collada.reader

import ch.digorydoo.titanium.import_asset.XMLTreeReader
import ch.digorydoo.titanium.import_asset.collada.data.LambertDiffuse
import org.w3c.dom.Element

class LambertDiffuseReader(node: Element): XMLTreeReader(node) {
    fun read(): LambertDiffuse {
        val result = LambertDiffuse()
        checkAttributes(arrayOf())

        forEachChild { child ->
            when (child.nodeName) {
                "color" -> {
                    require(result.color == null) { "<color> cannot appear more than once!" }
                    result.color = ColorReader(child).read()
                    require(result.color?.sid == "diffuse") { "sid is ${result.color?.sid}, expected \"diffuse\"" }
                }
                "texture" -> {
                    require(result.texture == null) { "<texture> cannot appear more than once!" }
                    result.texture = TextureReader(child).read()
                }
                else -> throw Exception("Unexpected tag: ${child.nodeName}")
            }
        }

        return result
    }
}
