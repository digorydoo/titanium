package ch.digorydoo.titanium.engine.i18n

/**
 * Warning: JetBrains IDEA behaves in an unexpected way regarding properties files! If you encounter encoding issues,
 * check properties files whether the IDE has garbled them. Always set Settings > File encodings > Properties default
 * encoding to UTF-8!
 */
interface ITextId {
    val bundle: I18nBundle
    val resId: String
}
