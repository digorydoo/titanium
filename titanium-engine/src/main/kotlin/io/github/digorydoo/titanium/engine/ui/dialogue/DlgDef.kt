package io.github.digorydoo.titanium.engine.ui.dialogue

import io.github.digorydoo.titanium.engine.file.SaveGameFileWriter
import io.github.digorydoo.titanium.engine.i18n.ITextId
import io.github.digorydoo.titanium.engine.intermission.Intermission

// This annotation prevents accidental calls of a member in the DSL's outer scope
@DslMarker
annotation class DlgDsl

@DlgDsl
class DlgDef private constructor() {
    var text = ""
    var textId: ITextId? = null
    val items = mutableListOf<DlgItemDef>()
    var suppressSoundsOnShowAndDismiss = false
    var focus: DlgItemDef? = null // initially highlighted item; null = highlight first item (if any)
    var dismiss: DlgItemDef? = null // item that will close the dialogue

    fun item(lambda: DlgTextItemDef.() -> Unit) =
        DlgTextItemDef.build(lambda).also { items.add(it) }

    fun itemWithBooleanValue(lambda: DlgBooleanItemDef.() -> Unit) =
        DlgBooleanItemDef.build(lambda).also { items.add(it) }

    fun itemWithIntValue(lambda: DlgIntItemDef.() -> Unit) =
        DlgIntItemDef.build(lambda).also { items.add(it) }

    fun itemWithFloatValue(lambda: DlgFloatItemDef.() -> Unit) =
        DlgFloatItemDef.build(lambda).also { items.add(it) }

    fun item(saveGameSummary: SaveGameFileWriter.Summary) =
        DlgSavegameItemDef.fromSummary(saveGameSummary).also { items.add(it) }

    // The selectedItem may be null if the dialogue has no items or if it was aborted
    var onClose: ((DlgItemDef?) -> Unit)? = null

    companion object {
        fun build(lambda: DlgDef.() -> Unit) =
            DlgDef().apply { lambda() }
    }
}

interface DlgItemData

@DlgDsl
sealed class DlgItemDef {
    // It is valid to set both a text and a textId, in which case text will be displayed to the user while textId may
    // be used by code to identify the item.
    var text = ""
    var textId: ITextId? = null

    // When textId is not enough to identify the item, callers can use data instead.
    var data: DlgItemData? = null
}

interface SuspendingOnSelect {
    val onSelect: (suspend Intermission.() -> Unit)?
}

@DlgDsl
class DlgTextItemDef private constructor(): DlgItemDef(), SuspendingOnSelect {
    var autoDismiss = true
    override var onSelect: (suspend Intermission.() -> Unit)? = null

    companion object {
        fun build(lambda: DlgTextItemDef.() -> Unit) =
            DlgTextItemDef().apply { lambda() }
    }
}

@DlgDsl
class DlgBooleanItemDef private constructor(): DlgItemDef() {
    var initialValue = false
    var onChange: ((newValue: Boolean) -> Unit)? = null

    companion object {
        fun build(lambda: DlgBooleanItemDef.() -> Unit) =
            DlgBooleanItemDef().apply { lambda() }
    }
}

@DlgDsl
class DlgIntItemDef private constructor(): DlgItemDef() {
    var initialValue = 0
    var minValue = Int.MIN_VALUE
    var maxValue = Int.MAX_VALUE
    var step = 1
    var smallStep = 1
    var onChange: ((newValue: Int) -> Unit)? = null

    companion object {
        fun build(lambda: DlgIntItemDef.() -> Unit) =
            DlgIntItemDef().apply { lambda() }
    }
}

@DlgDsl
class DlgFloatItemDef private constructor(): DlgItemDef() {
    var initialValue = 0.0f
    var minValue = Float.NEGATIVE_INFINITY
    var maxValue = Float.POSITIVE_INFINITY
    var step = 1.0f
    var smallStep = 0.5f
    var modulo: Float? = null // FIXME should be var modulo = false
    var onChange: ((newValue: Float) -> Unit)? = null

    companion object {
        fun build(lambda: DlgFloatItemDef.() -> Unit) =
            DlgFloatItemDef().apply { lambda() }
    }
}

@DlgDsl
class DlgSavegameItemDef private constructor(val summary: SaveGameFileWriter.Summary): DlgItemDef() {
    companion object {
        // Currently not a builder, but still using a private constructor to keep it somewhat consistent with the rest
        fun fromSummary(summary: SaveGameFileWriter.Summary) =
            DlgSavegameItemDef(summary)
    }
}

