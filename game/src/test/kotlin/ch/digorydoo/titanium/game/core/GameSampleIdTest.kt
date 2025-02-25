package ch.digorydoo.titanium.game.core

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class GameSampleIdTest {
    @Test
    fun `should have distinct ids`() {
        val entries = GameSampleId.entries
        assertTrue(entries.isNotEmpty())

        val set = mutableSetOf<Int>()

        entries.forEach { smp ->
            assertFalse("Id ${smp.id} of GameSampleId $smp must not be used more than once") {
                set.contains(smp.id)
            }
            set.add(smp.id)
        }
    }

    @Test
    fun `should use ids greater or equal 1000`() {
        GameSampleId.entries.forEach { smp ->
            assertTrue("Id ${smp.id} of GameSampleId $smp out of range") {
                smp.id >= 1000
            }
        }
    }
}
