package io.github.digorydoo.titanium.engine.file

import ch.digorydoo.kutils.file.KDataInputStream
import ch.digorydoo.kutils.logging.Log
import ch.digorydoo.kutils.matrix.Matrix4f
import ch.digorydoo.kutils.utils.newFloatBuffer
import ch.digorydoo.kutils.vector.Vector2f
import ch.digorydoo.kutils.vector.Vector3f
import io.github.digorydoo.titanium.engine.core.App
import io.github.digorydoo.titanium.engine.file.FileMarker.*
import io.github.digorydoo.titanium.engine.mesh.*
import java.io.BufferedInputStream
import java.io.DataInputStream
import java.io.File
import java.nio.FloatBuffer

/**
 * This class must be thread-safe!
 */
class MeshFileReader private constructor(private val input: KDataInputStream<FileMarker>) {
    private inner class IncompleteNode {
        var id = ""
        var geometryId = -1
        var skeletonId = -1
        var jointIdx = -1
        var transform: Matrix4f? = null
        val children = mutableListOf<IncompleteNode>()

        fun toNode(): MeshNode {
            var geometry: MeshGeometry? = null

            if (geometryId > 0) {
                finalGeometries.forEach {
                    if (it.id == geometryId) {
                        require(geometry == null) { "Geometry id not unique: $geometryId" }
                        geometry = it
                    }
                }

                requireNotNull(geometry) { "Geometry not found: id=$geometryId" }
            }

            var skeleton: Skeleton? = null

            if (skeletonId > 0) {
                finalSkeletons.forEach {
                    if (it.id == skeletonId) {
                        require(skeleton == null) { "Skeleton id not unique: $skeletonId" }
                        skeleton = it
                    }
                }

                requireNotNull(skeleton) { "Skeleton not found: id=$skeletonId" }
            }

            if (jointIdx < 0) {
                require(skeleton == null) { "Node part of skeleton, but jointIdx=$jointIdx" }
            } else {
                requireNotNull(skeleton)
                require(jointIdx in skeleton.joints.indices)
            }

            return MeshNode(
                id = id,
                localTransform = transform, // nullable
                geometry = geometry, // nullable
                children = children.map { it.toNode() },
                skeletonId = skeletonId,
                jointIdx = jointIdx,
            )
        }
    }

    private inner class IncompleteGeometry {
        var id = -1
        var positions: IntArray? = null
        var normals: IntArray? = null
        var texCoords: IntArray? = null
        var material: MeshMaterial? = null

        fun toGeometry(): MeshGeometry {
            require(id > 0) { "Geometry has no id" }

            // If positions and normals are still null at this point, this means that the geometry is empty.
            // This usually happens when a dummy mesh serves as a parent for nested meshes.
            val positions = positions?.let { indexArrayToFloatBuffer3f(it) } ?: emptyFloatBuffer
            val normals = normals?.let { indexArrayToFloatBuffer3f(it) } ?: emptyFloatBuffer

            // texCoords are optional
            val texCoords = texCoords?.let { indexArrayToFloatBuffer2f(it) }

            // We expect MeshFileWriter to propagate parent materials down the tree; null indicates DEFAULT.
            val material = material ?: MeshMaterial.DEFAULT

            return MeshGeometry(
                id = id,
                positions = positions,
                normals = normals,
                texCoords = texCoords, // nullable
                material = material,
            )
        }
    }

    private class IncompleteJoint {
        var invBindMatrix: Matrix4f? = null

        fun toJoint(): SkelJoint {
            return SkelJoint(invBindMatrix ?: Matrix4f.identity)
        }
    }

    private class IncompleteSkeleton {
        var id = -1
        var bindShapeMatrix: Matrix4f? = null
        val joints = mutableListOf<IncompleteJoint>()

        fun toSkeleton(): Skeleton {
            require(id > 0) { "Skeleton has no id" }
            return Skeleton(
                id = id,
                bindShapeMatrix = bindShapeMatrix ?: Matrix4f.identity,
                joints = joints.map { it.toJoint() },
            )
        }
    }

    private inner class IncompleteMesh {
        val geometries = mutableListOf<IncompleteGeometry>()
        val skeletons = mutableListOf<IncompleteSkeleton>()
        val nodes = mutableListOf<IncompleteNode>()

        fun toMesh(): ComplexMesh {
            // Wrap geometries and skeletons first, because node will have to look them up

            require(finalGeometries.isEmpty())
            geometries.forEach { finalGeometries.add(it.toGeometry()) }

            require(finalSkeletons.isEmpty())
            skeletons.forEach { finalSkeletons.add(it.toSkeleton()) }

            require(finalSkeletons.size <= 1) // multiple skeletons not currently supported

            return ComplexMesh(
                geometries = finalGeometries,
                nodes = nodes.map { it.toNode() },
                skeleton = finalSkeletons.firstOrNull(),
            )
        }
    }

