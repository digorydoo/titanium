package io.github.digorydoo.titanium.main.shader

import ch.digorydoo.kutils.colour.Colour
import ch.digorydoo.kutils.matrix.Matrix4f
import ch.digorydoo.kutils.vector.Vector3f
import io.github.digorydoo.titanium.engine.core.App
import io.github.digorydoo.titanium.main.opengl.checkGLError
import io.github.digorydoo.titanium.main.shader.ShaderUniforms.Uniform.*
import io.github.digorydoo.titanium.main.texture.TextureManagerImpl.SamplerUnit
import org.lwjgl.opengl.GL20.*

class ShaderUniforms {
    private enum class Uniform {
        AmbientLightAmount,     // uniform float
        AmbientLightColour,     // uniform vec3
        AspectRatio,            // uniform float
        Brightness,             // uniform float
        CameraDir,              // uniform vec3
        CameraSourcePos,        // uniform vec3
        ContourIntensity,       // uniform float
        ContourRamp,            // uniform float
        ContourTopReflectsSky,  // uniform float
        ContourWidth,           // uniform float
        Contrast,               // uniform float
        ColourWithAlpha,        // uniform vec4
        DiffuseLightAmount,     // uniform float
        DiffuseLightColour,     // uniform vec3
        EmittingLight,          // uniform vec3
        Haziness,               // uniform float
        HazyColour,             // uniform vec3
        Lamp0Colour,            // uniform vec3
        Lamp0Intensity,         // uniform float
        Lamp0Pos,               // uniform vec3
        Lamp0Radius,            // uniform float
        Lamp1Colour,            // uniform vec3
        Lamp1Intensity,         // uniform float
        Lamp1Pos,               // uniform vec3
        Lamp1Radius,            // uniform float
        Lamp2Colour,            // uniform vec3
        Lamp2Intensity,         // uniform float
        Lamp2Pos,               // uniform vec3
        Lamp2Radius,            // uniform float
        Lamp3Colour,            // uniform vec3
        Lamp3Intensity,         // uniform float
        Lamp3Pos,               // uniform vec3
        Lamp3Radius,            // uniform float
        Lamp4Colour,            // uniform vec3
        Lamp4Intensity,         // uniform float
        Lamp4Pos,               // uniform vec3
        Lamp4Radius,            // uniform float
        MultColour,             // uniform vec3
        Opacity,                // uniform float
        PenSize,                // uniform float
        Progress,               // uniform float
        Projection,             // uniform mat4
        RotOrigin,              // uniform vec3
        RotationPhi,            // uniform float
        RotationRho,            // uniform float
        ScaleFactor,            // uniform vec3
        SessionTime,            // uniform float
        ShadowMap,              // uniform sampler2DShadow
        ShadowProjection,       // uniform mat4
        Shininess,              // uniform float
        SkyColour1,             // uniform vec3
        SkyColour2,             // uniform vec3
        SunDir,                 // uniform vec3
        TexIntensity,           // uniform float
        Texture,                // uniform sampler2D
        TintAmount,             // uniform float
        TintColour,             // uniform vec3
        Translation,            // uniform vec3
        Transform,              // uniform mat4
    }

    private val locations = mutableMapOf<Uniform, Int>()

    fun findLocations(programId: Int) {
        Uniform.entries.forEach { u ->
            locations[u] = glGetUniformLocation(programId, u.name)
                .takeIf { glGetError() == GL_NO_ERROR }
                ?: -1
        }
    }

