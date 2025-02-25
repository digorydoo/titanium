package ch.digorydoo.titanium.engine.input

interface Gamepad {
    fun findJoyId()
    fun bindJoyId(jid: Int)
    fun update()
}
