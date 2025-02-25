package ch.digorydoo.titanium.import_asset.collada.reader

import ch.digorydoo.titanium.import_asset.XMLTreeReader
import ch.digorydoo.titanium.import_asset.collada.data.ColladaData
import org.w3c.dom.Element

class RootReader(node: Element): XMLTreeReader(node) {
    fun read(): ColladaData {
        if (node.nodeName != "COLLADA") {
            throw Exception("Unexpected XML root: ${node.nodeName}")
        }

        val data = ColladaData()

        forEachChild { child ->
            when (child.nodeName) {
                "asset" -> {
                    require(data.asset == null) { "<asset> not expected to occur more than once" }
                    data.asset = AssetReader(child).read()
                }
                "library_animations" -> data.animations.addAll(LibAnimationsReader(child).read())
                "library_cameras" -> data.cameras.addAll(LibCamerasReader(child).read())
                "library_controllers" -> data.controllers.addAll(LibControllersReader(child).read())
                "library_effects" -> data.effects.addAll(LibEffectsReader(child).read())
                "library_geometries" -> data.geometries.addAll(LibGeometriesReader(child).read())
                "library_images" -> data.images.addAll(LibImagesReader(child).read())
                "library_materials" -> data.materials.addAll(LibMaterialsReader(child).read())
                "library_lights" -> data.lights.addAll(LibLightsReader(child).read())
                "library_visual_scenes" -> data.visualScenes.addAll(LibVisualScenesReader(child).read())
                "scene" -> {
                    require(data.scene == null) { "<scene> not expected to appear more than once" }
                    data.scene = SceneReader(child).read()
                }
                else -> throw Exception("Unexpected tag: ${child.nodeName}")
            }
        }

        return data
    }
}
