package ch.digorydoo.titanium.engine.ui.choice

import ch.digorydoo.titanium.engine.file.SaveGameFileWriter.Summary

class SavegameChoice(val summary: Summary, private val onSelectLambda: () -> Unit): Choice() {
    override val itemText = "${summary.sceneTitle}\n${summary.saveDateLocalized}"
    override val autoDismiss = true
    override val canSelect = true

    override fun onSelect() {
        onSelectLambda()
    }
}
