package ch.digorydoo.titanium.game.core

import kotlin.test.Test
import kotlin.test.assertFalse

internal class SceneIdTest {
    @Test
    fun `should have distinct values`() {
        val set = mutableSetOf<Int>()

        SceneId.entries.forEach { sceneId ->
            assertFalse(set.contains(sceneId.value), "Value ${sceneId.value} of SceneId $sceneId used more than once")
            set.add(sceneId.value)
        }
    }
}
