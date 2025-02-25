package ch.digorydoo.titanium.import_asset.collada.reader

import ch.digorydoo.titanium.import_asset.XMLTreeReader
import ch.digorydoo.titanium.import_asset.collada.data.VisualScene
import org.w3c.dom.Element

class VisualSceneReader(node: Element): XMLTreeReader(node) {
    fun read(): VisualScene {
        val visualScene = VisualScene()

        checkAttributes(arrayOf("id", "name"))
        visualScene.id = getMandatoryAttr("id")
        visualScene.name = getMandatoryAttr("name")

        forEachChild { child ->
            when (child.nodeName) {
                "node" -> visualScene.nodes.add(VisualSceneNodeReader(child).read())
                else -> throw Exception("Unexpected tag: ${child.nodeName}")
            }
        }

        return visualScene
    }
}