    private var mesh: IncompleteMesh? = null
    private var geometry: IncompleteGeometry? = null
    private var finalGeometries = mutableListOf<MeshGeometry>()
    private var finalSkeletons = mutableListOf<Skeleton>()
    private val nodeStack = mutableListOf<IncompleteNode>()
    private var skeleton: IncompleteSkeleton? = null
    private var joint: IncompleteJoint? = null
    private var vec3fArray: Array<Vector3f>? = null
    private var vec2fArray: Array<Vector2f>? = null

    fun read(): ComplexMesh {
        mesh = IncompleteMesh()

        input.readExpected(BEGIN_MESH_FILE)
        var finished = false

        while (!finished) {
            when (val marker = input.readMarker()) {
                END_MESH_FILE -> finished = true
                BEGIN_GEOMETRY -> beginGeometry()
                END_GEOMETRY -> endGeometry()
                POSITIONS -> readPositions()
                NORMALS -> readNormals()
                TEXCOORDS -> readTexCoords()
                MATERIAL -> readMaterial()
                BEGIN_NODE -> beginNode()
                END_NODE -> endNode()
                NODE_TRANSFORM -> readNodeTransform()
                GEOMETRY_REF -> readGeometryRef()
                BEGIN_SKELETON -> beginSkeleton()
                END_SKELETON -> endSkeleton()
                COLLECTED_VEC3F -> readCollectedVec3f()
                COLLECTED_VEC2F -> readCollectedVec2f()
                SKELETON_REF -> readSkeletonRef()
                BEGIN_JOINT -> beginJoint()
                END_JOINT -> endJoint()
                INV_BIND_MATRIX -> readInvBindMatrix()
                BIND_SHAPE_MATRIX -> readBindShapeMatrix()
                else -> throw Exception("Marker not handled: $marker")
            }
        }

        require(geometry == null) { "Geometry not properly ended" }
        require(nodeStack.isEmpty()) { "Node not properly ended" }
        require(skeleton == null) { "Skeleton not properly ended" }
        return mesh!!.toMesh()
    }

    private fun beginGeometry() {
        require(geometry == null) { "Geometries cannot be nested" }
        val geometry = IncompleteGeometry()
        this.geometry = geometry
        geometry.id = input.readUInt16().toInt()
    }

    private fun endGeometry() {
        val geometry = geometry
        requireNotNull(geometry)

        val mesh = mesh
        requireNotNull(mesh)

        mesh.geometries.add(geometry)
        this.geometry = null
    }

    private fun readPositions() {
        val geometry = geometry
        requireNotNull(geometry)
        require(geometry.positions == null)
        geometry.positions = input.readUInt16ArrayAsInt()
    }

    private fun readNormals() {
        val geometry = geometry
        requireNotNull(geometry)
        require(geometry.normals == null)
        geometry.normals = input.readUInt16ArrayAsInt()
    }

    private fun readTexCoords() {
        val geometry = geometry
        requireNotNull(geometry)
        geometry.texCoords = input.readUInt16ArrayAsInt()
    }

    private fun readMaterial() {
        val geometry = geometry
        requireNotNull(geometry)
        require(geometry.material == null)

        val matId = input.readUInt16().toInt()
        geometry.material = MeshMaterial.fromInt(matId)
    }

    private fun beginNode() {
        val mesh = mesh
        requireNotNull(mesh)

        val newNode = IncompleteNode()

        if (nodeStack.isEmpty()) {
            mesh.nodes.add(newNode)
        } else {
            nodeStack.last().children.add(newNode)
        }

        nodeStack.add(newNode)
        newNode.id = input.readUTF8()
    }

    private fun endNode() {
        require(nodeStack.isNotEmpty())
        nodeStack.removeLast()
    }

    private fun readNodeTransform() {
        val node = nodeStack.last()
        require(node.transform == null)

        val arr = input.readFloatArray()
        require(arr.size == 16)
        node.transform = Matrix4f(arr)
    }

    private fun readGeometryRef() {
        // Currently, only nodes can have a GEOMETRY_REF.
        val node = nodeStack.last()
        require(node.geometryId < 0)
        node.geometryId = input.readUInt16().toInt() // will be verified later
    }

    private fun readSkeletonRef() {
        val node = nodeStack.last()
        require(node.skeletonId < 0)
    }

    private fun beginSkeleton() {
        require(skeleton == null) { "Skeletons cannot be nested" }
        val skeleton = IncompleteSkeleton()
        this.skeleton = skeleton
        skeleton.id = input.readUInt16().toInt()
    }

    private fun endSkeleton() {
        val skeleton = skeleton
        requireNotNull(skeleton)

        val mesh = mesh
        requireNotNull(mesh)

        mesh.skeletons.add(skeleton)
        this.skeleton = null
    }

