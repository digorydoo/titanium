package io.github.digorydoo.titanium.main.mesh

import ch.digorydoo.kutils.colour.Colour
import io.github.digorydoo.titanium.engine.mesh.MeshMaterial

/**
 * FIXME get rid of this class, use individual shaders instead!
 */
internal class MaterialProps(
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
) {
    companion object {
        fun get(mat: MeshMaterial): MaterialProps = when (mat) {
            MeshMaterial.DEFAULT -> greyStoneProps
            MeshMaterial.BLACK_CLOTH -> blackClothProps
            MeshMaterial.BLUE_METAL -> blueMetalProps
            MeshMaterial.CLAY -> clayProps
            MeshMaterial.GLOSSY_WHITE -> glossyWhiteProps
            MeshMaterial.GOLD -> goldProps
            MeshMaterial.GREY_STONE -> greyStoneProps
            MeshMaterial.MILITARY_DKGREEN_METAL -> militaryDkGreenMetalProps
            MeshMaterial.MILITARY_GREEN_METAL -> militaryGreenMetalProps
            MeshMaterial.RED_CLOTH -> redClothProps
            MeshMaterial.RED_METAL -> redMetalProps
            MeshMaterial.SILVER_METAL -> silverMetalProps
            MeshMaterial.WHITE_CLOTH -> whiteClothProps
            MeshMaterial.WHITE_PLASTIC -> whitePlasticProps
            MeshMaterial.WOOD -> woodProps
            MeshMaterial.WOOD_DARK -> woodDarkProps
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
            shininess = 0.0f,
            contourIntensity = 0.0f,
            contourRamp = 6.0f,
            contourTopReflectsSky = 0.0f,
            contourWidth = 0.2f,
            tintAmount = 0.0f,
            tintColour = Colour.white,
        )
        private val whitePlasticProps = MaterialProps(
            ambientLightAmount = 0.3f,
            diffuseLightAmount = 0.2f,
            shininess = 0.42f,
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
        private val woodDarkProps = MaterialProps(
            ambientLightAmount = 0.39f,
            diffuseLightAmount = 0.39f,
            contourIntensity = 0.1f,
            contourRamp = 6.0f,
            contourTopReflectsSky = 0.8f,
            contourWidth = 0.2f,
            shininess = 0.1f,
            tintAmount = 0.3f,
            tintColour = Colour(0.8f, 0.39f, 0.0f),
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
