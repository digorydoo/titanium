package ch.digorydoo.titanium.engine.i18n

import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

internal class EngineTextIdTest {
    @Test
    fun `should use the constant name as its id`() {
        EngineTextId.entries.forEach {
            assertEquals(it.name, it.resId)
        }
    }

    @Test
    fun `should have English translations for all of the defined texts`() {
        val bundle = I18nBundle(EngineTextId.BUNDLE_NAME, Locale.ENGLISH)
        EngineTextId.entries.forEach {
            assertNotNull(bundle.getStringOrNull(it), "EngineTextId.$it has no English translation!\n")
        }
    }

    @Test
    fun `should have German translations for all of the defined texts`() {
        val bundle = I18nBundle(EngineTextId.BUNDLE_NAME, Locale.GERMAN)
        EngineTextId.entries.forEach {
            assertNotNull(bundle.getStringOrNull(it), "EngineTextId.$it has no German translation!\n")
        }
    }
}
