package ch.digorydoo.titanium.engine.file

import kotlin.test.Test
import kotlin.test.assertFalse

internal class FileMarkerTest {
    @Test
    fun `should not use the same marker value more than once`() {
        val set = mutableSetOf<UShort>()

        FileMarker.entries.forEach { marker ->
            assertFalse(set.contains(marker.value), "FileMarker $marker uses value ${marker.value} more than once")
            set.add(marker.value)
        }
    }
}
