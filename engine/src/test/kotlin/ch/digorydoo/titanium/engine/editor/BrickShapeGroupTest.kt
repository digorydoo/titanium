package ch.digorydoo.titanium.engine.editor

import ch.digorydoo.titanium.engine.brick.BrickShape
import ch.digorydoo.titanium.engine.editor.items.BrickShapeGroup
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

internal class BrickShapeGroupTest {
    @Test
    fun `should each have either subgroups or shapes (or both)`() {
        BrickShapeGroup.entries.forEach { grp ->
            val subgroups = grp.subgroups()
            val shapes = grp.shapes()
            assertTrue(subgroups.isNotEmpty() || shapes.isNotEmpty(), "Group $grp has neither subgroup nor shape")
        }
    }

    @Test
    fun `should each be reachable starting from ROOT except ROOT itself`() {
        BrickShapeGroup.entries.forEach { grp ->
            assertTrue(
                grp == BrickShapeGroup.ROOT || BrickShapeGroup.ROOT.findFirstContaining(grp) != null,
                "Group $grp is not reachable from ROOT"
            )
        }
    }

    @Test
    fun `should provide at least one group for each BrickShape except for NONE`() {
        BrickShape.entries.forEach { shape ->
            assertTrue(
                shape == BrickShape.NONE || BrickShapeGroup.ROOT.findFirstContaining(shape) != null,
                "Shape $shape is not contained in any reachable group"
            )
        }
    }
}
