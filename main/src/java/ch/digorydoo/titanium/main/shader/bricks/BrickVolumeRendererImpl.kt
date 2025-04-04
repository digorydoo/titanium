package ch.digorydoo.titanium.main.shader.bricks

import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.engine.brick.BrickModelData
import ch.digorydoo.titanium.engine.brick.BrickShape
import ch.digorydoo.titanium.engine.brick.BrickVolumeRenderer
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.shader.ShaderManager.ShaderFlags
import ch.digorydoo.titanium.engine.shader.ShaderProgram.ProgramType
import ch.digorydoo.titanium.engine.texture.Texture
import ch.digorydoo.titanium.main.opengl.checkGLError
import ch.digorydoo.titanium.main.shader.Shader
import ch.digorydoo.titanium.main.shader.ShaderAttributes.Attribute
import ch.digorydoo.titanium.main.shader.ShaderVBO
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL14.GL_FUNC_ADD
import org.lwjgl.opengl.GL14.glBlendEquation
import java.nio.FloatBuffer

class BrickVolumeRendererImpl(
    private val translation: Point3f,
    private val tex: Texture,
    private val modelData: BrickModelData,
): BrickVolumeRenderer {
    private class SolidData {
        private val positionVBO = ShaderVBO()
        private val normalVBO = ShaderVBO()
        private val texCoordVBO = ShaderVBO()

        fun create() {
            positionVBO.create(ShaderVBO.Type.DYNAMIC_DRAW)
            normalVBO.create(ShaderVBO.Type.DYNAMIC_DRAW)
            texCoordVBO.create(ShaderVBO.Type.DYNAMIC_DRAW)
        }

        fun free() {
            positionVBO.free()
            normalVBO.free()
            texCoordVBO.free()
        }

        fun setPositions(positions: FloatBuffer) {
            positionVBO.bind()
            positionVBO.setData(positions)
        }

        fun setNormals(normals: FloatBuffer) {
            normalVBO.bind()
            normalVBO.setData(normals)
        }

        fun setTexCoords(texCoords: FloatBuffer) {
            texCoordVBO.bind()
            texCoordVBO.setData(texCoords)
        }
    }

    private class TransparentData {
        private val positionVBO = ShaderVBO()
        private val normalVBO = ShaderVBO()

        fun create() {
            positionVBO.create(ShaderVBO.Type.DYNAMIC_DRAW)
            normalVBO.create(ShaderVBO.Type.DYNAMIC_DRAW)
        }

        fun free() {
            positionVBO.free()
            normalVBO.free()
        }

        fun setPositions(positions: FloatBuffer) {
            positionVBO.bind()
            positionVBO.setData(positions)
        }

        fun setNormals(normals: FloatBuffer) {
            normalVBO.bind()
            normalVBO.setData(normals)
        }
    }

    private val solid = SolidData().apply { create() }
    private val transparent = TransparentData().apply { create() }
    private val shaderHolders = BrickShaderHoldersMap()

    // When enabling this, disable the shadow buffer in AppImpl, disable rendering bricks normally in
    // ActiveSceneContent, and render the shadow to the screen.
    private val debuggingShadows = false

    private val shadowsShader = Shader(
        ProgramType.SHADOWS,
        if (debuggingShadows) setOf(ShaderFlags.DEBUG) else null,
    ).also {
        it.create()
        if (debuggingShadows) Log.warn(TAG, "BrickVolumeRendererImpl: debuggingShadows is enabled")
    }

    private var valid = true

    override fun free() {
        if (valid) {
            solid.free()
            transparent.free()
            shaderHolders.free()
            shadowsShader.free()
            valid = false
        }
    }

    @Suppress("removal")
    protected fun finalize() {
        // Check that free has been called. We can't throw from finalize, so log only.
        if (valid) Log.error(TAG, "still valid at finalize")
    }

    override fun prepare() {
        require(valid)

        modelData.rebuild(translation, tex.width, tex.height)

        val solidData = modelData.solidRenderData
        val transparentData = modelData.transparentRenderData

        if (solidData.positions.limit() > 0) {
            solid.setPositions(solidData.positions)
            connectVAOToVBO(Attribute.ModelPos, solid = true)
            shadowsShader.connectToVBO(Attribute.ModelPos)

            solid.setNormals(solidData.normals)
            connectVAOToVBO(Attribute.Normal, solid = true)

            if (debuggingShadows) {
                shadowsShader.connectToVBO(Attribute.Normal)
            }

            solid.setTexCoords(solidData.texCoords)
            connectVAOToVBO(Attribute.TexCoord, solid = true)
        }

        if (transparentData.positions.limit() > 0) {
            transparent.setPositions(transparentData.positions)
            connectVAOToVBO(Attribute.ModelPos, solid = false)

            transparent.setNormals(transparentData.normals)
            connectVAOToVBO(Attribute.Normal, solid = false)
        }
    }

    private fun connectVAOToVBO(attr: Attribute, solid: Boolean) {
        shaderHolders.forEach(solid) { it.connectToVBO(attr) }
    }

    override fun renderShadows() {
        require(valid)

        val renderData = modelData.solidRenderData
        val numPositions = renderData.positions.limit() // limit <= capacity

        if (numPositions <= 0) {
            return // model is empty
        }

        val distanceFromCamera = modelData.distanceFromCamera()

        if (distanceFromCamera > SHADOW_MAX_RENDER_DISTANCE) {
            return
        }

        glEnable(GL_DEPTH_TEST)
        glDepthFunc(GL_LESS)
        glFrontFace(GL_CCW) // FIXME should be default
        glDisable(GL_BLEND) // FIXME should be default
        glEnable(GL_CULL_FACE)
        glCullFace(GL_BACK) // culls the back face

        shadowsShader.program.use()
        shadowsShader.bindVAO()
        shadowsShader.uniforms.setShadowProjection()

        if (debuggingShadows) {
            shadowsShader.uniforms.setSunDir(App.scene.lighting.sunDir.vector)
        }

        modelData.forEachMaterial { material, forEachShape ->
            if (material.solid) {
                forEachShape { shape, triangles ->
                    if (distanceFromCamera < maxShadowDistance(shape)) {
                        glDrawArrays(GL_TRIANGLES, triangles.skip, triangles.count)
                        checkGLError()
                    }
                }
            }
        }

        // glDepthMask(true)
        // glDisable(GL_BLEND)
        glDisable(GL_CULL_FACE) // FIXME should stay enabled
        // glBindBuffer(GL_ARRAY_BUFFER, 0) // unbind
    }

    override fun renderSolid() {
        require(valid)

        val renderData = modelData.solidRenderData
        val numPositions = renderData.positions.limit() // limit <= capacity

        if (numPositions <= 0) {
            return // model is empty
        }

        val distanceFromCamera = modelData.distanceFromCamera()

        if (distanceFromCamera > MAX_RENDER_DISTANCE) {
            return
        }

        glEnable(GL_DEPTH_TEST)
        glDepthFunc(GL_LESS)
        glFrontFace(GL_CCW) // FIXME should be default
        glDisable(GL_BLEND) // FIXME should be default
        glEnable(GL_CULL_FACE) // FIXME should be default
        glCullFace(GL_BACK) // FIXME should be default

        val lgt = App.scene.lighting

        modelData.forEachMaterial { material, forEachShape ->
            if (material.solid) {
                val holder = shaderHolders[material]

                if (holder.prepareSolid(distanceFromCamera, lgt, tex, material)) {
                    forEachShape { shape, triangles ->
                        if (distanceFromCamera < maxRenderDistance(shape)) {
                            glDrawArrays(GL_TRIANGLES, triangles.skip, triangles.count)
                            checkGLError()
                        }
                    }
                }
            }
        }

        // glDepthMask(true)
        // glDisable(GL_BLEND)
        glDisable(GL_CULL_FACE) // FIXME should stay enabled
        // glBindBuffer(GL_ARRAY_BUFFER, 0) // unbind
    }

    override fun renderTransparent() {
        require(valid)

        val renderData = modelData.transparentRenderData
        val numPositions = renderData.positions.limit() // limit <= capacity

        if (numPositions <= 0) {
            return // model is empty
        }

        val distanceFromCamera = modelData.distanceFromCamera()

        if (distanceFromCamera > TRANSPARENT_MAX_RENDER_DISTANCE) {
            return
        }

        glEnable(GL_DEPTH_TEST)
        glDepthFunc(GL_LESS)
        glFrontFace(GL_CCW) // FIXME should be default
        glDepthMask(false)
        glEnable(GL_BLEND)
        glBlendEquation(GL_FUNC_ADD)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_CULL_FACE) // FIXME should be default
        glCullFace(GL_BACK) // FIXME should be default

        val lgt = App.scene.lighting

        modelData.forEachMaterial { material, forEachShape ->
            if (!material.solid) {
                val holder = shaderHolders[material]

                if (holder.prepareTransparent(distanceFromCamera, lgt, tex, material)) {
                    forEachShape { shape, triangles ->
                        if (distanceFromCamera < maxRenderDistance(shape)) {
                            glDrawArrays(GL_TRIANGLES, triangles.skip, triangles.count)
                            checkGLError()
                        }
                    }
                }
            }
        }

        glDepthMask(true)
        glDisable(GL_BLEND)
        glDisable(GL_CULL_FACE) // FIXME should stay enabled
        // glBindBuffer(GL_ARRAY_BUFFER, 0) // unbind
    }

    private fun maxRenderDistance(shape: BrickShape) =
        when {
            App.camera.isInTopDownMode -> Float.POSITIVE_INFINITY
            else -> shape.relVolume.let {
                when {
                    it <= 0.1f -> 42.0f // e.g. UPRIGHT_BAR_NW
                    it <= 0.2f -> 100.0f // e.g. THIN_WALL_NORTH
                    it <= 0.5f -> 300.0f // e.g. RAMP_NORTH
                    else -> MAX_RENDER_DISTANCE
                }
            }
        }

    private fun maxShadowDistance(shape: BrickShape) =
        when (shape) {
            BrickShape.UPRIGHT_BAR_NW,
            BrickShape.UPRIGHT_BAR_NE,
            BrickShape.UPRIGHT_BAR_SE,
            BrickShape.UPRIGHT_BAR_SW,
            BrickShape.UPRIGHT_DBL_BAR_NORTH,
            BrickShape.UPRIGHT_DBL_BAR_EAST,
            BrickShape.UPRIGHT_DBL_BAR_SOUTH,
            BrickShape.UPRIGHT_DBL_BAR_WEST,
            BrickShape.BAR_FRAME_NORTH,
            BrickShape.BAR_FRAME_EAST,
            BrickShape.BAR_FRAME_SOUTH,
            BrickShape.BAR_FRAME_WEST,
            BrickShape.HIGH_BAR_NORTH,
            BrickShape.HIGH_BAR_EAST,
            BrickShape.HIGH_BAR_SOUTH,
            BrickShape.HIGH_BAR_WEST,
            BrickShape.WINDOW_TOP_L_NORTH,
            BrickShape.WINDOW_TOP_R_NORTH,
            BrickShape.WINDOW_TOP_L_EAST,
            BrickShape.WINDOW_TOP_R_EAST,
            BrickShape.WINDOW_TOP_L_SOUTH,
            BrickShape.WINDOW_TOP_R_SOUTH,
            BrickShape.WINDOW_TOP_L_WEST,
            BrickShape.WINDOW_TOP_R_WEST,
                -> -1.0f // no shadow

            else -> maxRenderDistance(shape)
        }

    companion object {
        private val TAG = Log.Tag("BrickVolumeRendererImpl")
        private const val SHADOW_MAX_RENDER_DISTANCE = 200.0f
        private const val TRANSPARENT_MAX_RENDER_DISTANCE = 64.0f // metres
        const val MAX_RENDER_DISTANCE = 350.0f // [metres] nothing gets rendered beyond this except in top-down mode
    }
}
