package ch.digorydoo.titanium.game.player

import ch.digorydoo.titanium.engine.anim.AnimCycle
import ch.digorydoo.titanium.engine.anim.AnimCycleDef
import ch.digorydoo.titanium.engine.anim.FrameCollection
import ch.digorydoo.titanium.engine.gel.GraphicElement
import ch.digorydoo.titanium.engine.utils.Direction
import ch.digorydoo.titanium.game.core.GameSampleId.WALK1
import ch.digorydoo.titanium.game.player.PlayerFrameManager.State.*

// TODO Move non-specific stuff into a base-class
class PlayerFrameManager(private val gel: GraphicElement, private val frames: FrameCollection) {
    enum class State {
        IDLE,
        WALKING,
        JUMPING,
        // TALKING,
        // KNEELING,
        // FOUND_ITEM,
        // USING_OBJECT,
        // CARRYING_OBJECT,
        // WALKING_AND_CARRYING,
    }

    private var state = IDLE

    val isIdle get() = state == IDLE
    val isJumping get() = state == JUMPING

    private val idle = mapOf(
        Direction.NORTH to AnimCycleDef(0),
        Direction.NE to AnimCycleDef(22),
        Direction.EAST to AnimCycleDef(44),
        Direction.SE to AnimCycleDef(66),
        Direction.SOUTH to AnimCycleDef(88),
        Direction.SW to AnimCycleDef(110),
        Direction.WEST to AnimCycleDef(132),
        Direction.NW to AnimCycleDef(154),
    )

    private val walking = mapOf(
        Direction.NORTH to AnimCycleDef(
            firstFrame = 2,
            lastFrame = 9,
            cycleDuration = 0.8f,
            startCycleSound = WALK1,
            halfCycleSound = WALK1,
        ),
        Direction.NE to AnimCycleDef(
            firstFrame = 24,
            lastFrame = 31,
            cycleDuration = 0.8f,
            startCycleSound = WALK1,
            halfCycleSound = WALK1,
        ),
        Direction.EAST to AnimCycleDef(
            firstFrame = 46,
            lastFrame = 53,
            cycleDuration = 0.8f,
            startCycleSound = WALK1,
            halfCycleSound = WALK1,
        ),
        Direction.SE to AnimCycleDef(
            firstFrame = 68,
            lastFrame = 75,
            cycleDuration = 0.8f,
            startCycleSound = WALK1,
            halfCycleSound = WALK1,
        ),
        Direction.SOUTH to AnimCycleDef(
            firstFrame = 90,
            lastFrame = 97,
            cycleDuration = 0.8f,
            startCycleSound = WALK1,
            halfCycleSound = WALK1,
        ),
        Direction.SW to AnimCycleDef(
            firstFrame = 112,
            lastFrame = 119,
            cycleDuration = 0.8f,
            startCycleSound = WALK1,
            halfCycleSound = WALK1,
        ),
        Direction.WEST to AnimCycleDef(
            firstFrame = 134,
            lastFrame = 141,
            cycleDuration = 0.8f,
            startCycleSound = WALK1,
            halfCycleSound = WALK1,
        ),
        Direction.NW to AnimCycleDef(
            firstFrame = 156,
            lastFrame = 163,
            cycleDuration = 0.8f,
            startCycleSound = WALK1,
            halfCycleSound = WALK1,
        ),
    )

    private val jumping = mapOf(
        Direction.NORTH to AnimCycleDef(1),
        Direction.NE to AnimCycleDef(23),
        Direction.EAST to AnimCycleDef(45),
        Direction.SE to AnimCycleDef(67),
        Direction.SOUTH to AnimCycleDef(89),
        Direction.SW to AnimCycleDef(111),
        Direction.WEST to AnimCycleDef(133),
        Direction.NW to AnimCycleDef(155),
    )

    private var dir = Direction.NW

    fun walk(d: Direction, cycleSpeedFactor: Float = 1.0f) {
        setDirStateCycle(d, WALKING, cycleSpeedFactor)
    }

    fun goIdle() {
        setDirStateCycle(dir, IDLE)
    }

    fun turn(d: Direction) {
        setDirStateCycle(d, IDLE)
    }

    fun jump() {
        setDirStateCycle(dir, JUMPING)
    }

    private fun setDirStateCycle(d: Direction, s: State, cycleSpeedFactor: Float = 1.0f) {
        if (dir == d && state == s) {
            // Just update cycleSpeedFactor.
            gel.cycle?.setSpeedFactor(cycleSpeedFactor)
        } else {
            dir = d
            state = s

            val f = when (state) {
                IDLE -> idle[dir]
                WALKING -> walking[dir]
                JUMPING -> jumping[dir]
            }

            if (f == null) {
                gel.cycle = null
                frames.setFrame(0)
            } else if (f.firstFrame == f.lastFrame || f.cycleDuration <= 0.0f) {
                gel.cycle = null
                frames.setFrame(f.firstFrame)
            } else {
                gel.cycle = AnimCycle(
                    object: AnimCycle.Delegate {
                        override val cycleDef: AnimCycleDef = f

                        override fun setFrame(idx: Int) {
                            frames.setFrame(idx)
                        }

                        override fun cycleEnded() {
                            gel.cycle = null
                        }
                    }
                ).apply { setSpeedFactor(cycleSpeedFactor) }

                frames.setFrame(f.firstFrame)
            }
        }
    }
}
