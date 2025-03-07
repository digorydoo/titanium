package ch.digorydoo.titanium.import_asset.collada

import ch.digorydoo.kutils.utils.requireExactlyOne
import ch.digorydoo.kutils.utils.requireHash
import ch.digorydoo.kutils.utils.requireNotNull
import ch.digorydoo.titanium.engine.mesh.MeshMaterial
import ch.digorydoo.titanium.import_asset.collada.data.*

class ColladaDataAccessor(private val data: ColladaData) {
    class GeometryData(
        val positions: FloatArray,
        val normals: FloatArray,
        val texCoords: FloatArray?,
        val material: MeshMaterial?,
    )

    class SkelController(
        @Suppress("unused") val skeleton: VisualSceneNode,
        val controller: Controller,
    )

    class SkelData(
        @Suppress("unused") val jointSource: SkinSource,
        @Suppress("unused") val poseMatrixSource: SkinSource,
        @Suppress("unused") val weightsSource: SkinSource,
        @Suppress("unused") val jointAccessor: SkinTechCommonAccessor,
        @Suppress("unused") val poseAccessor: SkinTechCommonAccessor,
        val weightJointInput: MeshInput,
        @Suppress("unused") val weightsAccessor: SkinTechCommonAccessor,
    )

    fun getActiveVisualScene(): VisualScene {
        val scene = data.scene.requireNotNull("data.scene")
        val instanceVisualScene = scene.instanceVisualScene.requireNotNull("instanceVisualScene")
        val visualSceneId = instanceVisualScene.url.requireHash("sceneId")

        return data.visualScenes
            .filter { it.id == visualSceneId }
            .requireExactlyOne("visualScene with visualSceneId=$visualSceneId")
    }

    fun getSkelController(instance: InstanceController): SkelController {
        val ctrl = getController(instance)
        val skelId = instance.skeleton!!.value.requireHash("skelId")
        val skel = getNodeById(skelId)
        return SkelController(skel!!, ctrl)
    }

    fun getSkelGeometry(skelCtrl: SkelController): Geometry {
        val skin = skelCtrl.controller.skin.requireNotNull("skin")
        val geometryId = skin.source.requireHash("geometryId")
        return getGeometryById(geometryId)!!
    }

    private fun getGeometryById(geometryId: String): Geometry? {
        data.geometries.forEach { geometry ->
            if (geometry.id == geometryId) {
                return geometry
            }
        }

        return null
    }

    private fun getNodeById(nodeId: String): VisualSceneNode? {
        data.visualScenes.forEach { visualScene ->
            visualScene.nodes.forEach { node ->
                val found = getNodeById(nodeId, node)
                if (found != null) return found
            }
        }

        return null
    }

    private fun getNodeById(nodeId: String, startWith: VisualSceneNode): VisualSceneNode? {
        if (startWith.id == nodeId) {
            return startWith
        }

        startWith.children.forEach { child ->
            val found = getNodeById(nodeId, child)
            if (found != null) return found
        }

        return null
    }

    private fun getController(instance: InstanceController): Controller {
        val ctrlId = instance.url.requireHash("ctrlId")

        return data.controllers
            .filter { it.id == ctrlId }
            .requireExactlyOne("Controller with id=$ctrlId")
    }

    fun getGeometry(instance: InstanceGeometry): Geometry {
        val geometryId = instance.url.requireHash("geometryId")

        return data.geometries
            .filter { it.id == geometryId }
            .requireExactlyOne("geometry with id=$geometryId")
    }

