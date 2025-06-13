package ch.digorydoo.titanium.engine.core

import ch.digorydoo.kutils.math.angleDiff
import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.engine.gel.GraphicElement
import ch.digorydoo.titanium.engine.i18n.ITextId
import kotlin.math.abs
import kotlin.math.atan2

class ActionManager {
    interface ActionDelegate {
        fun onSelect(action: Action) // called when the user selects the action
    }

    class Action(
        var verb: ITextId, // the action verb that will be displayed along with the action key in the HUD
        val target: GraphicElement, // a door, switch or similar gel; defines the position in world coords
        val delegate: ActionDelegate, // receives callbacks
        var time: Float, // session time when action was first issued or renewed
    )

    private val actions = mutableListOf<Action>()

    fun register(verb: ITextId, target: GraphicElement, delegate: ActionDelegate) {
        val oldAction = actions.find { it.target == target }

        if (oldAction == null) {
            // There is no action for the target yet.
            actions.add(Action(verb, target, delegate, App.time.sessionTime))
        } else if (oldAction.delegate != delegate) {
            // A gel should use the same delegate instance for all its actions, nor should multiple gels provide
            // an action for the same target.
            Log.warn(TAG, "Ignoring action $verb for $target, because somebody else has already registered it")
        } else if (oldAction.verb != verb) {
            // The gel reported a different verb for the same target, e.g. a door changed from "OPEN" to "CLOSE".
            oldAction.verb = verb
            oldAction.time = App.time.sessionTime
        } else {
            // The same action was registered again, which happens frequently when the player is close to some gel.
            oldAction.time = App.time.sessionTime
        }
    }

    fun maintain() {
        if (App.gameMenu.isShown || App.content.isLoading) return
        val player = App.player ?: return

        actions.removeAll { it.target.zombie || App.time.sessionTime - it.time > MAX_NUM_SECONDS_UNTIL_REMOVE }

        var closestAction: Action? = null

        if (player.allowActions && actions.isNotEmpty()) {
            val playerPos = player.pos
            val px = playerPos.x
            val py = playerPos.y
            val pz = playerPos.z

            // FIXME this currently does not work correctly, because player's rotationPhi is just camera...

            val playerPhi = player.rotationPhi

            var closestSqrDistToIdealSpot = Float.POSITIVE_INFINITY

            actions.forEach { action ->
                val targetPos = action.target.pos
                val dx = targetPos.x - px
                val dy = targetPos.y - py
                val rho = atan2(dy, dx)
                val dw = abs(angleDiff(playerPhi, rho))

                if (dw <= MAX_ANGLE_DIFF) {
                    val dz = targetPos.z - pz
                    val dsqr = dx * dx + dy * dy + dz * dz

                    if (dsqr < closestSqrDistToIdealSpot) {
                        closestAction = action
                        closestSqrDistToIdealSpot = dsqr
                    }
                }
            }
        }

        if (closestAction == null) {
            App.hud.hideAction()
        } else {
            App.hud.showAction(closestAction.verb, closestAction.target)
        }
    }

    companion object {
        private val TAG = Log.Tag("ActionManager")
        private const val MAX_NUM_SECONDS_UNTIL_REMOVE = 0.2f
        private const val MAX_ANGLE_DIFF = 2.0f // radians
    }
}
