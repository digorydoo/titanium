package ch.digorydoo.titanium.import_asset.collada

import ch.digorydoo.kutils.matrix.Matrix4f
import ch.digorydoo.kutils.point.Point2fSet
import ch.digorydoo.kutils.point.Point3fSet
import ch.digorydoo.titanium.engine.file.FileMarker
import ch.digorydoo.titanium.engine.file.MyDataOutputStream
import ch.digorydoo.titanium.engine.mesh.MeshMaterial
import ch.digorydoo.titanium.import_asset.WriterStats
import ch.digorydoo.titanium.import_asset.collada.data.Geometry
import ch.digorydoo.titanium.import_asset.collada.data.VisualScene
import ch.digorydoo.titanium.import_asset.collada.data.VisualSceneNode

class MeshFileWriter(private val stream: MyDataOutputStream, private val accessor: ColladaDataAccessor) {
    private val pt3fSet = Point3fSet()
    private val pt2fSet = Point2fSet()
    private val geometriesActuallyUsed = mutableSetOf<Geometry>()

    val stats: WriterStats = object: WriterStats() {
        override val numBytes get() = stream.bytesWritten
        override val numGeometries get() = geometriesActuallyUsed.size
        override val numPt3f get() = pt3fSet.size
        override val numPt2f get() = pt2fSet.size
    }

    fun write() {
        stats.clear()
        pt3fSet.clear()
        pt2fSet.clear()
        geometriesActuallyUsed.clear()
        require(stream.bytesWritten == 0) { "Stream doesn't start out empty!" }

        // First pass: gather the data to be written

        val visualScene = accessor.getActiveVisualScene()
        visualScene.nodes.forEach { prepareNode(it) }

        // No data should have been emitted yet

        require(stream.bytesWritten == 0) { "bytesWritten != 0 after PREPARE" }

        // Since we count most objects in the second pass, these counts must still be 0

        require(stats.numNodes == 0) { "numNodes != 0 after PREPARE" }
        require(stats.numPositions == 0) { "numPositions != 0 after PREPARE" }
        require(stats.numNormals == 0) { "numNormals != 0 after PREPARE" }
        require(stats.numTexCoords == 0) { "numTexCoords != 0 after PREPARE" }
        require(stats.numMatrices == 0) { "numMatrices != 0 after PREPARE" }

        // Second pass: Actually write the data

        stream.write(FileMarker.BEGIN_MESH_FILE)

        stream.write(FileMarker.COLLECTED_POINT3F)
        stream.write(pt3fSet.toFloatArray())

        stream.write(FileMarker.COLLECTED_POINT2F)
        stream.write(pt2fSet.toFloatArray())

        geometriesActuallyUsed.forEach { writeGeometry(it) }

        MeshMaterial.entries.forEach { material ->
            writeDivision(visualScene, material)
        }

        stream.write(FileMarker.END_MESH_FILE)
    }

    private fun prepareNode(node: VisualSceneNode) {
        var geometryCount = 0

        node.instanceGeometry
            ?.let { accessor.getGeometry(it) }
            ?.let { prepareGeometry(it) }
            ?.also { geometryCount++ }

        node.instanceController
            ?.let { accessor.getSkelController(it) }
            ?.let { accessor.getSkelGeometry(it) }
            ?.let { prepareGeometry(it) }
            ?.also { geometryCount++ }

        require(geometryCount <= 1) { "Node using more than one geometry: ${node.id}" }

        node.children.forEach { prepareNode(it) }
    }

    private fun writeDivision(visualScene: VisualScene, material: MeshMaterial) {
        var didStartDivision = false

        visualScene.nodes.forEach { node ->
            if (hasAnyDataForMaterial(node, material, MeshMaterial.DEFAULT)) {
                if (!didStartDivision) {
                    stream.write(FileMarker.BEGIN_DIVISION)
                    stream.writeUInt16(FileMarker.MATERIAL, material.value)
                    stats.didUseMaterial(material)
                    didStartDivision = true
                }

                writeNode(node, material, MeshMaterial.DEFAULT)
            }
        }

        if (didStartDivision) {
            stream.write(FileMarker.END_DIVISION)
        }
    }

