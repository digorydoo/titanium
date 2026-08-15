package io.github.digorydoo.titanium.main.renderer

import ch.digorydoo.kutils.logging.Log
import ch.digorydoo.kutils.matrix.Matrix4f
import io.github.digorydoo.titanium.engine.core.App
import io.github.digorydoo.titanium.engine.mesh.ComplexMeshRenderer
import io.github.digorydoo.titanium.engine.mesh.MeshGeometry
import io.github.digorydoo.titanium.engine.mesh.MeshMaterial
import io.github.digorydoo.titanium.engine.mesh.MeshNode
import io.github.digorydoo.titanium.engine.mesh.Skeleton
import io.github.digorydoo.titanium.engine.shader.ShaderManager.ShaderFlags
import io.github.digorydoo.titanium.engine.shader.ShaderProgram.ProgramType
import io.github.digorydoo.titanium.main.core.LeakDetector
import io.github.digorydoo.titanium.main.mesh.MaterialProps
import io.github.digorydoo.titanium.main.opengl.checkGLError
import io.github.digorydoo.titanium.main.shader.Shader
import io.github.digorydoo.titanium.main.shader.ShaderAttributes.Attribute
import io.github.digorydoo.titanium.main.shader.ShaderVBO
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER
import org.lwjgl.opengl.GL15.glBindBuffer