    fun getGeometryData(geometry: Geometry): GeometryData? {
        val ctx = "Geometry ${geometry.id}"
        val mesh = geometry.mesh.requireNotNull("$ctx: mesh")

        // if triangles is null, it may be a parent with no mesh data of its own
        val triangles = mesh.triangles ?: return null

        val material = triangles.material
            .takeIf { it.isNotEmpty() }
            ?.let { parseMaterial(it) }

        val vertexSrc = getVertexSource(mesh, triangles, "$ctx: vertexSrc")
        val normalSrc = getNormalSource(mesh, triangles, "$ctx: normalSrc")
        val texCoordSrc = getTexCoordSource(mesh, triangles, "$ctx: texCoordSrc")

        val (vertexArr, vertexSrcAccessor) = getArrayAndAccessor(vertexSrc, "$ctx: vertexSrc")
        val (normalArr, normalSrcAccessor) = getArrayAndAccessor(normalSrc, "$ctx: normalSrc")

        val (texCoordArr, texCoordSrcAccessor) =
            if (texCoordSrc == null) Pair(null, null)
            else getArrayAndAccessor(texCoordSrc, "$ctx: texCoordSrc")

        requireParams(vertexSrcAccessor.params, arrayOf("X", "Y", "Z"), "$ctx: vertexSrcAccessor")
        requireParams(normalSrcAccessor.params, arrayOf("X", "Y", "Z"), "$ctx: normalSrcAccessor")

        if (texCoordSrcAccessor != null) {
            requireParams(texCoordSrcAccessor.params, arrayOf("S", "T"), "$ctx: texCoordSrcAccessor")
        }

        val triangleMeshIntArray = triangles.p.requireNotNull("$ctx: triangleMeshIntArray")
        val triangleArr = triangleMeshIntArray.intArray

        val numCornersPerTriangle = 3
        val expectedSize = triangles.count * numCornersPerTriangle * (2 + if (texCoordSrcAccessor == null) 0 else 1)

        require(triangleArr.size == expectedSize) {
            "$ctx: triangleArr: Size is ${triangleArr.size}, expected was $expectedSize\n"
        }

        val positions = FloatArray(triangles.count * numCornersPerTriangle * vertexSrcAccessor.stride) { 0.0f }
        val normals = FloatArray(triangles.count * numCornersPerTriangle * normalSrcAccessor.stride) { 0.0f }

        // Due to a bug in linter, I can't join the following declaration with the if statement
        var texCoords: FloatArray? = null

        if (texCoordSrcAccessor != null) {
            texCoords = FloatArray(triangles.count * numCornersPerTriangle * texCoordSrcAccessor.stride) { 0.0f }
        }

        var meshIdx = 0
        var positionIdx = 0
        var normalIdx = 0
        var texCoordIdx = 0

        for (triangleIdx in 0 ..< triangles.count) {
            for (corner in 0 ..< numCornersPerTriangle) {
                // Vertex
                val avi = triangleArr[meshIdx++]
                positions[positionIdx++] = vertexArr[avi * 3] // x
                positions[positionIdx++] = vertexArr[avi * 3 + 1] // y
                positions[positionIdx++] = vertexArr[avi * 3 + 2] // z

                // Normal
                val ani = triangleArr[meshIdx++]
                normals[normalIdx++] = normalArr[ani * 3] // x
                normals[normalIdx++] = normalArr[ani * 3 + 1] // y
                normals[normalIdx++] = normalArr[ani * 3 + 2] // z

                // TexCoord
                if (texCoords != null) {
                    val ati = triangleArr[meshIdx++]
                    texCoords[texCoordIdx++] = texCoordArr!![ati * 2] // s
                    texCoords[texCoordIdx++] = texCoordArr[ati * 2 + 1] // t
                }
            }
        }

        return GeometryData(positions, normals, texCoords, material)
    }

    private fun parseMaterial(name: String): MeshMaterial {
        // We could look up the material in
        //    ColladaData.materials -> instanceEffect -> ColladaData.effects -> profileCommon.technique.lambert
        // Unfortunately, the values there are VERY incomplete since either Collada or Blender's exporter do not
        // support it. So, as a workaround, we give each material the name of enum class MeshMaterial, which will
        // recreate the material in the game (completely ignoring the lambert values from XML).
        val mat = name.replace("-material", "")
        return MeshMaterial.fromString(mat)
            ?: MeshMaterial.DEFAULT.also { println("Warning: Ignoring unknown material $mat") }
    }

    private fun getVertexSource(mesh: Mesh, triangles: MeshTriangles, ctx: String): MeshSource {
        val vertexInput = triangles.input
            .filter { it.semantic == "VERTEX" }
            .requireExactlyOne("$ctx: VERTEX input")

        require(vertexInput.offset == 0) { "$ctx: vertexInput.offset is ${vertexInput.offset}, should be 0" }

        val vertexWrapSrcId = vertexInput.source.requireHash("$ctx: vertexWrapSrcId")
        val vertexWrapSources = mesh.vertices.let { if (it == null) listOf() else listOf(it) }

        val vertexWrapSource = vertexWrapSources
            .filter { it.id == vertexWrapSrcId }
            .requireExactlyOne("$ctx: Vertex wrap source with id=$vertexWrapSrcId")

        val vertexWrapSrcInput = vertexWrapSource.input.requireExactlyOne("$ctx: vertexWrapSource.input")
        val vertexRealSrcId = vertexWrapSrcInput.source.requireHash("$ctx: vertexRealSrcId")

        return mesh.sources
            .filter { it.id == vertexRealSrcId }
            .requireExactlyOne("$ctx: Source for vertexRealSrcId=$vertexRealSrcId")
    }

    private fun getNormalSource(mesh: Mesh, triangles: MeshTriangles, ctx: String): MeshSource {
        val input = triangles.input
            .filter { it.semantic == "NORMAL" }
            .requireExactlyOne("$ctx: NORMAL input")

        require(input.offset == 1) { "$ctx: input.offset is ${input.offset}, should be 1" }

        val normalSrcId = input.source.requireHash("$ctx: normalSourceId")

        return mesh.sources
            .filter { it.id == normalSrcId }
            .requireExactlyOne("$ctx: Source for normalSrcId=$normalSrcId")
    }

    private fun getTexCoordSource(mesh: Mesh, triangles: MeshTriangles, ctx: String): MeshSource? {
        val inputs = triangles.input
            .filter { it.semantic == "TEXCOORD" }
            .takeIf { it.isNotEmpty() }
            ?: return null

        val input = inputs.requireExactlyOne("$ctx: TEXCOORD input")
        require(input.offset == 2) { "$ctx: input.offset is ${input.offset}, should be 2" }

        val texCoordSrcId = input.source.requireHash("$ctx: texCoordSrcId")

        return mesh.sources
            .filter { it.id == texCoordSrcId }
            .requireExactlyOne("$ctx: Source for texCoordSrcId=$texCoordSrcId")
    }

