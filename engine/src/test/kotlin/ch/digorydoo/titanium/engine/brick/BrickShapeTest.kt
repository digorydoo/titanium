package ch.digorydoo.titanium.engine.brick

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class BrickShapeTest {
    @Test
    fun `should have distinct ids`() {
        assertEquals(0, BrickShape.NONE.id)

        val set = mutableSetOf<Int>()

        BrickShape.entries.forEach { shape ->
            assertTrue(shape.id >= 0)
            assertFalse(set.contains(shape.id), "Id ${shape.id} of Shape $shape used more than once")
            set.add(shape.id)
        }
    }

    @Test
    fun `should have relVolumes between 0 and 1`() {
        BrickShape.entries.forEach { shape ->
            when (shape) {
                BrickShape.NONE -> assertEquals(0.0f, shape.relVolume)
                BrickShape.BASIC_BLOCK -> assertEquals(1.0f, shape.relVolume)
                else -> assertTrue(
                    shape.relVolume > 0.0f && shape.relVolume < 1.0f,
                    "Shape $shape has a relative volume of ${shape.relVolume}, which is out of range"
                )
            }
        }
    }
}
