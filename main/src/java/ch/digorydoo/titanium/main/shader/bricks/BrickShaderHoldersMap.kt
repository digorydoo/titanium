package ch.digorydoo.titanium.main.shader.bricks

import ch.digorydoo.titanium.engine.brick.BrickMaterial
import ch.digorydoo.titanium.engine.brick.BrickMaterial.*
import ch.digorydoo.titanium.engine.scene.Lighting
import ch.digorydoo.titanium.engine.texture.Texture
import ch.digorydoo.titanium.main.shader.ShaderAttributes.Attribute

class BrickShaderHoldersMap {
    interface ShaderHolder {
        val intendedForSolid: Boolean
        fun create()
        fun free()
        fun connectToVBO(attr: Attribute)
        fun prepareSolid(distance: Float, lgt: Lighting, tex: Texture, material: BrickMaterial): Boolean
        fun prepareTransparent(distance: Float, lgt: Lighting, tex: Texture, material: BrickMaterial): Boolean
    }

    private val concreteShaderHolder = ConcreteShaderHolder().apply { create() }
    private val glassShaderHolder = GlassShaderHolder().apply { create() }
    private val metalShaderHolder = MetalShaderHolder().apply { create() }
    private val waterShaderHolder = WaterShaderHolder().apply { create() }
    private val windowInteriorShaderHolder = WindowInteriorShaderHolder().apply { create() }
    private val otherMaterialsShaderHolder = OtherMaterialsShaderHolder().apply { create() }

    fun free() {
        concreteShaderHolder.free()
        glassShaderHolder.free()
        metalShaderHolder.free()
        waterShaderHolder.free()
        windowInteriorShaderHolder.free()
        otherMaterialsShaderHolder.free()
    }

    private val map = mapOf(
        ASPHALT_RED to concreteShaderHolder,
        CONCRETE_CELLAR_WINDOW to concreteShaderHolder,
        CONCRETE_FAKE_DOOR to concreteShaderHolder,
        CONCRETE_GRAFITTI to concreteShaderHolder,
        CONCRETE_LARGE_VENTILATION to concreteShaderHolder,
        CONCRETE_SMALL_VENTILATION to concreteShaderHolder,
        CONCRETE_SQUARE_WINDOW to concreteShaderHolder,
        CONCRETE_TALL_WINDOW to concreteShaderHolder,
        GREEN_CONCRETE to concreteShaderHolder,
        GREY_CONCRETE to concreteShaderHolder,
        METAL_RED to metalShaderHolder,
        ORANGE_CONCRETE to concreteShaderHolder,
        STONE_WALL_YELLOW to concreteShaderHolder,
        WHITE_CONCRETE to concreteShaderHolder,
        WINDOW_INTERIOR to windowInteriorShaderHolder,
        GLASS to glassShaderHolder,
        STANDING_WATER to waterShaderHolder,
        null to otherMaterialsShaderHolder
    )

    operator fun get(material: BrickMaterial) =
        map[material] ?: otherMaterialsShaderHolder

    fun forEach(solid: Boolean, lambda: (holder: ShaderHolder) -> Unit) =
        map.forEach { (_, holder) ->
            if (holder.intendedForSolid == solid) {
                lambda(holder)
            }
        }
}
