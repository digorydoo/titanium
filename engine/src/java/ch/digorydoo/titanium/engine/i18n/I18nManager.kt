package ch.digorydoo.titanium.engine.i18n

import java.util.*

abstract class I18nManager {
    protected var locale: Locale = Locale.ENGLISH; private set
    internal var engine = I18nBundle(EngineTextId.BUNDLE_NAME, locale); private set

    open fun setLocale(newLocale: Locale) {
        locale = newLocale
        engine = I18nBundle(EngineTextId.BUNDLE_NAME, newLocale)
    }

    fun getString(textId: ITextId) = textId.bundle.getString(textId)
    fun choose(textId: ITextId, value: Int) = textId.bundle.choose(textId, value)
    fun format(textId: ITextId, vararg args: Any) = textId.bundle.format(textId, *args)
}