class ComplexMeshRendererImpl(
    private val delegate: Delegate,
    @Suppress("unused") private val antiAliasing: Boolean, // texturing not currently implemented (see below)
    private val cullFace: Boolean,
    private val depthTest: Boolean,
    private val hasSkeleton: Boolean,
): ComplexMeshRenderer() {
    private val shader = Shader(ProgramType.MESH, if (hasSkeleton) setOf(ShaderFlags.SKELETON) else null)
    private val positionVBO = ShaderVBO()
    private val normalVBO = ShaderVBO()

    init {
        shader.create()
        positionVBO.create(ShaderVBO.Type.DYNAMIC_DRAW)
        normalVBO.create(ShaderVBO.Type.DYNAMIC_DRAW)
    }

    private val leakDetector = LeakDetector(TAG.name, initiallyValid = true)

    override fun free() {
        if (leakDetector.resourceValid) {
            shader.free()
            positionVBO.free()
            normalVBO.free()
            leakDetector.resourceValid = false
        }
    }

    private var prevMaterialUploaded: MeshMaterial? = null

    override fun renderShadows() {}
    override fun renderTransparent() {}

    override fun renderSolid() {
        require(leakDetector.resourceValid)
        val mesh = delegate.mesh ?: return

        checkGLError()
        glDisable(GL_BLEND)

        if (depthTest) {
            glEnable(GL_DEPTH_TEST)
            glDepthFunc(GL_LESS)
        } else {
            glDisable(GL_DEPTH_TEST)
        }

        if (cullFace) {
            glEnable(GL_CULL_FACE)
            glCullFace(GL_BACK) // culls the back face
            glFrontFace(GL_CCW)
        } else {
            glDisable(GL_CULL_FACE)
        }

        shader.program.use()
        checkGLError()

        val lgt = App.scene.lighting

        shader.uniforms.apply {
            setAmbientLightColour(lgt.ambientLight)
            setDiffuseLightColour(lgt.diffuseLight)
            setSkyColour1(lgt.skyColour1)
            setSunDir(lgt.sunDir.vector)
            setHazyColour(lgt.hazyColour)
            setHaziness(lgt.haziness)
            setBrightness(lgt.brightness)
            setContrast(lgt.contrast)

            setCameraSourcePos()
            setProjection()
            setScaleFactor(delegate.scaleFactor)
            setTranslation(delegate.renderPos)
            setRotationPhi(delegate.rotationPhi)
        }

        shader.bindVAO()

        if (hasSkeleton) {
            val skeleton = mesh.skeleton
            require(skeleton != null) { "Enabling SKELETON shader flag for mesh without skeleton is inefficient" }
            updateSkeleton(skeleton)
        }

        prevMaterialUploaded = null

        for (node in mesh.nodes) {
            renderSolidRecursively(node, Matrix4f.identity)
        }

        glDisable(GL_CULL_FACE) // FIXME AppImpl.renderState.cullFace = false could avoid repeatedly calling GL
        glDisable(GL_DEPTH_TEST) // FIXME and even AppImpl.renderState.reset()
    }

    /**
     * Traverses the node hierarchy to compute the skinMatrix of each of the skeleton's joints.
     */
    private fun updateSkeleton(skeleton: Skeleton) {
        for (joint in skeleton.joints) {
            val localAnimatedTransform = joint.localAnimatedTransform
            val skinTransform = joint.skinTransform

            localAnimatedTransform.setIdentity() // TODO apply rotation of joint (but only if values have changed)

            // The skinTransform is part of the matrix used for skinning, but it lacks the joint's global bind-pose
            // transform, which will be sent separately to the GPU through Transform. (It's the same as the accumulated
            // node transforms, since we don't keep a separate structure for the skeleton.)
            //    The invBindMatrix is the inverse of the joint's global bind-pose transform, i.e. the accumulated
            // node transforms from the skeleton root down to and including this joint. If we wanted to bake any of
            // those node transforms into the vertex data, we would have to adjust the invBindMatrix accordingly,
            // otherwise the skinning matrices would no longer match the vertex coordinate space.

            skinTransform.setMultiplied(localAnimatedTransform, joint.invBindMatrix)
            skinTransform *= skeleton.bindShapeMatrix
        }

        // TODO
        // 1. Create a very simple rigged object in Blender and import it here (needs a new Gel).
        // 2. Export weight and index information from COLLADA importer. Needs to determine 4 largest influences.
        // 3. Pass SkinTransforms down (array of uniforms)
        // 4. Pass indices and weights down (vertex attributes)

        // For geometries not taking part of the skeleton, I can
        // set skinMatrices[0] to the worldTransform
        // set index to (0, 0, 0, 0)
        // set weight to (1f, 0f, 0f, 0f)

        // Each vertex should have
        // up to 4 bone indices, i.e. boneIndices: Vector4i
        // up to 4 bone weights, i.e. boneWeights: Vector4f
        // If a vertex has more than four influences, most engines simply keep the four largest weights and
        // renormalize them
    }

    private fun renderSolidRecursively(node: MeshNode, parentTransform: Matrix4f) {
        val worldTransform = node.worldTransform
        worldTransform.set(parentTransform)
        node.localTransform?.let { worldTransform *= it }

        node.geometry?.let { renderGeometry(it, worldTransform) }

        node.children?.let { children ->
            for (child in children) {
                renderSolidRecursively(child, worldTransform)
            }
        }
    }

    private fun renderGeometry(geometry: MeshGeometry, worldTransform: Matrix4f) {
        val positions = geometry.positions

        val numPositions = positions.limit() // limit <= capacity
        if (numPositions <= 0) return

        positionVBO.bind()
        positionVBO.setData(positions)
        shader.connectToVBO(Attribute.ModelPos) // can I move this out?

        normalVBO.bind()
        normalVBO.setData(geometry.normals)
        shader.connectToVBO(Attribute.Normal) // can I move this out?

        if (geometry.material != prevMaterialUploaded) {
            uploadMaterial(geometry.material)
            prevMaterialUploaded = geometry.material
        }

        shader.uniforms.setTransform(worldTransform)

        glDrawArrays(GL_TRIANGLES, 0, numPositions / 3)
        checkGLError()

        glBindBuffer(GL_ARRAY_BUFFER, 0) // unbind
    }

    private fun uploadMaterial(mat: MeshMaterial) {
        val props = MaterialProps.get(mat)

        shader.uniforms.apply {
            setAmbientLightAmount(props.ambientLightAmount)
            setDiffuseLightAmount(props.diffuseLightAmount)
            setShininess(props.shininess)
            setContourIntensity(props.contourIntensity)
            setContourRamp(props.contourRamp)
            setContourWidth(props.contourWidth)
            setContourTopReflectsSky(props.contourTopReflectsSky)
            setEmittingLight(
                props.emittingLight.red + delegate.emittingLight,
                props.emittingLight.green + delegate.emittingLight,
                props.emittingLight.blue + delegate.emittingLight,
            )
            setTintAmount(props.tintAmount)
            setTintColour(props.tintColour)
        }
    }

    companion object {
        private val TAG = Log.Tag("ComplexMeshRendererImpl")
    }
}
