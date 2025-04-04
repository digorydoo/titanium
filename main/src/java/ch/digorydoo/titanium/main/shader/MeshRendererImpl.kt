package ch.digorydoo.titanium.main.shader

import ch.digorydoo.kutils.colour.Colour
import ch.digorydoo.kutils.matrix.Matrix4f
import ch.digorydoo.kutils.matrix.MutableMatrix4f
import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.mesh.MeshMaterial
import ch.digorydoo.titanium.engine.mesh.MeshNode
import ch.digorydoo.titanium.engine.mesh.MeshRenderer
import ch.digorydoo.titanium.engine.shader.ShaderProgram.ProgramType
import ch.digorydoo.titanium.engine.texture.Texture
import ch.digorydoo.titanium.main.opengl.checkGLError
import ch.digorydoo.titanium.main.shader.ShaderAttributes.Attribute
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER
import org.lwjgl.opengl.GL15.glBindBuffer
import java.nio.FloatBuffer

class MeshRendererImpl(
    private val delegate: Delegate,
    @Suppress("unused") private val antiAliasing: Boolean, // texturing not currently implemented (see below)
    private val cullFace: Boolean,
    private val depthTest: Boolean,
): MeshRenderer() {
    // FIXME get rid of this class, use individual shaders instead!
    private class MaterialProps(
        val ambientLightAmount: Float = 0.0f,     // 0..1
        val contourIntensity: Float = 0.0f,       // 0..1
        val contourRamp: Float = 1.0f,            // must be >= 1; higher values lead to sharper contour
        val contourTopReflectsSky: Float = 0.0f,  // 0..1; for metallic surfaces
        val contourWidth: Float = 0.0f,           // 0 <= x < 1; 0=only linear ramp will be visible
        val diffuseLightAmount: Float = 0.0f,     // 0..1; directional sunlight
        val emittingLight: Colour = Colour.black, // colour with pre-multiplied amount; black=no light emitted
        val shininess: Float = 0.0f,              // 0..1; wet or metallic surfaces reflecting light
        val tintAmount: Float = 0.0f,             // 0..1
        val tintColour: Colour = Colour.grey300,  // amount is NOT pre-multiplied
    )

    private val shader = Shader(ProgramType.MESH)
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

    @Suppress("removal")
    protected fun finalize() {
        // Check that free has been called. We can't throw from finalize, so log only.
        if (valid) Log.error(TAG, "still valid at finalize")
    }

    private val rootTransform = MutableMatrix4f()
    private val rotationMatrix = MutableMatrix4f()

    override fun renderShadows() {}

    override fun renderSolid() {
        require(valid)

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
            setTranslation(delegate.renderPos)
        }

        rootTransform.setScaleTranslation(delegate.scaleFactor, delegate.renderPos) // FIXME should be done in shader

        rotationMatrix.setRotationZ(delegate.rotationPhi, clear = false)
        rootTransform.multiply(rotationMatrix)

        delegate.mesh.divisions.forEach { division ->
            division.nodes.forEach { render(it, division.material, rootTransform) }
        }

        glDisable(GL_CULL_FACE)
        glDisable(GL_DEPTH_TEST)
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
        shader.connectToVBO(Attribute.ModelPos)

        normalVBO.bind()
        normalVBO.setData(normals)
        shader.connectToVBO(Attribute.Normal)

        val props = mat.props()

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

        glDrawArrays(GL_TRIANGLES, 0, numPositions / 3)
        checkGLError()

        glBindBuffer(GL_ARRAY_BUFFER, 0) // unbind
    }

    override fun renderTransparent() {}

    companion object {
        private val TAG = Log.Tag("MeshRendererImpl")

        private fun MeshMaterial.props(): MaterialProps = when (this) {
            MeshMaterial.DEFAULT -> greyStoneProps
            MeshMaterial.RED_CLOTH -> redClothProps
            MeshMaterial.GREY_STONE -> greyStoneProps
            MeshMaterial.WOOD -> woodProps
            MeshMaterial.SILVER_METAL -> silverMetalProps
            MeshMaterial.RED_METAL -> redMetalProps
            MeshMaterial.GOLD -> goldProps
            MeshMaterial.BLACK_CLOTH -> blackClothProps
            MeshMaterial.WHITE_CLOTH -> whiteClothProps
            MeshMaterial.BLUE_METAL -> blueMetalProps
            MeshMaterial.GLOSSY_WHITE -> glossyWhiteProps
            MeshMaterial.MILITARY_DKGREEN_METAL -> militaryDkGreenMetalProps
            MeshMaterial.MILITARY_GREEN_METAL -> militaryGreenMetalProps
            MeshMaterial.CLAY -> clayProps
        }

        private val blackClothProps = MaterialProps(
            ambientLightAmount = 0.0f,
            diffuseLightAmount = 0.2f,
            shininess = 0.0f,
            contourIntensity = 0.5f,
            contourRamp = 6.0f,
            contourTopReflectsSky = 0.2f,
            contourWidth = 0.2f,
            tintAmount = 0.0f,
            tintColour = Colour.black,
        )
        private val clayProps = MaterialProps(
            ambientLightAmount = 0.64f,
            diffuseLightAmount = 0.42f,
            contourIntensity = 0.05f,
            contourRamp = 6.0f,
            contourTopReflectsSky = 0.0f,
            contourWidth = 0.2f,
            tintAmount = 0.5f,
            tintColour = Colour(1.0f, 0.5f, 0.2f),
        )
        private val goldProps = MaterialProps(
            ambientLightAmount = 0.42f,
            diffuseLightAmount = 0.4f,
            contourIntensity = 0.8f,
            contourRamp = 2.0f,
            contourTopReflectsSky = 1.0f,
            contourWidth = 0.2f,
            shininess = 0.8f,
            tintAmount = 0.6f,
            tintColour = Colour(1.0f, 0.9f, 0.0f),
        )
        private val greyStoneProps = MaterialProps(
            ambientLightAmount = 0.5f,
            diffuseLightAmount = 0.74f,
            contourIntensity = 0.15f,
            contourRamp = 6.0f,
            contourTopReflectsSky = 0.2f,
            contourWidth = 0.2f,
            tintAmount = 0.04f,
            tintColour = Colour(1.0f, 0.8f, 0.0f),
        )
        private val redClothProps = MaterialProps(
            ambientLightAmount = 0.6f,
            diffuseLightAmount = 0.5f,
            contourIntensity = 0.15f,
            contourRamp = 6.0f,
            contourTopReflectsSky = 0.2f,
            contourWidth = 0.2f,
            tintAmount = 0.64f,
            tintColour = Colour(1.0f, 0.1f, 0.0f),
        )
        private val redMetalProps = MaterialProps(
            ambientLightAmount = 0.42f,
            diffuseLightAmount = 0.4f,
            contourIntensity = 0.8f,
            contourRamp = 2.0f,
            contourTopReflectsSky = 1.0f,
            contourWidth = 0.2f,
            shininess = 0.6f,
            tintAmount = 0.6f,
            tintColour = Colour(1.0f, 0.2f, 0.1f),
        )
        private val silverMetalProps = MaterialProps(
            ambientLightAmount = 0.5f,
            diffuseLightAmount = 0.0f,
            contourIntensity = 0.25f,
            contourRamp = 2.0f,
            contourTopReflectsSky = 0.6f,
            contourWidth = 0.2f,
            shininess = 0.7f,
        )
        private val whiteClothProps = MaterialProps(
            ambientLightAmount = 0.3f,
            diffuseLightAmount = 0.2f,
            emittingLight = Colour.grey900,
            shininess = 0.0f,
            contourIntensity = 0.0f,
            contourRamp = 6.0f,
            contourTopReflectsSky = 0.0f,
            contourWidth = 0.2f,
            tintAmount = 0.0f,
            tintColour = Colour.white,
        )
        private val woodProps = MaterialProps(
            ambientLightAmount = 0.42f,
            diffuseLightAmount = 0.42f,
            contourIntensity = 0.1f,
            contourRamp = 6.0f,
            contourTopReflectsSky = 0.8f,
            contourWidth = 0.2f,
            shininess = 0.1f,
            tintAmount = 0.3f,
            tintColour = Colour(1.0f, 0.42f, 0.0f),
        )
        private val glossyWhiteProps = MaterialProps(
            ambientLightAmount = 0.5f,
            diffuseLightAmount = 0.2f,
            shininess = 0.7f,
            contourIntensity = 0.0f,
            contourRamp = 6.0f,
            contourTopReflectsSky = 0.0f,
            contourWidth = 0.2f,
            tintAmount = 0.0f,
            tintColour = Colour.white,
        )
        private val blueMetalProps = MaterialProps(
            ambientLightAmount = 0.46f,
            diffuseLightAmount = 0.4f,
            contourIntensity = 0.8f,
            contourRamp = 2.0f,
            contourTopReflectsSky = 0.8f,
            contourWidth = 0.2f,
            shininess = 0.6f,
            tintAmount = 0.6f,
            tintColour = Colour(0.1f, 0.2f, 0.5f),
        )
        private val militaryGreenMetalProps = MaterialProps(
            ambientLightAmount = 0.46f,
            diffuseLightAmount = 0.4f,
            contourIntensity = 0.8f,
            contourRamp = 2.0f,
            contourTopReflectsSky = 1.0f,
            contourWidth = 0.2f,
            shininess = 0.6f,
            tintAmount = 0.6f,
            tintColour = Colour(0.11f, 0.26f, 0.15f),
        )
        private val militaryDkGreenMetalProps = MaterialProps(
            ambientLightAmount = 0.42f,
            diffuseLightAmount = 0.4f,
            contourIntensity = 0.8f,
            contourRamp = 2.0f,
            contourTopReflectsSky = 1.0f,
            contourWidth = 0.2f,
            shininess = 0.6f,
            tintAmount = 0.6f,
            tintColour = Colour(0.05f, 0.13f, 0.07f),
        )
    }
}
