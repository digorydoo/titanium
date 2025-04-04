package ch.digorydoo.titanium.main.shader

import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.shader.ShaderProgram.ProgramType
import ch.digorydoo.titanium.engine.sky.SkydomeRenderer
import ch.digorydoo.titanium.main.opengl.checkGLError
import ch.digorydoo.titanium.main.shader.ShaderAttributes.Attribute
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER
import org.lwjgl.opengl.GL15.glBindBuffer

class SkydomeRendererImpl(private val delegate: Delegate): SkydomeRenderer() {
    private val shader = Shader(ProgramType.SKYDOME)
    private val positionVBO = ShaderVBO()

    init {
        shader.create()
        positionVBO.create(ShaderVBO.Type.DYNAMIC_DRAW)
        shader.uniforms.setScaleFactor(delegate.scaleFactor)
        positionVBO.bind()
        positionVBO.setData(delegate.positions)
        shader.bindVAO()
        shader.connectToVBO(Attribute.ModelPos)
    }

    private var valid = true

    override fun free() {
        if (valid) {
            shader.free()
            positionVBO.free()
            valid = false
        }
    }

    @Suppress("removal")
    protected fun finalize() {
        // Check that free has been called. We can't throw from finalize, so log only.
        if (valid) Log.error(TAG, "still valid at finalize")
    }

    override fun renderShadows() {}

    override fun renderSolid() {
        require(valid)

        val numPositions = delegate.positions.limit() // limit <= capacity

        if (numPositions <= 0) {
            return // model is empty
        }

        checkGLError()
        glDisable(GL_BLEND)

        glEnable(GL_DEPTH_TEST)
        glDepthFunc(GL_LEQUAL)
        // glDisable(GL_DEPTH_TEST)

        glEnable(GL_CULL_FACE)
        glCullFace(GL_FRONT) // we cull the front face, because we're always inside the box
        glFrontFace(GL_CCW)

        shader.program.use()
        shader.bindVAO()

        val lgt = App.scene.lighting

        shader.uniforms.apply {
            setProjection()
            setSunDir(lgt.sunDir.vector)
            setSkyColour1(lgt.skyColour1)
            setSkyColour2(lgt.skyColour2)
            setBrightness(lgt.brightness)
            setContrast(lgt.contrast)
            setTranslation(App.camera.sourcePos)
        }

        glDrawArrays(GL_TRIANGLES, 0, numPositions / 3)
        checkGLError()

        glDisable(GL_CULL_FACE) // FIXME should be default
        glBindBuffer(GL_ARRAY_BUFFER, 0) // unbind
    }

    override fun renderTransparent() {}

    companion object {
        private val TAG = Log.Tag("SkydomeRendererImpl")
    }
}
