package ch.digorydoo.titanium.import_asset.collada.reader

import ch.digorydoo.titanium.import_asset.XMLTreeReader
import ch.digorydoo.titanium.import_asset.collada.data.Camera
import org.w3c.dom.Element

class CameraReader(node: Element): XMLTreeReader(node) {
    fun read(): Camera {
        val camera = Camera()

        checkAttributes(arrayOf("id", "name"))
        camera.id = getMandatoryAttr("id")
        camera.name = getMandatoryAttr("name")

        forEachChild { child ->
            when (child.nodeName) {
                "optics" -> {
                    require(camera.optics == null) { "<optics> not expected to appear more than once!" }
                    camera.optics = CameraOpticsReader(child).read()
                }
                "extra" -> {
                    require(camera.extra == null) { "<extra> not expected to appear more than once!" }
                    camera.extra = CameraExtraReader(child).read()
                }
                else -> throw Exception("Unexpected tag: ${child.nodeName}")
            }
        }

        return camera
    }
}
