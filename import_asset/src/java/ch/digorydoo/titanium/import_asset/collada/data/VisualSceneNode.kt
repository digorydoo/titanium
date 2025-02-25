package ch.digorydoo.titanium.import_asset.collada.data

import ch.digorydoo.kutils.string.indentLines

class VisualSceneNode {
    var id = ""
    var sid = ""
    var name = ""
    var type = "" // NODE, JOINT
    var instanceCamera: Instance? = null
    var instanceController: InstanceController? = null
    var instanceGeometry: InstanceGeometry? = null
    var instanceLight: Instance? = null
    var matrix: Matrix? = null
    var extra: NodeExtra? = null
    val children = mutableListOf<VisualSceneNode>()

    override fun toString() =
        indentLines(
            arrayOf(
                "VisualSceneNode {",
                "id = \"$id\"",
                "sid = \"$sid\"",
                "name = \"$name\"",
                "type = \"$type\"",
                "instanceCamera = ${indentLines("$instanceCamera")}",
                "instanceController = ${indentLines("$instanceController")}",
                "instanceGeometry = ${indentLines("$instanceGeometry")}",
                "instanceLight = ${indentLines("$instanceLight")}",
                "matrix = ${indentLines("$matrix")}",
                "extra = ${indentLines("$extra")}",
                "children = [${indentLines(children.joinToString(", "))}]",
                "}",
            )
        )
}
