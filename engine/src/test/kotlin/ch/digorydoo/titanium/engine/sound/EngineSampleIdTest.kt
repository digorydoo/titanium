package ch.digorydoo.titanium.engine.sound

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class EngineSampleIdTest {
    @Test
    fun `should have distinct ids`() {
        val entries = EngineSampleId.entries
        assertTrue(entries.isNotEmpty())

        val set = mutableSetOf<Int>()

        entries.forEach { smp ->
            assertFalse("Id ${smp.id} of EngineSampleId $smp must not be used more than once") {
                set.contains(smp.id)
            }
            set.add(smp.id)
        }
    }

    @Test
    fun `should use ids in range 0 until 1000`() {
        EngineSampleId.entries.forEach { smp ->
            assertTrue("Id ${smp.id} of EngineSampleId $smp out of range") {
                smp.id in 0 ..< 1000
            }
        }
    }
}
