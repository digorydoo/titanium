package io.github.digorydoo.titanium.engine.material

import io.github.digorydoo.titanium.engine.brick.BrickMaterial
import kotlin.test.Test
import kotlin.test.assertFalse

internal class BrickMaterialTest {
    @Test
    fun `should have distinct values`() {
        val set = mutableSetOf<Int>()

        BrickMaterial.entries.forEach { mat ->
            assertFalse(
                set.contains(mat.value),
                "Value ${mat.value} of BrickMaterial $mat must not be used more than once"
            )
            set.add(mat.value)
        }
    }
}
