package com.example.snackplay

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.helper.widget.Layer
import androidx.core.view.isVisible
import com.example.snackplay.View.Controller.GameEvent
import com.example.snackplay.View.Model.GameObject
import com.example.snackplay.View.PlayView

class GameActivity : AppCompatActivity() {

    val game: PlayView by lazy { findViewById(R.id.game) }

    val tvEndSettlement: TextView by lazy { findViewById(R.id.tvEndSettlement) }

    val layoutOperate: Layer by lazy { findViewById(R.id.operate_layout) }

    val tvRestart: Button by lazy { findViewById(R.id.tvRestart) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        var diff = intent.getIntExtra("difficult", 1)
        game.difficulty = diff
        var controller = game.controller
        controller.registerClickFun { event ->
            if (event == GameEvent.RESTART) {
                tvEndSettlement.isVisible = false
                layoutOperate.visibility = View.VISIBLE
                tvRestart.isVisible = false
            }
        }
        findViewById<Button>(R.id.tvDown).setOnClickListener {
            controller.moveDown()
        }
        findViewById<Button>(R.id.tvUp).setOnClickListener {
            controller.moveUp()
        }
        findViewById<Button>(R.id.tvRight).setOnClickListener {
            controller.moveRight()
        }
        findViewById<Button>(R.id.tvLeft).setOnClickListener {
            controller.moveLeft()
        }
        tvRestart.setOnClickListener {
            controller.restart()
        }
        game.endFun = { gameEvent, i ->
            var endReason = ""
            when(gameEvent){
                GameEvent.TOUCH_BOUNDARY -> {
                    endReason = "触边了"
                }
                GameEvent.EAT_SELF -> {
                    endReason = "自噬了"
                }
                else -> {}
            }
            tvEndSettlement.isVisible = true
            layoutOperate.visibility = View.INVISIBLE
            tvRestart.isVisible = true
            tvEndSettlement.text = "$endReason\n分数：${game.totalBounds}\n难度：${diff}"
        }
    }
}