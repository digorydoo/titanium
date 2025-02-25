package ch.digorydoo.titanium.engine.editor

import ch.digorydoo.titanium.engine.brick.BrickShape
import ch.digorydoo.titanium.engine.brick.BrickShape.*
import ch.digorydoo.titanium.engine.editor.wizard.wizardRules
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class DrawingWizardTest {
    @Test
    fun `should cover all BrickShapes except known exceptions`() {
        val notCovered = arrayOf(
            NONE,
            BASIC_BLOCK,
            FLAT_CEILING,
            FLAT_FLOOR,
            FLAT_WALL_EAST,
            FLAT_WALL_NORTH,
            FLAT_WALL_SOUTH,
            FLAT_WALL_WEST,
        )

        val shapes = BrickShape.entries.toMutableList().apply { removeAll(notCovered) }

        wizardRules.forEach { check ->
            assertFalse(
                notCovered.contains(check.then),
                "BrickShape ${check.then} is covered by wizardRules, please remove it from explicit exclusion"
            )
            shapes.remove(check.then)
        }

        assertTrue(
            shapes.isEmpty(),
            "The following BrickShapes are not covered by wizardRules: ${shapes.joinToString(", ")}"
        )
    }
}
