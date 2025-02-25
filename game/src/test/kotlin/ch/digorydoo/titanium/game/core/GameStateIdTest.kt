package ch.digorydoo.titanium.game.core

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class GameStateIdTest {
    @Test
    fun `should have distinct values`() {
        val set = mutableSetOf<UShort>()

        GameStateId.entries.forEach { id ->
            assertFalse(set.contains(id.value), "Value ${id.value} of GameStateId $id used more than once")
            set.add(id.value)
        }
    }

    @Test
    fun `should have values in the expected range`() {
        GameStateId.entries.forEach { id ->
            assertTrue(id.value in 1001u ..< 10000u, "Value ${id.value} of $id not inside expected range")
        }
    }
}