    fun setAmbientLightAmount(amount: Float) = set(AmbientLightAmount, amount)
    fun setAmbientLightColour(c: Colour) = setRGB(AmbientLightColour, c)
    fun setAspectRatio(ratio: Float) = set(AspectRatio, ratio)
    fun setBrightness(brite: Float) = set(Brightness, brite)
    fun setCameraDir() = set(CameraDir, App.camera.currentDir)
    fun setCameraSourcePos() = set(CameraSourcePos, App.camera.sourcePos)
    fun setColourWithAlpha(c: Colour) = setRGBA(ColourWithAlpha, c)
    fun setContourIntensity(f: Float) = set(ContourIntensity, f)
    fun setContourRamp(f: Float) = set(ContourRamp, f)
    fun setContourTopReflectsSky(f: Float) = set(ContourTopReflectsSky, f)
    fun setContourWidth(f: Float) = set(ContourWidth, f)
    fun setContrast(c: Float) = set(Contrast, c)
    fun setDiffuseLightAmount(amount: Float) = set(DiffuseLightAmount, amount)
    fun setDiffuseLightColour(c: Colour) = setRGB(DiffuseLightColour, c)
    fun setEmittingLight(r: Float, g: Float, b: Float) = set(EmittingLight, r, g, b)
    fun setHaziness(hazy: Float) = set(Haziness, hazy)
    fun setHazyColour(c: Colour) = setRGB(HazyColour, c)
    fun setMultColour(c: Colour) = setRGB(MultColour, c)
    fun setOpacity(opacity: Float) = set(Opacity, opacity)
    fun setPenSize(f: Float) = set(PenSize, f)
    fun setProgress(f: Float) = set(Progress, f)
    fun setProjection() = setMat4(Projection, App.camera.projMatrix)
    fun setProjection(matrix: Matrix4f) = setMat4(Projection, matrix)
    fun setRotOrigin(x: Float, y: Float, z: Float) = set(RotOrigin, x, y, z)
    fun setRotationPhi(radians: Float) = set(RotationPhi, radians)
    fun setRotationRho(radians: Float) = set(RotationRho, radians)
    fun setScaleFactor(f: Float) = set(ScaleFactor, f, f, f)
    fun setSessionTime() = set(SessionTime, App.time.sessionTime)
    fun setShadowMapSamplerUnit() = set(ShadowMap, SamplerUnit.SHADOW_MAP.index)
    fun setShadowProjection() = setMat4(ShadowProjection, App.shadowBuffer.projMatrix)
    fun setShininess(f: Float) = set(Shininess, f)
    fun setSkyColour1(c: Colour) = setRGB(SkyColour1, c)
    fun setSkyColour2(c: Colour) = setRGB(SkyColour2, c)
    fun setSunDir(vec: Vector3f) = set(SunDir, vec)
    fun setTexIntensity(f: Float) = set(TexIntensity, f)
    fun setTextureSamplerUnit() = set(Texture, SamplerUnit.TEXTURE.index)
    fun setTintAmount(amount: Float) = set(TintAmount, amount)
    fun setTintColour(c: Colour) = setRGB(TintColour, c)
    fun setTransform(mat: Matrix4f) = setMat4(Transform, mat)
    fun setTranslation(tr: Vector3f) = set(Translation, tr.x, tr.y, tr.z)

    fun setLamp0Props() {
        val lamp0 = App.lamps.getOrNull(0)

        if (lamp0 == null) {
            setLamp0Intensity(0.0f)
        } else {
            setLamp0Intensity(lamp0.intensity)
            setLamp0Pos(lamp0.pos)
            setLamp0Colour(lamp0.colour)
            setLamp0Radius(lamp0.radius)
        }
    }

    fun setLamp1Props() {
        val lamp1 = App.lamps.getOrNull(1)

        if (lamp1 == null) {
            setLamp1Intensity(0.0f)
        } else {
            setLamp1Intensity(lamp1.intensity)
            setLamp1Pos(lamp1.pos)
            setLamp1Colour(lamp1.colour)
            setLamp1Radius(lamp1.radius)
        }
    }

    fun setLamp2Props() {
        val lamp2 = App.lamps.getOrNull(2)

        if (lamp2 == null) {
            setLamp2Intensity(0.0f)
        } else {
            setLamp2Intensity(lamp2.intensity)
            setLamp2Pos(lamp2.pos)
            setLamp2Colour(lamp2.colour)
            setLamp2Radius(lamp2.radius)
        }
    }

    fun setLamp3Props() {
        val lamp3 = App.lamps.getOrNull(3)

        if (lamp3 == null) {
            setLamp3Intensity(0.0f)
        } else {
            setLamp3Intensity(lamp3.intensity)
            setLamp3Pos(lamp3.pos)
            setLamp3Colour(lamp3.colour)
            setLamp3Radius(lamp3.radius)
        }
    }

