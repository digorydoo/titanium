package ch.digorydoo.titanium.import_asset.collada.reader

import ch.digorydoo.titanium.import_asset.XMLTreeReader
import ch.digorydoo.titanium.import_asset.collada.data.Camera
import org.w3c.dom.Element

class LibCamerasReader(node: Element): XMLTreeReader(node) {
    fun read(): List<Camera> {
        val cameras = mutableListOf<Camera>()
        checkAttributes(arrayOf())

        forEachChild { child ->
            when (child.nodeName) {
                "camera" -> cameras.add(CameraReader(child).read())
                else -> throw Exception("Unexpected tag: ${child.nodeName}")
            }
        }

        return cameras
    }
}
