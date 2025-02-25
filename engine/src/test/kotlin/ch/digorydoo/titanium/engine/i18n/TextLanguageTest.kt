package ch.digorydoo.titanium.engine.i18n

import java.util.*
import kotlin.test.Test
import kotlin.test.assertFalse

internal class TextLanguageTest {
    @Test
    fun `should not use the same id more than once`() {
        val set = mutableSetOf<Int>()
        TextLanguage.entries.forEach {
            assertFalse(set.contains(it.id), "Id ${it.id} used by TextLanguage.$it has already been used")
            set.add(it.id)
        }
    }

    @Test
    fun `should not use the same Locale more than once`() {
        val set = mutableSetOf<Locale>()
        TextLanguage.entries.forEach {
            assertFalse(set.contains(it.locale), "Locale ${it.locale} used by TextLanguage.$it has already been used")
            set.add(it.locale)
        }
    }
}
