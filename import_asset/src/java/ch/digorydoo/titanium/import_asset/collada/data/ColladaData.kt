package ch.digorydoo.titanium.import_asset.collada.data

import ch.digorydoo.kutils.string.indentLines

class ColladaData {
    var asset: Asset? = null
    val animations = mutableListOf<Animation>()
    val cameras = mutableListOf<Camera>()
    val controllers = mutableListOf<Controller>()
    val effects = mutableListOf<Effect>()
    val geometries = mutableListOf<Geometry>()
    val images = mutableListOf<Image>()
    val materials = mutableListOf<Material>()
    val lights = mutableListOf<Light>()
    val visualScenes = mutableListOf<VisualScene>()
    var scene: Scene? = null

    override fun toString() =
        indentLines(
            arrayOf(
                "ColladaData {",
                "asset = ${indentLines("$asset")}",
                "animations = [${indentLines(animations.joinToString { "$it" })}]",
                "cameras = [${indentLines(cameras.joinToString { "$it" })}]",
                "controllers = [${indentLines(controllers.joinToString { "$it" })}]",
                "effects = [${indentLines(effects.joinToString { "$it" })}]",
                "geometries = [${indentLines(geometries.joinToString { "$it" })}]",
                "images = [${indentLines(images.joinToString { "$it" })}]",
                "materials = [${indentLines(materials.joinToString { "$it" })}]",
                "lights = [${indentLines(lights.joinToString { "$it" })}]",
                "visualScenes = [${indentLines(visualScenes.joinToString { "$it" })}]",
                "scene = ${indentLines("$scene")}",
                "}"
            )
        )
}
