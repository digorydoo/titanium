package ch.digorydoo.titanium.game.i18n

import ch.digorydoo.titanium.engine.i18n.I18nBundle
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

internal class GameTextIdTest {
    @Test
    fun `should use the constant name as its id`() {
        GameTextId.entries.forEach {
            assertEquals(it.name, it.resId)
        }
    }

    @Test
    fun `should have English translations for all of the defined texts`() {
        val bundle = I18nBundle(GameTextId.BUNDLE_NAME, Locale.ENGLISH)
        GameTextId.entries.forEach {
            assertNotNull(bundle.getStringOrNull(it), "GameTextId.$it has no English translation!\n")
        }
    }

    @Test
    fun `should have German translations for all of the defined texts`() {
        val bundle = I18nBundle(GameTextId.BUNDLE_NAME, Locale.GERMAN)
        GameTextId.entries.forEach {
            assertNotNull(bundle.getStringOrNull(it), "GameTextId.$it has no German translation!\n")
        }
    }
}
