package ch.digorydoo.titanium.engine.scene

import ch.digorydoo.kutils.string.zapPackageName
import ch.digorydoo.titanium.engine.i18n.ITextId
import ch.digorydoo.titanium.engine.state.StateManager.RestoredState

abstract class Scene(
    val id: ISceneId?, // null for anonymous loading scene
    val title: ITextId, // will appear in savegames
    fileNameStem: String, // used for all the related files
    initialLighting: Lighting,
    var lightingFollowsStoryTime: Boolean,
    val hasSky: Boolean,
    val hasShadows: Boolean,
) {
    val brickVolumeFileName = "${fileNameStem}.pf"
    open val brickTexFileName = "tiles-${fileNameStem}.png"
    val gelListFileName = "${fileNameStem}.gls"

    val lighting = MutableLighting(initialLighting)

    open fun enter(restore: RestoredState?) {}

    final override fun toString() =
        zapPackageName(super.toString())
}