    private fun beginJoint() {
        val skeleton = skeleton
        requireNotNull(skeleton)

        require(joint == null) { "Joints cannot be nested" }
        val joint = IncompleteJoint()
        this.joint = joint

        val name = input.readUTF8()
        val node = findNodeById(name)
        requireNotNull(node)
        require(node.skeletonId == skeleton.id)
        require(node.jointIdx < 0)
        node.jointIdx = skeleton.joints.size
    }

    private fun endJoint() {
        val joint = joint
        requireNotNull(joint)

        val skeleton = skeleton
        requireNotNull(skeleton)

        skeleton.joints.add(joint)
        this.joint = null
    }

    private fun readInvBindMatrix() {
        val joint = joint
        requireNotNull(joint)
        require(joint.invBindMatrix == null)

        val arr = input.readFloatArray()
        require(arr.size == 16)
        joint.invBindMatrix = Matrix4f(arr)
    }

    private fun readBindShapeMatrix() {
        val skeleton = skeleton
        requireNotNull(skeleton)
        require(skeleton.bindShapeMatrix == null)

        val arr = input.readFloatArray()
        require(arr.size == 16)
        skeleton.bindShapeMatrix = Matrix4f(arr)
    }

    private fun findNodeById(id: String): IncompleteNode? {
        val mesh = mesh
        requireNotNull(mesh)

        if (id.isEmpty()) return null

        var result: IncompleteNode? = null

        mesh.nodes.forEach { node ->
            val found = node.findNodeById(id)

            if (found != null) {
                require(result == null) { "Node id not unique: $id" }
                result = found
            }
        }

        return result
    }

    private fun IncompleteNode.findNodeById(id: String): IncompleteNode? {
        if (id.isEmpty()) return null
        if (id == this.id) return this

        var result: IncompleteNode? = null

        children.forEach { child ->
            val found = child.findNodeById(id)

            if (found != null) {
                require(result == null) { "Node id not unique: $id" }
                result = found
            }
        }

        return result
    }

    private fun readCollectedVec3f() {
        require(vec3fArray == null)

        val rawArr = input.readFloatArray()
        require(rawArr.size % 3 == 0)

        val numPts = rawArr.size / 3
        val list = mutableListOf<Vector3f>()
        var j = 0

        (0 ..< numPts).forEach { _ ->
            val x = rawArr[j++]
            val y = rawArr[j++]
            val z = rawArr[j++]
            list.add(Vector3f(x, y, z))
        }

        require(j == rawArr.size)
        require(list.size == numPts)
        vec3fArray = list.toTypedArray()
    }

    private fun readCollectedVec2f() {
        require(vec2fArray == null)

        val rawArr = input.readFloatArray()
        require(rawArr.size % 2 == 0)

        val numPts = rawArr.size / 2
        val list = mutableListOf<Vector2f>()
        var j = 0

        (0 ..< numPts).forEach { _ ->
            val x = rawArr[j++]
            val y = rawArr[j++]
            list.add(Vector2f(x, y))
        }

        require(j == rawArr.size)
        require(list.size == numPts)
        vec2fArray = list.toTypedArray()
    }

    private fun indexArrayToFloatBuffer3f(indices: IntArray): FloatBuffer {
        val vec3fArray = vec3fArray
        requireNotNull(vec3fArray)

        val buf = newFloatBuffer(indices.size * 3)
        buf.position(0)

        for (idx in indices) {
            val vec = vec3fArray[idx]
            buf.put(vec.x)
            buf.put(vec.y)
            buf.put(vec.z)
        }

        return buf
    }

    private fun indexArrayToFloatBuffer2f(indices: IntArray): FloatBuffer {
        val vec2fArray = vec2fArray
        requireNotNull(vec2fArray)

        val buf = newFloatBuffer(indices.size * 2)
        buf.position(0)

        for (idx in indices) {
            val vec = vec2fArray[idx]
            buf.put(vec.x)
            buf.put(vec.y)
        }

        return buf
    }

    companion object {
        private val TAG = Log.Tag("MeshFileReader")
        private val emptyFloatBuffer = newFloatBuffer(0)

        /**
         * This function is internal, because callers should generally go through App.meshes.
         */
        internal fun readFile(fileName: String): ComplexMesh {
            val path = App.assets.pathToMesh(fileName) // read-only access is thread-safe
            val file = File(path)

            val mesh = file.inputStream()
                .let { BufferedInputStream(it) }
                .let { DataInputStream(it) }
                .use { MeshFileReader(KDataInputStream(it, FileMarker::fromUShort)).read() }

            Log.info(TAG, "$fileName: ${mesh.geometries.size} geometries(s)")
            return mesh
        }
    }
}
