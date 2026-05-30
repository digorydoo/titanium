package io.github.digorydoo.titanium.main.mesh

import ch.digorydoo.kutils.logging.Log
import ch.digorydoo.kutils.matrix.Matrix4f
import ch.digorydoo.kutils.matrix.MutableMatrix4f
import io.github.digorydoo.titanium.engine.core.App
import io.github.digorydoo.titanium.engine.mesh.ComplexMeshRenderer
import io.github.digorydoo.titanium.engine.mesh.MeshMaterial
import io.github.digorydoo.titanium.engine.mesh.MeshNode
import io.github.digorydoo.titanium.engine.shader.ShaderProgram
import io.github.digorydoo.titanium.engine.texture.Texture
import io.github.digorydoo.titanium.main.opengl.checkGLError
import io.github.digorydoo.titanium.main.shader.Shader
import io.github.digorydoo.titanium.main.shader.ShaderAttributes
import io.github.digorydoo.titanium.main.shader.ShaderVBO
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL15
import java.nio.FloatBuffer

class ComplexMeshRendererImpl(
    private val delegate: Delegate,
    @Suppress("unused") private val antiAliasing: Boolean, // texturing not currently implemented (see below)
    private val cullFace: Boolean,
    private val depthTest: Boolean,
): ComplexMeshRenderer() {
    private val shader = Shader(ShaderProgram.ProgramType.MESH)
    private val positionVBO = ShaderVBO()
    private val normalVBO = ShaderVBO()

    init {
        shader.create()
        positionVBO.create(ShaderVBO.Type.DYNAMIC_DRAW)
        normalVBO.create(ShaderVBO.Type.DYNAMIC_DRAW)
    }

    private var valid = true

    override fun free() {
        if (valid) {
            shader.free()
            positionVBO.free()
            normalVBO.free()
            valid = false
        }
    }

    protected fun finalize() {
        // Check that free has been called. We can't throw from finalize, so log only.
        if (valid) Log.error(TAG, "still valid at finalize")
    }

    private val rootTransform = MutableMatrix4f()
    private val rotationMatrix = MutableMatrix4f()

    override fun renderShadows() {}

    override fun renderSolid() {
        require(valid)
        val mesh = delegate.mesh ?: return

        checkGLError()
        GL11.glDisable(GL11.GL_BLEND)

        if (depthTest) {
            GL11.glEnable(GL11.GL_DEPTH_TEST)
            GL11.glDepthFunc(GL11.GL_LESS)
        } else {
            GL11.glDisable(GL11.GL_DEPTH_TEST)
        }

        if (cullFace) {
            GL11.glEnable(GL11.GL_CULL_FACE)
            GL11.glCullFace(GL11.GL_BACK) // culls the back face
            GL11.glFrontFace(GL11.GL_CCW)
        } else {
            GL11.glDisable(GL11.GL_CULL_FACE)
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
            setTranslation(delegate.renderPos)
        }

        rootTransform.setScaleTranslation(delegate.scaleFactor, delegate.renderPos) // FIXME should be done in shader

        rotationMatrix.setRotationZ(delegate.rotationPhi, clear = false)
        rootTransform.multiply(rotationMatrix)

        // FIXME inefficient: we pass down the mesh every time even though it generally won't change
        mesh.divisions.forEach { division ->
            division.nodes.forEach { render(it, division.material, rootTransform) }
        }

        GL11.glDisable(GL11.GL_CULL_FACE)
        GL11.glDisable(GL11.GL_DEPTH_TEST)
    }

    private fun render(node: MeshNode, material: MeshMaterial, parentTransform: Matrix4f) {
        val geometry = node.geometry

        // Combine the group's own transform with the parentTransform.
        node.combinedTransform.set(parentTransform)
        node.combinedTransform.multiply(node.transform ?: Matrix4f.identity)

        // Apply the projection to get the group's finalTransform.
        node.finalTransform.set(App.camera.projMatrix)
        node.finalTransform.multiply(node.combinedTransform)

        // Render children.
        node.children?.forEach { render(it, material, node.combinedTransform) }

        if (geometry != null) {
            render(geometry.positions, geometry.normals, node.tex, geometry.texCoords, material, node.finalTransform)
        }
    }

    private fun render(
        positions: FloatBuffer,
        normals: FloatBuffer,
        tex: Texture?,
        texCoords: FloatBuffer?,
        mat: MeshMaterial,
        projection: Matrix4f,
    ) {
        val numPositions = positions.limit() // limit <= capacity

        if (numPositions <= 0) {
            return
        }

        // FIXME inefficient! Pass everything down once, then do the transformations in shader!

        shader.bindVAO()

        positionVBO.bind()
        positionVBO.setData(positions)
        shader.connectToVBO(ShaderAttributes.Attribute.ModelPos)

        normalVBO.bind()
        normalVBO.setData(normals)
        shader.connectToVBO(ShaderAttributes.Attribute.Normal)

        val props = MaterialProps.fromMaterial(mat)

        // The uniforms that do not change with material are set in renderSolid.
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
            setProjection(projection)
            setRotationPhi(delegate.rotationPhi)
        }

        if (tex == null || texCoords == null) {
            // Shader does not currently implement texture mapping.
            // program.setTexIntensity(0.0f)
        } else {
            throw Exception("Shader does not currently implement texture mapping")
            // val lgt = App.scene.lighting
            // program.setTexCoordData(texCoords)
            // program.bindTexCoordData()
            // program.setTextureSamplerUnit() // our tex goes into uniform Texture
            //
            // program.setTexIntensity(lgt.texIntensity * mat.texIntensity)
            //
            // tex.apply()
            //
            // val filter = if (antiAliasing) GL_LINEAR else GL_NEAREST
            // glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, filter)
            // glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, filter)
            //
            // glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
            // glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)
            // checkGLError()
        }

        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, numPositions / 3)
        checkGLError()

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0) // unbind
    }

    override fun renderTransparent() {}

    companion object {
        private val TAG = Log.Tag("ComplexMeshRendererImpl")
    }
}
