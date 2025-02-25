package ch.digorydoo.titanium.engine.i18n

import java.util.*

// This enum lists the languages we actually support. It's called TextLanguage, because later we may add another
// enum for SpeechLanguage, which may or may not be the same set of languages.
enum class TextLanguage(val id: Int, val locale: Locale, val displayText: String) {
    ENGLISH(0, Locale.ENGLISH, "English"),
    GERMAN(1, Locale.GERMAN, "Deutsch");

    companion object {
        fun fromId(id: Int) =
            entries.find { it.id == id }

        fun fromLocale(locale: Locale) =
            entries.find { it.locale == locale }
    }
}
