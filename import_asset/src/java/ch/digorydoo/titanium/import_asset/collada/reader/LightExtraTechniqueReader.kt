package ch.digorydoo.titanium.import_asset.collada.reader

import ch.digorydoo.titanium.import_asset.XMLTreeReader
import ch.digorydoo.titanium.import_asset.collada.data.LightExtraTechnique
import org.w3c.dom.Element

class LightExtraTechniqueReader(node: Element): XMLTreeReader(node) {
    fun read(): LightExtraTechnique {
        val tech = LightExtraTechnique()

        checkAttributes(arrayOf("profile"))
        tech.profile = getMandatoryAttr("profile")

        forEachChild { child ->
            when (child.nodeName) {
                "type" -> tech.type = SidValueReader(child).read()
                "flag" -> tech.flag = SidValueReader(child).read()
                "mode" -> tech.mode = SidValueReader(child).read()
                "gamma" -> tech.gamma = SidValueReader(child).read()
                "red" -> tech.red = SidValueReader(child).read()
                "green" -> tech.green = SidValueReader(child).read()
                "blue" -> tech.blue = SidValueReader(child).read()
                "shadow_r" -> tech.shadowR = SidValueReader(child).read()
                "shadow_g" -> tech.shadowG = SidValueReader(child).read()
                "shadow_b" -> tech.shadowB = SidValueReader(child).read()
                "energy" -> tech.energy = SidValueReader(child).read()
                "dist" -> tech.dist = SidValueReader(child).read()
                "spotsize" -> tech.spotsize = SidValueReader(child).read()
                "spotblend" -> tech.spotblend = SidValueReader(child).read()
                "att1" -> tech.att1 = SidValueReader(child).read()
                "att2" -> tech.att2 = SidValueReader(child).read()
                "falloff_type" -> tech.falloffType = SidValueReader(child).read()
                "clipsta" -> tech.clipStart = SidValueReader(child).read()
                "clipend" -> tech.clipEnd = SidValueReader(child).read()
                "bias" -> tech.bias = SidValueReader(child).read()
                "soft" -> tech.soft = SidValueReader(child).read()
                "bufsize" -> tech.bufSize = SidValueReader(child).read()
                "samp" -> tech.samp = SidValueReader(child).read()
                "buffers" -> tech.buffers = SidValueReader(child).read()
                "area_shape" -> tech.areaShape = SidValueReader(child).read()
                "area_size" -> tech.areaSize = SidValueReader(child).read()
                "area_sizey" -> tech.areaSizeY = SidValueReader(child).read()
                "area_sizez" -> tech.areaSizeZ = SidValueReader(child).read()
                else -> throw Exception("Unexpected tag: ${child.nodeName}")
            }
        }

        return tech
    }
}
