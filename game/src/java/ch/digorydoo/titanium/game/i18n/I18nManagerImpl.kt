package ch.digorydoo.titanium.game.i18n

import ch.digorydoo.titanium.engine.i18n.I18nBundle
import ch.digorydoo.titanium.engine.i18n.I18nManager
import java.util.*

class I18nManagerImpl: I18nManager() {
    internal var game = I18nBundle(GameTextId.BUNDLE_NAME, locale); private set

    override fun setLocale(newLocale: Locale) {
        super.setLocale(newLocale)
        game = I18nBundle(GameTextId.BUNDLE_NAME, newLocale)
    }
}
