package ch.digorydoo.titanium.import_asset.collada.reader

import ch.digorydoo.titanium.import_asset.XMLTreeReader
import ch.digorydoo.titanium.import_asset.collada.data.Scene
import org.w3c.dom.Element

class SceneReader(node: Element): XMLTreeReader(node) {
    fun read(): Scene {
        val scene = Scene()
        checkAttributes(arrayOf())

        forEachChild { child ->
            when (child.nodeName) {
                "instance_visual_scene" -> {
                    require(scene.instanceVisualScene == null) { "<instance_visual_scene> cannot occur more than once" }
                    scene.instanceVisualScene = InstanceReader(child).read()
                }
                else -> throw Exception("Unexpected tag: ${child.nodeName}")
            }
        }

        return scene
    }
}
