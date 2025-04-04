package ch.digorydoo.titanium.engine.file

import ch.digorydoo.kutils.matrix.Matrix4f
import ch.digorydoo.kutils.point.Point2f
import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.kutils.utils.newFloatBuffer
import ch.digorydoo.kutils.utils.requireNotNull
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.file.FileMarker.*
import ch.digorydoo.titanium.engine.mesh.Geometry
import ch.digorydoo.titanium.engine.mesh.Mesh
import ch.digorydoo.titanium.engine.mesh.MeshDivision
import ch.digorydoo.titanium.engine.mesh.MeshMaterial
import ch.digorydoo.titanium.engine.mesh.MeshNode
import ch.digorydoo.titanium.engine.texture.Texture
import java.io.File
import java.nio.FloatBuffer

class MeshFileReader private constructor(private val input: MyDataInputStream) {
    private class IncompleteGeometry(private val owner: IncompleteMesh) {
        var positions: IntArray? = null
        var normals: IntArray? = null
        var texCoords: IntArray? = null

        fun toGeometry(): Geometry? {
            // If positions and normals are still null at this point, this means that the geometry is empty.
            // This usually happens when a dummy mesh serves as a parent for nested meshes.
            val positions = positions ?: return null
            val normals = normals ?: return null
            return Geometry(
                positions = owner.lookUpPoint3f(positions),
                normals = owner.lookUpPoint3f(normals),
                texCoords = texCoords?.let { owner.lookUpPoint2f(it) },
            )
        }
    }

    private class IncompleteNode {
        var id = ""
        var tex: Texture? = null
        var transform: Matrix4f? = null
        var geometry: IncompleteGeometry? = null
        val children = mutableListOf<IncompleteNode>()

        fun toNode(): MeshNode = MeshNode(
            id = id,
            tex = tex,
            transform = transform,
            geometry = geometry?.toGeometry(),
            children = children.map { it.toNode() }
        )
    }

    private class IncompleteDivision {
        var material: MeshMaterial? = null
        val nodes = mutableListOf<IncompleteNode>()

        fun toDivision() = MeshDivision(
            material = material!!,
            nodes = nodes.map { it.toNode() }
        )
    }

    private class IncompleteMesh {
        val divisions = mutableListOf<IncompleteDivision>()
        val pt3fList = mutableListOf<Point3f>()
        val pt2fList = mutableListOf<Point2f>()

        fun toMesh() = Mesh(
            divisions = divisions.map { it.toDivision() }
        )

        fun lookUpPoint3f(indices: IntArray): FloatBuffer {
            val buf = newFloatBuffer(indices.size * 3)
            buf.position(0)

            for (idx in indices) {
                val pt = pt3fList[idx]
                buf.put(pt.x)
                buf.put(pt.y)
                buf.put(pt.z)
            }

            return buf
        }

        fun lookUpPoint2f(indices: IntArray): FloatBuffer {
            val buf = newFloatBuffer(indices.size * 2)
            buf.position(0)

            for (idx in indices) {
                val pt = pt2fList[idx]
                buf.put(pt.x)
                buf.put(pt.y)
            }

            return buf
        }
    }

    private var mesh: IncompleteMesh? = null
    private var division: IncompleteDivision? = null
    private var geometry: IncompleteGeometry? = null
    private val geometries = mutableListOf<IncompleteGeometry?>()
    private val nodeStack = mutableListOf<IncompleteNode>()

    fun read(): Mesh {
        mesh = IncompleteMesh()

        input.readExpected(BEGIN_MESH_FILE)
        var finished = false

        while (!finished) {
            val marker = input.readMarker()

            when (marker) {
                END_MESH_FILE -> finished = true
                COLLECTED_POINT3F -> readCollectedPt3f()
                COLLECTED_POINT2F -> readCollectedPt2f()
                BEGIN_DIVISION -> beginDivision()
                END_DIVISION -> endDivision()
                BEGIN_NODE -> beginNode()
                END_NODE -> endNode()
                BEGIN_GEOMETRY -> beginGeometry()
                END_GEOMETRY -> endGeometry()
                GEOMETRY_REF -> readGeometryRef()
                POSITIONS -> readPositions()
                NORMALS -> readNormals()
                TEXCOORDS -> readTexCoords()
                MATERIAL -> readMaterial()
                MATRIX -> readMatrix()
                else -> throw Exception("Marker not handled: $marker")
            }
        }

        return mesh!!.toMesh()
    }

    private fun beginDivision() {
        val mesh = mesh!!
        require(division == null)

        IncompleteDivision().let {
            division = it
            mesh.divisions.add(it)
        }
    }