    private fun getArrayAndAccessor(source: MeshSource, ctx: String): Pair<FloatArray, MeshTechCommonAccessor> {
        val techCommon = source.techCommon.requireNotNull("$ctx: techCommon")
        val accessor = techCommon.accessor.requireNotNull("$ctx: accessor")
        val params = accessor.params
        val meshFloatArray = source.meshFloatArray.requireNotNull("$ctx: meshFloatArray")
        val arr = meshFloatArray.floatArray

        require(arr.size == accessor.count * accessor.stride) { "$ctx: arr: size doesn't match count * stride" }
        require(params.size == accessor.stride) { "$ctx: accessor param count must equal stride" }

        return Pair(arr, accessor)
    }

    private fun requireParams(params: List<MeshAccessorParam>, expected: Array<String>, ctx: String) {
        require(params.size == expected.size) { "$ctx: params.size is ${params.size}, expected was ${expected.size}" }

        params.forEachIndexed { i, param ->
            require(param.name == expected[i]) { "$ctx: param[$i] is \"${param.name}\", expected was ${expected[i]}" }
        }
    }

    private fun getJointSource(skin: Skin): SkinSource? {
        val jointSrcId = skin.joints?.jointInput?.source?.requireHash("jointSourceId")
        return skin.sources.find { it.id == jointSrcId }
            ?.also { require(it.floatArray == null) }
    }

    private fun getJointAccessor(jointSrc: SkinSource): SkinTechCommonAccessor {
        val nameArray = jointSrc.nameArray!!
        val acc = jointSrc.techCommon!!.accessor!!
        require(acc.source == "#${nameArray.id}")
        require(acc.count == nameArray.count) // the number of nodes involved
        require(acc.stride == 1)
        require(acc.param!!.name == "JOINT")
        require(acc.param!!.type == "name")
        return acc
    }

    private fun getPoseMatrixSource(skin: Skin): SkinSource? {
        val poseMatrixSrcId = skin.joints?.invBindMatrixInput?.source?.requireHash("poseMatrixSrcId")
        return skin.sources.find { it.id == poseMatrixSrcId }
            ?.also { require(it.nameArray == null) }
    }

    private fun getPoseAccessor(poseMatrixSource: SkinSource, nameArray: NameArray): SkinTechCommonAccessor {
        val acc = poseMatrixSource.techCommon!!.accessor!!
        require(acc.count == nameArray.count) // one matrix per node
        require(acc.stride == 16)
        require(acc.param!!.name == "TRANSFORM")
        require(acc.param!!.type == "float4x4")

        val poseMatrixValues = poseMatrixSource.floatArray!!
        require(acc.source == "#${poseMatrixValues.id}")
        require(poseMatrixValues.floatArray.size == nameArray.count * 16) // 16 values per matrix

        return acc
    }

    private fun getWeightsSource(skin: Skin): SkinSource? {
        val vertexWeights = skin.vertexWeights!!
        // require(vertexWeights.count == number of positions in skelGeometry)

        val weightInput = vertexWeights.weightInput!!
        require(weightInput.semantic == "WEIGHT")
        require(weightInput.offset == 1)

        val weightsSrcId = weightInput.source.requireHash("weightsSrcId")
        return skin.sources.find { it.id == weightsSrcId }
    }

    private fun getWeightJointInput(skin: Skin, jointSrcId: String): MeshInput {
        val vertexWeights = skin.vertexWeights!!
        val weightJointInput = vertexWeights.jointInput!!
        require(weightJointInput.semantic == "JOINT")
        require(weightJointInput.source == "#${jointSrcId}")
        require(weightJointInput.offset == 0)
        return weightJointInput
    }

    private fun getWeightsAccessor(skin: Skin, weightsSource: SkinSource): SkinTechCommonAccessor {
        val weightValues = weightsSource.floatArray!!
        val acc = weightsSource.techCommon!!.accessor!!
        require(acc.source == "#${weightValues.id}")
        require(acc.count == weightValues.floatArray.size)
        require(acc.stride == 1)
        require(acc.param!!.name == "WEIGHT")
        require(acc.param!!.type == "float")

        val vertexWeights = skin.vertexWeights!!
        require(vertexWeights.vertices.size == acc.count * 2)
        require(vertexWeights.vcount.size == vertexWeights.count)

        return acc
    }

    fun getSkelData(skin: Skin): SkelData {
        val jointSource = getJointSource(skin)!!
        val poseMatrixSource = getPoseMatrixSource(skin)!!
        val weightsSource = getWeightsSource(skin)!!

        return SkelData(
            jointSource,
            poseMatrixSource,
            weightsSource,
            jointAccessor = getJointAccessor(jointSource),
            poseAccessor = getPoseAccessor(poseMatrixSource, jointSource.nameArray!!),
            weightJointInput = getWeightJointInput(skin, jointSource.id),
            weightsAccessor = getWeightsAccessor(skin, weightsSource),
        )
    }
}
