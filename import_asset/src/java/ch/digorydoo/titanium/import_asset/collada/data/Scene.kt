package ch.digorydoo.titanium.import_asset.collada.data

import ch.digorydoo.kutils.string.indentLines

class Scene {
    var instanceVisualScene: Instance? = null

    override fun toString() =
        indentLines(
            arrayOf(
                "Scene {",
                "instanceVisualScene = ${indentLines("$instanceVisualScene")}",
                "}",
            )
        )
}
