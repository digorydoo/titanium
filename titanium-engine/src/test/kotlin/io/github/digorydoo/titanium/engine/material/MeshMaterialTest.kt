package io.github.digorydoo.titanium.engine.material

import io.github.digorydoo.titanium.engine.mesh.MeshMaterial
import kotlin.test.Test
import kotlin.test.assertFalse

internal class MeshMaterialTest {
    @Test
    fun `should have distinct values`() {
        val set = mutableSetOf<Int>()

        MeshMaterial.entries.forEach { mat ->
            assertFalse(
                set.contains(mat.value),
                "Value ${mat.value} of MeshMaterial $mat must not be used more than once",
            )
            set.add(mat.value)
        }
    }
}
