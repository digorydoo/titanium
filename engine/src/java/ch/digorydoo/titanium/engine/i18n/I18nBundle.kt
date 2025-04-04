package ch.digorydoo.titanium.engine.i18n

import ch.digorydoo.kutils.utils.Log
import java.text.ChoiceFormat
import java.text.MessageFormat
import java.util.*

class I18nBundle(private val name: String, private val locale: Locale) {
    private var _res: ResourceBundle? = null

    private val res: ResourceBundle
        get() = _res ?: load()

    private fun load(): ResourceBundle {
        return ResourceBundle.getBundle(name, locale)!!.also { _res = it }
    }

    fun getStringOrNull(id: ITextId): String? {
        // We must not access id.bundle here, because this function is called from unit tests, where App is unavailable.
        val res = res // outside try-block, so it can fail by its own
        try {
            return res.getString(id.resId)
        } catch (_: MissingResourceException) {
            Log.warn(TAG, "Missing i18n text: $id")
            return null
        }
    }

    fun getString(id: ITextId): String {
        require(id.bundle == this)
        return getStringOrNull(id) ?: "?$id?"
    }

    fun choose(id: ITextId, value: Int): String {
        require(id.bundle == this)
        val fmt = getString(id)
        val chosen = ChoiceFormat(fmt).format(value) ?: "" // choose a variant
        return MessageFormat.format(chosen, value) ?: "" // replace placeholders within the variant
    }

    fun format(id: ITextId, vararg args: Any): String {
        require(id.bundle == this)
        val fmt = getString(id)
        return MessageFormat.format(fmt, *args) ?: ""
    }

    companion object {
        private val TAG = Log.Tag("I18nBundle")
    }
}
