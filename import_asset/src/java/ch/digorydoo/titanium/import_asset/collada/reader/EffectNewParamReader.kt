package ch.digorydoo.titanium.import_asset.collada.reader

import ch.digorydoo.titanium.import_asset.XMLTreeReader
import ch.digorydoo.titanium.import_asset.collada.data.EffectNewParam
import org.w3c.dom.Element

class EffectNewParamReader(node: Element): XMLTreeReader(node) {
    fun read(): EffectNewParam {
        val result = EffectNewParam()

        checkAttributes(arrayOf("sid"))
        result.sid = getMandatoryAttr("sid")

        forEachChild { child ->
            when (child.nodeName) {
                "surface" -> {
                    val surface = EffectSurfaceReader(child).read()

                    when (surface.type) {
                        "2D" -> {
                            require(result.surface2D == null) { "Multiple surfaces of type ${surface.type}" }
                            result.surface2D = surface
                        }
                        else -> throw Exception("Unexpected surface type: ${surface.type}")
                    }
                }
                "sampler2D" -> {
                    require(result.sampler2D == null) { "<sampler2D> cannot appear more than once!" }
                    result.sampler2D = EffectSampler2DReader(child).read()
                }
                else -> throw Exception("Unexpected tag: ${child.nodeName}")
            }
        }

        return result
    }
}