    private fun endDivision() {
        require(mesh != null)
        require(division != null)
        division = null
    }

    private fun beginNode() {
        val division = division!!
        val newNode = IncompleteNode()

        if (nodeStack.isEmpty()) {
            division.nodes.add(newNode)
        } else {
            nodeStack.last().children.add(newNode)
        }

        nodeStack.add(newNode)
        newNode.id = input.readUTF8()
    }

    private fun endNode() {
        require(nodeStack.isNotEmpty()) { "nodeStack is empty!" }
        nodeStack.removeLast()
    }

    private fun beginGeometry() {
        val mesh = mesh!!
        require(geometry == null) { "$BEGIN_GEOMETRY cannot be nested!" }
        geometry = IncompleteGeometry(mesh)
    }

    private fun endGeometry() {
        val geo = geometry.requireNotNull("$END_GEOMETRY without matching $BEGIN_GEOMETRY seen!")
        geometries.add(geo)
        geometry = null
    }

    private fun readPositions() {
        val geometry = geometry.requireNotNull("$POSITIONS requires a geometry!")
        require(geometry.positions == null) { "Geometry already has a list of positions" }
        geometry.positions = input.readUInt16ArrayAsInt()
    }

    private fun readNormals() {
        val geometry = geometry.requireNotNull("$NORMALS requires a geometry!")
        require(geometry.normals == null) { "Geometry already has a list of normals" }
        geometry.normals = input.readInt32Array()
    }

    private fun readTexCoords() {
        val geometry = geometry.requireNotNull("$TEXCOORDS requires a geometry!")
        require(geometry.texCoords == null) { "Geometry already has a list of texCoords" }
        geometry.texCoords = input.readUInt16ArrayAsInt()

    }

    private fun readMaterial() {
        val division = division!!
        require(division.material == null) { "Division already has a material" }
        val matId = input.readUInt16().toInt()
        division.material = MeshMaterial.fromInt(matId)
    }

    private fun readMatrix() {
        require(nodeStack.isNotEmpty()) { "$MATRIX requires a node!" }
        val last = nodeStack.last()
        require(last.transform == null) { "Node already has a transform: ${last.id}" }
        val arr = input.readFloatArray()
        require(arr.size == 16) { "Float array read as matrix has size ${arr.size}, should be 16" }
        last.transform = Matrix4f(arr)
    }

    private fun readGeometryRef() {
        require(nodeStack.isNotEmpty()) { "$GEOMETRY_REF requires a node!" }
        val last = nodeStack.last()
        require(last.geometry == null) { "Node already has a geometry: ${last.id}" }

        val geometryIdx = input.readUInt16().toInt()
        require(geometryIdx in geometries.indices) { "Geometry idx ($geometryIdx) out of range (${geometries.size})" }

        // Geometries may be reused by multiple nodes!
        last.geometry = geometries[geometryIdx]
    }

    private fun readCollectedPt3f() {
        val mesh = mesh!!
        require(mesh.pt3fList.isEmpty()) { "pt3fList has already been filled!" }
        val arr = input.readFloatArray()
        require(arr.size % 3 == 0) { "readCollectedPt3f: array size is not a multiple of 3!" }

        val numPts = arr.size / 3
        var j = 0

        (0 ..< numPts).forEach {
            val x = arr[j++]
            val y = arr[j++]
            val z = arr[j++]
            mesh.pt3fList.add(Point3f(x, y, z))
        }

        require(j == arr.size)
        require(mesh.pt3fList.size == numPts)
    }

    private fun readCollectedPt2f() {
        val mesh = mesh!!
        require(mesh.pt2fList.isEmpty()) { "pt2fList has already been filled!" }
        val arr = input.readFloatArray()
        require(arr.size % 2 == 0) { "readCollectedPt2f: array size is not a multiple of 2!" }

        val numPts = arr.size / 2
        var j = 0

        (0 ..< numPts).forEach {
            val x = arr[j++]
            val y = arr[j++]
            mesh.pt2fList.add(Point2f(x, y))
        }

        require(j == arr.size)
        require(mesh.pt2fList.size == numPts)
    }

    companion object {
        private val TAG = Log.Tag("MeshFileReader")

        fun readFile(fileName: String): Mesh {
            val path = App.assets.pathToMesh(fileName)
            val file = File(path)
            val mesh = MyDataInputStream.use(file) {
                MeshFileReader(it).read()
            }
            Log.info(TAG, "$fileName: ${mesh.divisions.size} divisions(s)")
            return mesh
        }
    }
}
