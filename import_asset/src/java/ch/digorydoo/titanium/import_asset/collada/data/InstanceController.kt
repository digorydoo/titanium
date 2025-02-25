package ch.digorydoo.titanium.import_asset.collada.data

import ch.digorydoo.kutils.string.indentLines

class InstanceController {
    var url = "" // #id of Controller
    var skeleton: SidValue? = null // #id of VisualSceneNode
    var bindMaterial: BindMaterial? = null

    override fun toString() =
        indentLines(
            arrayOf(
                "InstanceController {",
                "url = \"$url\"",
                "skeleton = ${indentLines("$skeleton")}",
                "bindMaterial = ${indentLines("$bindMaterial")}",
                "}",
            )
        )
}
