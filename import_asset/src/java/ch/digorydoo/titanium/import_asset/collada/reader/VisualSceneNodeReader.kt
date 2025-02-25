package ch.digorydoo.titanium.import_asset.collada.reader

import ch.digorydoo.titanium.import_asset.XMLTreeReader
import ch.digorydoo.titanium.import_asset.collada.data.VisualSceneNode
import org.w3c.dom.Element

class VisualSceneNodeReader(node: Element): XMLTreeReader(node) {
    fun read(): VisualSceneNode {
        val node = VisualSceneNode()

        checkAttributes(arrayOf("id", "sid", "name", "type"))
        node.id = getMandatoryAttr("id")
        node.sid = getOptionalAttr("sid") ?: ""
        node.name = getMandatoryAttr("name")
        node.type = getMandatoryAttr("type")

        forEachChild { child ->
            when (child.nodeName) {
                "instance_camera" -> {
                    require(node.instanceCamera == null) { "<instance_camera> cannot occur more than once" }
                    node.instanceCamera = InstanceReader(child).read()
                }
                "instance_controller" -> {
                    require(node.instanceController == null) { "<instance_controller> cannot occur more than once" }
                    node.instanceController = InstanceControllerReader(child).read()
                }
                "instance_geometry" -> {
                    require(node.instanceGeometry == null) { "<instance_geometry> cannot occur more than once" }
                    node.instanceGeometry = InstanceGeometryReader(child).read()
                }
                "instance_light" -> {
                    require(node.instanceLight == null) { "<instance_light> cannot occur more than once" }
                    node.instanceLight = InstanceReader(child).read()
                }
                "matrix" -> {
                    require(node.matrix == null) { "<matrix> cannot occur more than once" }
                    node.matrix = MatrixReader(child).read()
                }
                "extra" -> {
                    require(node.extra == null) { "<extra> cannot appear more than once" }
                    node.extra = NodeExtraReader(child).read()
                }
                "node" -> node.children.add(VisualSceneNodeReader(child).read())
                else -> throw Exception("Unexpected tag: ${child.nodeName}")
            }
        }

        return node
    }
}
