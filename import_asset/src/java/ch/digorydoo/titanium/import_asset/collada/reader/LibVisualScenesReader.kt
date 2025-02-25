package ch.digorydoo.titanium.import_asset.collada.reader

import ch.digorydoo.titanium.import_asset.XMLTreeReader
import ch.digorydoo.titanium.import_asset.collada.data.VisualScene
import org.w3c.dom.Element

class LibVisualScenesReader(node: Element): XMLTreeReader(node) {
    fun read(): List<VisualScene> {
        val visualScenes = mutableListOf<VisualScene>()
        checkAttributes(arrayOf())

        forEachChild { child ->
            when (child.nodeName) {
                "visual_scene" -> visualScenes.add(VisualSceneReader(child).read())
                else -> throw Exception("Unexpected tag: ${child.nodeName}")
            }
        }

        return visualScenes
    }
}
