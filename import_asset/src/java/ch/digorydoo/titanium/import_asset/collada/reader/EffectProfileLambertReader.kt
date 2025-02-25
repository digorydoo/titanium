package ch.digorydoo.titanium.import_asset.collada.reader

import ch.digorydoo.titanium.import_asset.XMLTreeReader
import ch.digorydoo.titanium.import_asset.collada.data.EffectProfileLambert
import org.w3c.dom.Element

class EffectProfileLambertReader(node: Element): XMLTreeReader(node) {
    fun read(): EffectProfileLambert {
        val lambert = EffectProfileLambert()
        checkAttributes(arrayOf())

        forEachChild { child ->
            when (child.nodeName) {
                "emission" -> {
                    require(lambert.emission == null) { "<emission> cannot appear more than once!" }
                    lambert.emission = LambertColorReader(child, "emission").read()
                }
                "diffuse" -> {
                    require(lambert.diffuse == null) { "<diffuse> cannot appear more than once!" }
                    lambert.diffuse = LambertDiffuseReader(child).read()
                }
                "index_of_refraction" -> {
                    require(lambert.indexOfRefraction == null) { "<index_of_refraction> cannot appear more than once!" }
                    lambert.indexOfRefraction = LambertValueReader(child, "ior").read()
                }
                "reflectivity" -> {
                    require(lambert.reflectivity == null) { "<reflectivity> cannot appear more than once!" }
                    lambert.reflectivity = LambertValueReader(child, "specular").read()
                }
                "transparent" -> {
                    require(lambert.transparent == null) { "<transparent> cannot appear more than once!" }
                    lambert.transparent = LambertTransparentReader(child).read()
                }
                else -> throw Exception("Unexpected tag: ${child.nodeName}")
            }
        }

        return lambert
    }
}
