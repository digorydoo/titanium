package ch.digorydoo.titanium.engine.ui.dialogue

import ch.digorydoo.titanium.engine.file.SaveGameFileWriter
import ch.digorydoo.titanium.engine.i18n.ITextId

// This annotation prevents accidental calls of a member in the DSL's outer scope
@DslMarker
annotation class DlgDsl

@DlgDsl
class DlgDef<Id> private constructor() {
    var text = ""
    var textId: ITextId? = null
    val items = mutableListOf<DlgItemDef<Id>>()
    var suppressSoundsOnShowAndDismiss = false
    var focus: DlgItemDef<Id>? = null // initially highlighted item; null = highlight first item (if any)
    var dismiss: DlgItemDef<Id>? = null // item that will close the dialogue

    fun item(lambda: DlgTextItemDef<Id>.() -> Unit) =
        DlgTextItemDef.build(lambda).also { items.add(it) }

    fun itemWithBooleanValue(lambda: DlgBooleanItemDef<Id>.() -> Unit) =
        DlgBooleanItemDef.build(lambda).also { items.add(it) }

    fun itemWithIntValue(lambda: DlgIntItemDef<Id>.() -> Unit) =
        DlgIntItemDef.build(lambda).also { items.add(it) }

    fun itemWithFloatValue(lambda: DlgFloatItemDef<Id>.() -> Unit) =
        DlgFloatItemDef.build(lambda).also { items.add(it) }

    fun itemForSavegame(lambda: DlgSavegameItemDef<Id>.() -> Unit) =
        DlgSavegameItemDef.build(lambda).also { items.add(it) }

    // The selectedItem may be null if the dialogue has no items or if it was aborted
    var onClose: ((DlgItemDef<Id>?) -> Unit)? = null

    companion object {
        fun <Id> build(lambda: DlgDef<Id>.() -> Unit) =
            DlgDef<Id>().apply { lambda() }
    }
}

@DlgDsl
sealed class DlgItemDef<Id> {
    var id: Id? = null
    var text = ""
    var textId: ITextId? = null
}

@DlgDsl
class DlgTextItemDef<Id> private constructor(): DlgItemDef<Id>() {
    var autoDismiss = true
    var onSelect: (() -> Unit)? = null

    companion object {
        fun <Id> build(lambda: DlgTextItemDef<Id>.() -> Unit) =
            DlgTextItemDef<Id>().apply { lambda() }
    }
}

@DlgDsl
class DlgBooleanItemDef<Id> private constructor(): DlgItemDef<Id>() {
    var initialValue = false
    var onChange: ((newValue: Boolean) -> Unit)? = null

    companion object {
        fun <Id> build(lambda: DlgBooleanItemDef<Id>.() -> Unit) =
            DlgBooleanItemDef<Id>().apply { lambda() }
    }
}

@DlgDsl
class DlgIntItemDef<Id> private constructor(): DlgItemDef<Id>() {
    var initialValue = 0
    var minValue = Int.MIN_VALUE
    var maxValue = Int.MAX_VALUE
    var step = 1
    var smallStep = 1
    var onChange: ((newValue: Int) -> Unit)? = null

    companion object {
        fun <Id> build(lambda: DlgIntItemDef<Id>.() -> Unit) =
            DlgIntItemDef<Id>().apply { lambda() }
    }
}

@DlgDsl
class DlgFloatItemDef<Id> private constructor(): DlgItemDef<Id>() {
    var initialValue = 0.0f
    var minValue = Float.NEGATIVE_INFINITY
    var maxValue = Float.POSITIVE_INFINITY
    var step = 1.0f
    var smallStep = 0.5f
    var modulo: Float? = null // FIXME should be var modulo = false
    var onChange: ((newValue: Float) -> Unit)? = null

    companion object {
        fun <Id> build(lambda: DlgFloatItemDef<Id>.() -> Unit) =
            DlgFloatItemDef<Id>().apply { lambda() }
    }
}

@DlgDsl
class DlgSavegameItemDef<Id> private constructor(): DlgItemDef<Id>() {
    var summary: SaveGameFileWriter.Summary? = null
    var onSelect: (() -> Unit)? = null

    companion object {
        fun <Id> build(lambda: DlgSavegameItemDef<Id>.() -> Unit) =
            DlgSavegameItemDef<Id>().apply { lambda() }
    }
}

