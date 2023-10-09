package com.example.snackplay.View.Controller

class GameController {

    private var clickFun = mutableListOf<((GameEvent) -> Unit)>()

    fun dispatchEvent(event: GameEvent) {
        clickFun.forEach {
            it.invoke(event)
        }
    }

    fun moveUp() {
        dispatchEvent(GameEvent.UP)
    }

    fun moveDown() {
        dispatchEvent(GameEvent.DOWN)
    }

    fun moveLeft() {
        dispatchEvent(GameEvent.LEFT)
    }

    fun moveRight() {
        dispatchEvent(GameEvent.RIGHT)
    }

    fun restart() {
        dispatchEvent(GameEvent.RESTART)
    }

    fun registerClickFun(function: (GameEvent) -> Unit) {
        clickFun.add(function)
    }

    fun unregisterClickFun(function: (GameEvent) -> Unit) {
        clickFun.remove(function)
    }

}

enum class GameEvent{
    UP, DOWN, LEFT, RIGHT, TOUCH_BOUNDARY, EAT_SELF, RESTART
}

