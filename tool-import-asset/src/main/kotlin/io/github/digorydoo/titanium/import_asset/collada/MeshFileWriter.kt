package io.github.digorydoo.titanium.import_asset.collada

import ch.digorydoo.kutils.file.KDataOutputStream
import ch.digorydoo.kutils.matrix.Matrix4f
import ch.digorydoo.kutils.vector.Vector2fSet
import ch.digorydoo.kutils.vector.Vector3fSet
import io.github.digorydoo.titanium.engine.file.FileMarker
import io.github.digorydoo.titanium.engine.mesh.MeshMaterial
import io.github.digorydoo.titanium.import_asset.WriterStats
import io.github.digorydoo.titanium.import_asset.collada.ColladaDataAccessor.GeometryData
import io.github.digorydoo.titanium.import_asset.collada.ColladaDataAccessor.SkelController
import io.github.digorydoo.titanium.import_asset.collada.data.Geometry
import io.github.digorydoo.titanium.import_asset.collada.data.VisualSceneNode
import java.io.BufferedOutputStream
import java.io.DataOutputStream
import java.io.File

// In theory, to apply the transformation to the normal would require the inverse transform:
// transformedNormal = transpose(inverse(M3)) × normal
// However, if we assume node matrices do not contain scaling, then we can directly use world
// transforms. Therefore, if normals look odd, try baking transforms in Blender before exporting.

