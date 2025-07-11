package ch.digorydoo.titanium.game.player

import ch.digorydoo.titanium.engine.sprite.FrameCollection
import ch.digorydoo.titanium.engine.sprite.FrameCycle
import ch.digorydoo.titanium.engine.sprite.FrameCycleDef
import ch.digorydoo.titanium.engine.utils.Direction
import ch.digorydoo.titanium.game.core.GameSampleId.WALK1
import ch.digorydoo.titanium.game.player.PlayerFrameCycles.State.*

class PlayerFrameCycles(private val frames: FrameCollection) {
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

    var cycle: FrameCycle? = null; private set

    private val idle = mapOf(
        Direction.NORTH to FrameCycleDef(0),
        Direction.NE to FrameCycleDef(22),
        Direction.EAST to FrameCycleDef(44),
        Direction.SE to FrameCycleDef(66),
        Direction.SOUTH to FrameCycleDef(88),
        Direction.SW to FrameCycleDef(110),
        Direction.WEST to FrameCycleDef(132),
        Direction.NW to FrameCycleDef(154),
    )

    private val walking = mapOf(
        Direction.NORTH to FrameCycleDef(
            firstFrame = 2,
            lastFrame = 9,
            cycleDuration = 0.8f,
            startCycleSound = WALK1,
            halfCycleSound = WALK1,
        ),
        Direction.NE to FrameCycleDef(
            firstFrame = 24,
            lastFrame = 31,
            cycleDuration = 0.8f,
            startCycleSound = WALK1,
            halfCycleSound = WALK1,
        ),
        Direction.EAST to FrameCycleDef(
            firstFrame = 46,
            lastFrame = 53,
            cycleDuration = 0.8f,
            startCycleSound = WALK1,
            halfCycleSound = WALK1,
        ),
        Direction.SE to FrameCycleDef(
            firstFrame = 68,
            lastFrame = 75,
            cycleDuration = 0.8f,
            startCycleSound = WALK1,
            halfCycleSound = WALK1,
        ),
        Direction.SOUTH to FrameCycleDef(
            firstFrame = 90,
            lastFrame = 97,
            cycleDuration = 0.8f,
            startCycleSound = WALK1,
            halfCycleSound = WALK1,
        ),
        Direction.SW to FrameCycleDef(
            firstFrame = 112,
            lastFrame = 119,
            cycleDuration = 0.8f,
            startCycleSound = WALK1,
            halfCycleSound = WALK1,
        ),
        Direction.WEST to FrameCycleDef(
            firstFrame = 134,
            lastFrame = 141,
            cycleDuration = 0.8f,
            startCycleSound = WALK1,
            halfCycleSound = WALK1,
        ),
        Direction.NW to FrameCycleDef(
            firstFrame = 156,
            lastFrame = 163,
            cycleDuration = 0.8f,
            startCycleSound = WALK1,
            halfCycleSound = WALK1,
        ),
    )

    private val jumping = mapOf(
        Direction.NORTH to FrameCycleDef(1),
        Direction.NE to FrameCycleDef(23),
        Direction.EAST to FrameCycleDef(45),
        Direction.SE to FrameCycleDef(67),
        Direction.SOUTH to FrameCycleDef(89),
        Direction.SW to FrameCycleDef(111),
        Direction.WEST to FrameCycleDef(133),
        Direction.NW to FrameCycleDef(155),
    )

    var dir = Direction.NW; private set

    fun walk(d: Direction, cycleSpeedFactor: Float = 1.0f) {
        setDirStateCycle(d, WALKING, cycleSpeedFactor)
    }

    fun goIdle() {
        setDirStateCycle(dir, IDLE)
    }

    fun turn(d: Direction) {
        setDirStateCycle(d, if (isJumping) JUMPING else IDLE)
    }

    fun jump(d: Direction) {
        setDirStateCycle(d, JUMPING)
    }

    private fun setDirStateCycle(d: Direction, s: State, cycleSpeedFactor: Float = 1.0f) {
        if (dir == d && state == s) {
            // Just update cycleSpeedFactor.
            cycle?.setSpeedFactor(cycleSpeedFactor)
        } else {
            dir = d
            state = s

            val f = when (state) {
                IDLE -> idle[dir]
                WALKING -> walking[dir]
                JUMPING -> jumping[dir]
            }

            if (f == null) {
                cycle = null
                frames.setFrame(0)
            } else if (f.firstFrame == f.lastFrame || f.cycleDuration <= 0.0f) {
                cycle = null
                frames.setFrame(f.firstFrame)
            } else {
                cycle = FrameCycle(
                    object: FrameCycle.Delegate {
                        override val cycleDef: FrameCycleDef = f

                        override fun setFrame(idx: Int) {
                            frames.setFrame(idx)
                        }

                        override fun cycleEnded() {
                            cycle = null
                        }
                    }
                ).apply { setSpeedFactor(cycleSpeedFactor) }

                frames.setFrame(f.firstFrame)
            }
        }
    }
}
