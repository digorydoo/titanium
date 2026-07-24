package io.github.digorydoo.titanium.engine.core

abstract class GameLoop {
    @Suppress("RedundantInnerClassModifier") // not redundant
    inner class Token // the "inner" modifier makes it impossible to construct this class from outside

    // Implementing this interface makes it unnecessary to suppress the "unused" warning for the token argument.
    // The token is merely used to restrict otherwise public API.
    interface Tick {
        fun tick(token: Token)
    }

    // Only GameLoop and its implementation may access the token directly; but callees are free to pass it around.
    protected val token = Token()
}