    fun setLamp4Props() {
        val lamp4 = App.lamps.getOrNull(4)

        if (lamp4 == null) {
            setLamp4Intensity(0.0f)
        } else {
            setLamp4Intensity(lamp4.intensity)
            setLamp4Pos(lamp4.pos)
            setLamp4Colour(lamp4.colour)
            setLamp4Radius(lamp4.radius)
        }
    }

    private fun setLamp0Intensity(f: Float) = set(Lamp0Intensity, f)
    private fun setLamp0Pos(pos: Vector3f) = set(Lamp0Pos, pos)
    private fun setLamp0Colour(c: Colour) = setRGB(Lamp0Colour, c)
    private fun setLamp0Radius(r: Float) = set(Lamp0Radius, r)
    private fun setLamp1Intensity(f: Float) = set(Lamp1Intensity, f)
    private fun setLamp1Pos(pos: Vector3f) = set(Lamp1Pos, pos)
    private fun setLamp1Colour(c: Colour) = setRGB(Lamp1Colour, c)
    private fun setLamp1Radius(r: Float) = set(Lamp1Radius, r)
    private fun setLamp2Intensity(f: Float) = set(Lamp2Intensity, f)
    private fun setLamp2Pos(pos: Vector3f) = set(Lamp2Pos, pos)
    private fun setLamp2Colour(c: Colour) = setRGB(Lamp2Colour, c)
    private fun setLamp2Radius(r: Float) = set(Lamp2Radius, r)
    private fun setLamp3Intensity(f: Float) = set(Lamp3Intensity, f)
    private fun setLamp3Pos(pos: Vector3f) = set(Lamp3Pos, pos)
    private fun setLamp3Colour(c: Colour) = setRGB(Lamp3Colour, c)
    private fun setLamp3Radius(r: Float) = set(Lamp3Radius, r)
    private fun setLamp4Intensity(f: Float) = set(Lamp4Intensity, f)
    private fun setLamp4Pos(pos: Vector3f) = set(Lamp4Pos, pos)
    private fun setLamp4Colour(c: Colour) = setRGB(Lamp4Colour, c)
    private fun setLamp4Radius(r: Float) = set(Lamp4Radius, r)

    private fun set(u: Uniform, i: Int) {
        val loc = locations[u] ?: -1
        require(loc >= 0) { "Uniform location unknown: $u" }
        glUniform1i(loc, i)
        checkGLError { "Failed to set value for uniform $u" }
    }

    private fun set(u: Uniform, f: Float) {
        val loc = locations[u] ?: -1
        require(loc >= 0) { "Uniform location unknown: $u" }
        glUniform1f(loc, f)
        checkGLError { "Failed to set value for uniform $u" }
    }

    private fun set(u: Uniform, x: Float, y: Float, z: Float) {
        val loc = locations[u] ?: -1
        require(loc >= 0) { "Uniform location unknown: $u" }
        glUniform3f(loc, x, y, z)
        checkGLError { "Failed to set value for uniform $u" }
    }

    private fun set(u: Uniform, p: Vector3f) {
        val loc = locations[u] ?: -1
        require(loc >= 0) { "Uniform location unknown: $u" }
        glUniform3f(loc, p.x, p.y, p.z)
        checkGLError { "Failed to set value for uniform $u" }
    }

    private fun setRGB(u: Uniform, c: Colour) {
        val loc = locations[u] ?: -1
        require(loc >= 0) { "Uniform location unknown: $u" }
        glUniform3f(loc, c.red, c.green, c.blue)
        checkGLError { "Failed to set value for uniform $u" }
    }

    private fun setRGBA(u: Uniform, c: Colour) {
        val loc = locations[u] ?: -1
        require(loc >= 0) { "Uniform location unknown: $u" }
        glUniform4f(loc, c.red, c.green, c.blue, c.alpha)
        checkGLError { "Failed to set value for uniform $u" }
    }

    private fun setMat4(u: Uniform, m: Matrix4f) {
        val loc = locations[u] ?: -1
        require(loc >= 0) { "Uniform location unknown: $u" }
        m.buffer.position(0)
        glUniformMatrix4fv(loc, false, m.buffer)
    }

    override fun toString() =
        locations.map { (key, loc) -> "   $key: $loc" }
            .joinToString("\n")
            .let { "ShaderUniforms(\n$it\n)" }
}