    private fun hasAnyDataForMaterial(
        node: VisualSceneNode,
        requiredMaterial: MeshMaterial,
        materialOfParentNode: MeshMaterial,
    ): Boolean {
        if (node.instanceCamera != null || node.instanceLight != null) {
            return false
        }

        val geometry = node.instanceGeometry?.let { accessor.getGeometry(it) }
        val myMaterial: MeshMaterial

        if (geometry != null) {
            val gdata = accessor.getGeometryData(geometry)
            myMaterial = gdata?.material ?: materialOfParentNode

            if (myMaterial == requiredMaterial) {
                return true
            }
        } else {
            myMaterial = materialOfParentNode
        }

        // Even if this node's geometry is not meant for the material, one of its children may be.
        return node.children.any { hasAnyDataForMaterial(it, requiredMaterial, myMaterial) }
    }

    private fun writeNode(
        node: VisualSceneNode,
        requiredMaterial: MeshMaterial,
        materialOfParentNode: MeshMaterial,
    ) {
        // We assume hasAnyDataForMaterial is true for this node when this method was called.
        // But this does not necessarily mean that our own geometry is meant for the material.

        stream.write(FileMarker.BEGIN_NODE, node.name)
        stats.numNodes++

        val geometry = node.instanceGeometry?.let { accessor.getGeometry(it) }
        val myMaterial: MeshMaterial

        if (geometry != null) {
            val gdata = accessor.getGeometryData(geometry)
            myMaterial = gdata?.material ?: materialOfParentNode

            if (myMaterial == requiredMaterial) {
                // This node has data for the required material.
                writeGeometryRef(geometry)
            }
        } else {
            myMaterial = materialOfParentNode
        }

        val skelCtrl = node.instanceController
            ?.let { accessor.getSkelController(it) }

        var matrix = node.matrix?.floatArray?.let { Matrix4f(it) } ?: Matrix4f.identity

        if (skelCtrl != null) {
            val skelGeometry = accessor.getSkelGeometry(skelCtrl)
            writeGeometryRef(skelGeometry)

            val skin = skelCtrl.controller.skin!!
            val skelMatrix = skin.bindShapeMatrix?.floatArray?.let { Matrix4f(it) }

            if (skelMatrix != null) {
                matrix *= skelMatrix // is this correct? test object has identity in var matrix
            }

            val skelData = accessor.getSkelData(skin)
            require(skelData.weightJointInput.semantic == "JOINT") // redundant check; to suppress unused local val

            // TODO Actually export the vertex weights and counts.
        }

        matrix.takeIf { !it.isIdentity() }
            ?.let { writeMatrix(it) }

        node.children.forEach { child ->
            if (hasAnyDataForMaterial(child, requiredMaterial, myMaterial)) {
                writeNode(child, requiredMaterial, myMaterial)
            }
        }

        stream.write(FileMarker.END_NODE)
    }

    private fun prepareGeometry(geometry: Geometry) {
        if (geometriesActuallyUsed.contains(geometry)) {
            stats.numGeometriesReused++
            return // this geometry's mesh is used more than once
        }

        geometriesActuallyUsed.add(geometry)
        val gdata = accessor.getGeometryData(geometry)

        if (gdata != null) {
            pt3fSet.addAll(gdata.positions)
            pt3fSet.addAll(gdata.normals)
            gdata.texCoords?.let { pt2fSet.addAll(it) }
        }
    }

    private fun writeGeometryRef(geometry: Geometry) {
        val idx = geometriesActuallyUsed.indexOf(geometry)
        require(idx >= 0) { "Geometry wasn't added to list: ${geometry.id}" }
        stream.writeUInt16(FileMarker.GEOMETRY_REF, idx)
    }

    private fun writeGeometry(geometry: Geometry) {
        stream.write(FileMarker.BEGIN_GEOMETRY)

        val gdata = accessor.getGeometryData(geometry)

        if (gdata != null) {
            val positionIndices = pt3fSet.findIndices(gdata.positions).toIntArray()
            stream.writeIntArrayAsUInt16(FileMarker.POSITIONS, positionIndices)
            stats.numPositions += gdata.positions.size

            val normalIndices = pt3fSet.findIndices(gdata.normals).toIntArray()
            stream.writeIntArrayAsInt32(FileMarker.NORMALS, normalIndices)
            stats.numNormals += gdata.normals.size

            gdata.texCoords?.let { texCoords ->
                val texCoordsIndices = pt2fSet.findIndices(texCoords).toIntArray()
                stream.writeIntArrayAsUInt16(FileMarker.TEXCOORDS, texCoordsIndices)
                stats.numTexCoords += texCoords.size
            }
        }

        stream.write(FileMarker.END_GEOMETRY)
    }

    private fun writeMatrix(matrix: Matrix4f) {
        stream.write(FileMarker.MATRIX)
        stream.write(matrix.buffer)
        stats.numMatrices++
    }
}
