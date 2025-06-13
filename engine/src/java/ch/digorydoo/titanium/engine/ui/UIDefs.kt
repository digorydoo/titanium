package ch.digorydoo.titanium.engine.ui

import ch.digorydoo.kutils.colour.Colour
import ch.digorydoo.titanium.engine.ui.game_menu.GameMenu.Companion.SAVEGAME_THUMBNAIL_HEIGHT
import ch.digorydoo.titanium.engine.ui.game_menu.GameMenu.Companion.SAVEGAME_THUMBNAIL_WIDTH

// Dialogue

const val DLG_TEXT_MARGIN_X = 16
const val DLG_TEXT_MARGIN_Y = 16
const val DLG_BOTTOM_MARGIN = 48
const val DLG_LR_MARGIN = 8
const val DLG_BTN_RIGHT_MARGIN = 80
const val DLG_BTN_BOTTOM_MARGIN = 18
const val DLG_TEXT_PADDING = 8 // must be large enough for glyphs not overlapping the box
const val DLG_DEFAULT_WIDTH = 800 // width when there are no choice items
const val DLG_MIN_WIDTH = 700
const val DLG_CORNER_SIZE = 24

val dlgTextColour = Colour.grey50
val dlgBgColour = Colour(0.15f, 0.15f, 0.15f, 0.9f)

// ButtonGel when used as a menu item

const val ITEM_CORNER_SIZE = 16
const val ITEM_TEXT_OUTER_PADDING = 12 // space between otl and text
const val ITEM_TEXT_INNER_PADDING = 8 // must be large enough for glyphs not overlapping the box
const val ITEM_DEFAULT_HEIGHT = 64
const val ITEM_VALUE_MAX_WIDTH = 96 // maximal width of value (e.g. FloatChoice)
const val ITEM_INCDEC_MARGIN_LR = 48 // extra margin for increment/decrement symbols
const val ITEM_INCDEC_MARGIN_TOP = ITEM_TEXT_OUTER_PADDING + ITEM_TEXT_INNER_PADDING
const val ITEM_SPACING = 4
const val ITEM_MIN_WIDTH = 312
const val ITEM_MARGIN_RIGHT = 92
const val ITEM_MARGIN_TOP = 64 // comes into play when total height of items exceeds screen height
const val ITEM_MARGIN_BOTTOM = 128
const val ITEM_ICON_OFFSET_X = 56 // distance from right side of item to the left side of its icon
const val ITEM_ICON_OFFSET_Y = 46

val itemOtlColour = Colour.grey300

// ButtonGel when used as a save game summary button

const val SUMMARY_LINE_SPACING = 12
const val SUMMARY_BTN_MARGIN_TOP = 192
const val SUMMARY_BTN_WIDTH = SAVEGAME_THUMBNAIL_WIDTH + 512
const val SUMMARY_BTN_HEIGHT = SAVEGAME_THUMBNAIL_HEIGHT + 2 * ITEM_TEXT_OUTER_PADDING

// SnackbarGel

const val SNACKBAR_TEXT_MARGIN = 8
const val SNACKBAR_MARGIN_TOP = 64
const val SNACKBAR_TEXT_PADDING = 8 // must be large enough for glyphs not overlapping the box
const val SNACKBAR_DEFAULT_WIDTH = 800
const val SNACKBAR_CORNER_SIZE = 16
