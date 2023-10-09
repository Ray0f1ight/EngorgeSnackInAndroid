package com.example.snackplay.View

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.os.SystemClock
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.example.snackplay.View.Model.BoundaryObject
import com.example.snackplay.View.Model.FoodObject
import com.example.snackplay.View.Model.GameObject
import com.example.snackplay.View.Model.SnackNode
import java.util.jar.Attributes

abstract class BaseView @JvmOverloads constructor(context: Context, attributes: AttributeSet? = null, defStyleAttr: Int = 0): View(context, attributes, defStyleAttr) {

    var isRun = true

    var fps = 30

    private var lastUpdateTime = 0L

    private lateinit var runThread: Thread

    private var isFirstInit = true

    private val sizeUnit by lazy { width * 1 / 300 }

    private val snackPaint = Paint()

    private val foodPaint = Paint()

    private val boundaryPaint = Paint()

    open var gameObjects = mutableListOf<GameObject>()

    init {
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
//        Log.i("BaseView", "onLayout")
        if (isFirstInit) {
            initData()
            start()
            runThread = Thread {
                while (isRun) {
//                    Log.i("BaseView", "run in Thread:  ${SystemClock.currentThreadTimeMillis() - lastUpdateTime > (1000 / fps)}")
                    if (SystemClock.currentThreadTimeMillis() - lastUpdateTime > (1000 / fps)) {
                        lastUpdateTime = SystemClock.currentThreadTimeMillis()
                        update()
                        postInvalidate()
                    }
                }
            }
            runThread.start()
            isFirstInit = false
        }
    }

    private fun initData() {
        snackPaint.color = Color.CYAN
        foodPaint.color = Color.RED
        foodPaint.style = Paint.Style.FILL
        boundaryPaint.color = Color.BLUE
        boundaryPaint.style = Paint.Style.STROKE
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        gameObjects.reversed().forEach { item ->
            when(item) {
                is SnackNode -> drawSnack(canvas, item)
                is FoodObject -> drawFood(canvas, item)
                is BoundaryObject -> drawBoundary(canvas, item)
            }
        }
    }

    //刷新间隔 毫秒
    protected fun getRefreshInterval(): Long {
        return (1000 / fps).toLong()
    }

    //画蛇
    private fun drawSnack(canvas: Canvas?, node: SnackNode?) {
        if (node == null) {
            return
        }
        canvas?.drawRect(
            node.getRect(getRealSize(node.snackNodeWidth)), snackPaint
        )
        drawSnack(canvas, node.next)
    }

    //画食物
    private fun drawFood(canvas: Canvas?, food: FoodObject) {
        canvas?.drawOval(
            food.position.x - getRealSize(food.foodWidth) / 2,
            food.position.y - getRealSize(food.foodWidth) / 2,
            food.position.x + getRealSize(food.foodWidth) / 2,
            food.position.y + getRealSize(food.foodWidth) / 2,
            foodPaint
        )
    }

    //画边框
    private fun drawBoundary(canvas: Canvas?, boundaryObject: BoundaryObject) {
        var boundaryPath = Path()
        boundaryPaint.strokeWidth = (getRealSize(boundaryObject.boundaryPadding)).toFloat()
        boundaryPath.moveTo(
            0f,
            getRealSize(boundaryObject.boundaryPadding).toFloat() / 2
        )
        boundaryPath.lineTo(
            width - getRealSize(boundaryObject.boundaryPadding).toFloat() / 2,
            getRealSize(boundaryObject.boundaryPadding).toFloat() / 2
        )
        boundaryPath.lineTo(
            width - getRealSize(boundaryObject.boundaryPadding).toFloat() / 2,
            height - getRealSize(boundaryObject.boundaryPadding).toFloat() / 2
        )
        boundaryPath.lineTo(
            getRealSize(boundaryObject.boundaryPadding).toFloat() / 2,
            height - getRealSize(boundaryObject.boundaryPadding).toFloat() / 2
        )
        boundaryPath.lineTo(
            getRealSize(boundaryObject.boundaryPadding).toFloat() / 2,
            getRealSize(boundaryObject.boundaryPadding).toFloat() / 2
        )
        canvas?.drawPath(boundaryPath, boundaryPaint)
    }

    fun getRealSize(scale: Int): Int {
        return scale * sizeUnit
    }

    override fun onDetachedFromWindow() {
        disposed()
        isRun = false
        isFirstInit = false
        gameObjects.clear()
        super.onDetachedFromWindow()
    }

    abstract fun start()

    abstract fun update()

    abstract fun disposed()

}