package ch.digorydoo.titanium.engine.editor

import ch.digorydoo.titanium.engine.brick.BrickMaterial
import ch.digorydoo.titanium.engine.editor.items.BrickMaterialGroup
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

internal class BrickMaterialGroupTest {
    @Test
    fun `should each have either subgroups or materials (or both)`() {
        BrickMaterialGroup.entries.forEach { grp ->
            val subgroups = grp.subgroups()
            val materials = grp.materials()
            assertTrue(subgroups.isNotEmpty() || materials.isNotEmpty(), "Group $grp has neither subgroup nor material")
        }
    }

    @Test
    fun `should each be reachable starting from ROOT except ROOT itself`() {
        BrickMaterialGroup.entries.forEach { grp ->
            assertTrue(
                grp == BrickMaterialGroup.ROOT || BrickMaterialGroup.ROOT.findFirstContaining(grp) != null,
                "Group $grp is not reachable from ROOT"
            )
        }
    }

    @Test
    fun `should provide at least one group for each BrickMaterial`() {
        BrickMaterial.entries.forEach { mat ->
            assertTrue(
                BrickMaterialGroup.ROOT.findFirstContaining(mat) != null,
                "Material $mat is not contained in any reachable group"
            )
        }
    }
}
