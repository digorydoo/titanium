package ch.digorydoo.titanium.engine.state

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class EngineStateIdTest {
    @Test
    fun `should have distinct values`() {
        val set = mutableSetOf<UShort>()

        EngineStateId.entries.forEach { id ->
            assertFalse(set.contains(id.value), "Value ${id.value} of EngineStateId $id used more than once")
            set.add(id.value)
        }
    }

    @Test
    fun `should have values in the expected range`() {
        EngineStateId.entries.forEach { id ->
            assertTrue(id.value in 1u ..< 1000u, "Value ${id.value} of $id not inside expected range")
        }
    }
}