class MeshFileWriter private constructor(
    private val stream: KDataOutputStream<FileMarker>,
    private val accessor: ColladaDataAccessor,
) {
    private val vec3fSet = Vector3fSet()
    private val vec2fSet = Vector2fSet()

    private var nextId = 1 // shared, i.e. ids will be distinct across all kinds of objects
    private val mapGeometryToId = mutableMapOf<Geometry, Int>()
    private val mapSkeletonToId = mutableMapOf<SkelController, Int>()

    private val geometryDataCollected = mutableSetOf<Geometry>()
    private val skeletonDataCollected = mutableSetOf<SkelController>()

    private val stats: WriterStats = object: WriterStats() {
        override val numBytes get() = stream.bytesWritten
        override val numGeometries get() = mapGeometryToId.size
        override val numVec3f get() = vec3fSet.size
        override val numVec2f get() = vec2fSet.size
    }

    private fun write() {
        require(stream.bytesWritten == 0) { "Stream doesn't start out empty!" }

        stats.clear()
        vec3fSet.clear()
        vec2fSet.clear()
        nextId = 1
        mapGeometryToId.clear()
        mapSkeletonToId.clear()
        geometryDataCollected.clear()
        skeletonDataCollected.clear()

        stream.write(FileMarker.BEGIN_MESH_FILE)

        // Write the scene node hierarchy and collect geometries

        accessor.getActiveVisualScene().nodes.forEach {
            writeNode(it, MeshMaterial.DEFAULT)
        }

        // Collect vector data from all geometries and skeletons

        for ((geometry, _) in mapGeometryToId) {
            collectVectorData(geometry)
        }

        for ((skeleton, _) in mapSkeletonToId) {
            collectVectorData(skeleton)
        }

        writeCollectedVectorData()

        // Write the geometries and skeletons using the indices from the collected vector data

        for ((geometry, _) in mapGeometryToId) {
            writeGeometry(geometry)
        }

        for ((skeleton, _) in mapSkeletonToId) {
            writeSkeleton(skeleton)
        }

        stream.write(FileMarker.END_MESH_FILE)
    }

    private fun writeNode(node: VisualSceneNode, parentMaterial: MeshMaterial) {
        if (node.instanceCamera != null || node.instanceLight != null) {
            return
        }

        stream.write(FileMarker.BEGIN_NODE, node.name)
        stats.numNodes++

        val skeleton = node.instanceController?.let { accessor.getNewSkelController(it) }
        val skelGeometry = skeleton?.let { accessor.getSkelGeometry(it) }
        val rigidGeometry = node.instanceGeometry?.let { accessor.getGeometry(it) }

        if (skeleton != null && rigidGeometry != null) {
            // This is unexpected, shouldn't ever happen.
            throw Exception("Node $node has both a rigid geometry and a skeleton")
        }

        val geometry = skelGeometry ?: rigidGeometry // null if neither
        var myMaterial = parentMaterial

        if (geometry != null) {
            var geomId = mapGeometryToId[geometry]

            if (geomId == null) {
                geomId = nextId++
                mapGeometryToId[geometry] = geomId
            }

            stream.writeUInt16(FileMarker.GEOMETRY_REF, geomId)

            // Propagate parent material down (will be needed later)
            val gdata = accessor.getGeometryDataCached(geometry)
            myMaterial = gdata?.material ?: parentMaterial
            gdata?.material = myMaterial
        }

        if (skeleton != null) {
            var skelId = mapSkeletonToId[skeleton]

            if (skelId == null) {
                skelId = nextId++
                mapSkeletonToId[skeleton] = skelId
            }

            stream.writeUInt16(FileMarker.SKELETON_REF, skelId)
        }

        val nodeTransform = node.matrix?.floatArray?.let { Matrix4f(it) }

        if (nodeTransform != null && !nodeTransform.isIdentity()) {
            writeMatrix(FileMarker.NODE_TRANSFORM, nodeTransform)
        }

        node.children.forEach { child ->
            writeNode(child, myMaterial)
        }

        stream.write(FileMarker.END_NODE)
    }

    private fun collectVectorData(geometry: Geometry) {
        if (geometryDataCollected.contains(geometry)) {
            stats.numGeometriesReused++
            return // this geometry's mesh is used more than once
        }

        geometryDataCollected.add(geometry)
        val gdata = accessor.getGeometryDataCached(geometry) ?: return

        vec3fSet.addAll(gdata.positions)
        vec3fSet.addAll(gdata.normals)
        gdata.texCoords?.let { vec2fSet.addAll(it) }
    }

    private fun collectVectorData(skeleton: SkelController) {
        if (skeletonDataCollected.contains(skeleton)) {
            throw Exception("Skeleton unexpectedly used more than once")
        }

        skeletonDataCollected.add(skeleton)

        // Currently no vector data; FIXME can this function be removed?
        // val skin = skeleton.controller.skin!!
        // val skelData = accessor.getSkelData(skin)
    }

    private fun writeCollectedVectorData() {
        stream.write(FileMarker.COLLECTED_VEC3F)
        stream.write(vec3fSet.toFloatArray())

        stream.write(FileMarker.COLLECTED_VEC2F)
        stream.write(vec2fSet.toFloatArray())
    }

    private fun writeGeometry(geometry: Geometry) {
        val id = mapGeometryToId[geometry]
        requireNotNull(id) { "Geometry wasn't assigned an id" }

        stream.writeUInt16(FileMarker.BEGIN_GEOMETRY, id)

        accessor.getGeometryDataCached(geometry)?.let {
            writeGeometryData(it)
        }

        stream.write(FileMarker.END_GEOMETRY)
    }

    private fun writeGeometryData(gdata: GeometryData) {
        val material = gdata.material
        requireNotNull(material) { "Parent material wasn't properly propagated to geometries" }
        stats.didUseMaterial(material)

        if (material != MeshMaterial.DEFAULT) {
            stream.writeUInt16(FileMarker.MATERIAL, material.value)
        }

        vec3fSet.findIndices(gdata.positions) // throws if a position is not found
            .toIntArray()
            .let { stream.writeIntArrayAsUInt16(FileMarker.POSITIONS, it) }

        vec3fSet.findIndices(gdata.normals)
            .toIntArray()
            .let { stream.writeIntArrayAsUInt16(FileMarker.NORMALS, it) }

        stats.numPositions += gdata.positions.size
        stats.numNormals += gdata.normals.size

        gdata.texCoords?.let { texCoords ->
            vec2fSet.findIndices(texCoords)
                .toIntArray()
                .let { stream.writeIntArrayAsUInt16(FileMarker.TEXCOORDS, it) }

            stats.numTexCoords += texCoords.size
        }
    }

    private fun writeSkeleton(skeleton: SkelController) {
        val id = mapSkeletonToId[skeleton]
        requireNotNull(id) { "Skeleton wasn't assigned an id" }

        stream.writeUInt16(FileMarker.BEGIN_SKELETON, id)

        val skin = skeleton.controller.skin!!
        val bindShapeMatrix = skin.bindShapeMatrix?.floatArray?.let { Matrix4f(it) }

        if (bindShapeMatrix != null && !bindShapeMatrix.isIdentity()) {
            writeMatrix(FileMarker.BIND_SHAPE_MATRIX, bindShapeMatrix)
        }

        val skelData = accessor.getSkelDataCached(skeleton)

        for (joint in skelData.joints) {
            require(joint.name.isNotEmpty()) { "Joint name cannot be empty" }

            val node = accessor.getNodeById(joint.name)
            requireNotNull(node) { "Node of joint ${joint.name} not found" }

            stream.write(FileMarker.BEGIN_JOINT, joint.name)

            if (!joint.invBindMatrix.isIdentity()) {
                writeMatrix(FileMarker.INV_BIND_MATRIX, joint.invBindMatrix)
            }

            stream.write(FileMarker.END_JOINT)
        }

        stream.write(FileMarker.END_SKELETON)
    }

    private fun writeMatrix(marker: FileMarker, mat: Matrix4f) {
        stream.write(marker)
        stream.write(mat.buffer)
        stats.numMatrices++
    }

    companion object {
        fun write(accessor: ColladaDataAccessor, file: File): WriterStats =
            file.outputStream()
                .let { BufferedOutputStream(it) }
                .let { DataOutputStream(it) }
                .use {
                    val writer = MeshFileWriter(KDataOutputStream(it), accessor)
                    writer.write()
                    writer.stats
                }
    }
}
