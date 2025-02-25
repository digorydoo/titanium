package ch.digorydoo.titanium.engine.input

interface Keyboard {
    fun onCharPressedOnce(lambda: (c: Char) -> Unit)
    fun onCharPressedWithRepeat(lambda: (c: Char) -> Unit)
    fun onNumberPressedOnce(lambda: (num: Int) -> Unit)
    fun onNumberPressedWithRepeat(lambda: (num: Int) -> Unit)
}
