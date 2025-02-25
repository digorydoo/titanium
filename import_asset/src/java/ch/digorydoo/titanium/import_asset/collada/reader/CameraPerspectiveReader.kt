package ch.digorydoo.titanium.import_asset.collada.reader

import ch.digorydoo.titanium.import_asset.XMLTreeReader
import ch.digorydoo.titanium.import_asset.collada.data.CameraPerspective
import org.w3c.dom.Element

class CameraPerspectiveReader(node: Element): XMLTreeReader(node) {
    fun read(): CameraPerspective {
        val cp = CameraPerspective()

        checkAttributes(arrayOf())

        forEachChild { child ->
            when (child.nodeName) {
                "xfov" -> cp.xfov = SidValueReader(child).read()
                "aspect_ratio" -> cp.aspectRatio = getChildValue(child)
                "znear" -> cp.znear = SidValueReader(child).read()
                "zfar" -> cp.zfar = SidValueReader(child).read()
                else -> throw Exception("Unexpected tag: ${child.nodeName}")
            }
        }

        return cp
    }
}
