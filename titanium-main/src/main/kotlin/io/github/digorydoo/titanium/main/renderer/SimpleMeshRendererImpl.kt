package io.github.digorydoo.titanium.main.renderer

import ch.digorydoo.kutils.logging.Log
import ch.digorydoo.kutils.matrix.Matrix4f
import io.github.digorydoo.titanium.engine.core.App
import io.github.digorydoo.titanium.engine.mesh.SimpleMeshRenderer
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

class SimpleMeshRendererImpl(
    private val delegate: Delegate,
    @Suppress("unused") private val antiAliasing: Boolean, // texturing not currently implemented (see below)
    private val cullFace: Boolean,
    private val depthTest: Boolean,
): SimpleMeshRenderer() {
    private val shader = Shader(ProgramType.MESH)
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

    override fun renderShadows() {}

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
        }

        val positions = mesh.positions
        val numPositions = positions.limit() // limit <= capacity

        if (numPositions <= 0) {
            return
        }

        // FIXME inefficient: we pass down the mesh every time even though it generally won't change
        // FIXME inefficient! Pass everything down once, then do the transformations in shader!

        val normals = mesh.normals
        val mat = mesh.material

        shader.bindVAO()
        positionVBO.bind()
        positionVBO.setData(positions)
        shader.connectToVBO(Attribute.ModelPos)

        normalVBO.bind()
        normalVBO.setData(normals)
        shader.connectToVBO(Attribute.Normal)

        val props = MaterialProps.get(mat)

        shader.uniforms.apply {
            setAmbientLightAmount(props.ambientLightAmount)
            setDiffuseLightAmount(props.diffuseLightAmount)
            setShininess(props.shininess)
            setContourIntensity(props.contourIntensity)
            setContourRamp(props.contourRamp)
            setContourWidth(props.contourWidth)
            setContourTopReflectsSky(props.contourTopReflectsSky)
            setEmittingLight(props.emittingLight.red, props.emittingLight.green, props.emittingLight.blue)
            setTintAmount(props.tintAmount)
            setTintColour(props.tintColour)
            setTransform(Matrix4f.identity) // necessary, because we share the shader with ComplexMeshRendererImpl
            setRotationPhi(delegate.rotationPhi)
        }

        glDrawArrays(GL_TRIANGLES, 0, numPositions / 3)
        checkGLError()

        glBindBuffer(GL_ARRAY_BUFFER, 0) // unbind
        glDisable(GL_CULL_FACE)
        glDisable(GL_DEPTH_TEST)
    }

    override fun renderTransparent() {}

    companion object {
        private val TAG = Log.Tag("SimpleMeshRendererImpl")
    }
}
