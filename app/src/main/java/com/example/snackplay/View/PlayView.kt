package com.example.snackplay.View

import android.content.Context
import android.graphics.*
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import com.example.snackplay.View.Controller.GameController
import com.example.snackplay.View.Controller.GameEvent
import com.example.snackplay.View.Model.BoundaryObject
import com.example.snackplay.View.Model.FoodObject
import com.example.snackplay.View.Model.SnackNode
import java.util.concurrent.ThreadLocalRandom

class PlayView @JvmOverloads constructor(
    context: Context,
    attributes: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    BaseView(context, attributes, defStyleAttr) {

    var snackHead = SnackNode(PointF(0f, 0f), PointF(0f, 0f), -1)

    var snackTail = snackHead

    var difficulty = 1

    var totalBounds = 0 //得分

    var growUpSize = 16 //成长大小

    var haveNotGrowUp = 0 //成长缓存 实际尺寸

    var endFun: ((GameEvent, Int) -> Unit)? = null  //游戏结束

    private var isEnd = false

    private var direction = -1 // 0 上 -1 下  1 左 2 右

    private val boundary = BoundaryObject()

    private lateinit var food: FoodObject

    var _baseSpeed = 60  //速度 60单位/秒

    var speed = 60 //速度 60单位/秒
    get() {
        return _baseSpeed * (1 +((difficulty - 1) / 2))
    }

    val controller = GameController()

    init {
        controller.registerClickFun { gameEvent ->
            when (gameEvent) {
                GameEvent.UP -> {
                    if (direction != 0 && direction != -1) {
                        direction = 0
                    }
                }
                GameEvent.DOWN -> {
                    if (direction != -1 && direction != 0) {
                        direction = -1
                    }
                }
                GameEvent.LEFT -> {
                    if (direction != 1 && direction != 2) {
                        direction = 1
                    }
                }
                GameEvent.RIGHT -> {
                    if (direction != 2 && direction != 1) {
                        direction = 2
                    }
                }
                GameEvent.RESTART -> {
                    restart()
                }
                else -> {}
            }
        }
    }

    override fun start() {
        food = FoodObject( PointF((0.5 * width).toFloat(), (0.5 * height).toFloat()))
        snackHead.fromPoint.x =
            getRealSize(boundary.boundaryPadding) + (getRealSize(snackHead.snackNodeWidth) / 2).toFloat()
        snackHead.fromPoint.y =
            getRealSize(boundary.boundaryPadding) + (getRealSize(snackHead.snackNodeWidth) / 2).toFloat()
        snackHead.toPoint.x =
            getRealSize(boundary.boundaryPadding) + (getRealSize(snackHead.snackNodeWidth) / 2).toFloat()
        snackHead.toPoint.y =
            getRealSize(boundary.boundaryPadding) + (getRealSize(snackHead.snackNodeWidth) / 2).toFloat()
        gameObjects.add(food)
        gameObjects.add(snackHead)
        gameObjects.add(boundary)
    }

    override fun disposed() {

    }

    override fun update() {
        if (isEnd) {
            return
        }
        //生成前进矢量
        var realSpeed = getRealSize(speed) / fps
        var nextNodePoint: PointF =
            when (direction) {
                0 -> { //上
                    PointF(snackHead.toPoint.x, snackHead.toPoint.y - realSpeed)
                }
                -1 -> { //下
                    PointF(snackHead.toPoint.x, snackHead.toPoint.y + realSpeed)
                }
                1 -> { //左
                    PointF(snackHead.toPoint.x - realSpeed, snackHead.toPoint.y)
                }
                2 -> { //右
                    PointF(snackHead.toPoint.x + realSpeed, snackHead.toPoint.y)
                }
                else -> PointF(0f, 0f)
            }
        var endEvent: GameEvent? = null
        //是否碰到边
        if (isPositionOutBoundarySnack(nextNodePoint.x, nextNodePoint.y)) {
            endEvent = GameEvent.TOUCH_BOUNDARY
        }
        //是否碰到自身
        if (isEatSelf(nextNodePoint, snackHead.next?.next)) {
            endEvent = GameEvent.EAT_SELF
        }
        var isEatFood = false
        //是否吃到果子
        if (isEatFood(nextNodePoint)) {
            isEatFood = true
            totalBounds++
            haveNotGrowUp += getRealSize(growUpSize)
        }
        //前进处理
        if (isChangeDirection()) { //是否转向
            var newNode = SnackNode(
                snackHead.toPoint,
                toPoint = nextNodePoint,
                next = snackHead,
                direction = direction
            )
            snackHead.prev = newNode
            //无法强引用，又要保持顺序，操作繁琐
            gameObjects.remove(snackHead)
            snackHead = newNode
            gameObjects.add(snackHead)
        } else {
            snackHead.toPoint = nextNodePoint
        }
        //处理尾部
        var tailForward = realSpeed
        var isNeedChangeTail = true
        //存在未长的部分
        if (haveNotGrowUp!=0) {
            haveNotGrowUp -= realSpeed
            if (haveNotGrowUp < 0) {
                tailForward = Math.abs(haveNotGrowUp)
                haveNotGrowUp = 0
            } else {
                isNeedChangeTail = false
            }
        }
        if (isNeedChangeTail) {
            changeSnackTail(tailForward.toFloat())
        }
        //生成新食物
        if (isEatFood) {
            gameObjects.remove(food)
            food = FoodObject(generateNewFood())
            gameObjects.add(food)
        }
        //处理game over
        endEvent?.let {
            dispatchGameOver(endEvent)
        }
    }


    //是否在边缘
    private fun isPositionOutBoundarySnack(dx: Float, dy: Float): Boolean {
        var res = false
        if (dx < getRealSize(boundary.boundaryPadding) + getRealSize(snackHead.snackNodeWidth) / 2 || dx > (width - getRealSize(
                boundary.boundaryPadding
            ) - getRealSize(snackHead.snackNodeWidth) / 2)
        ) {
            res = true
        }
        if (dy < getRealSize(boundary.boundaryPadding) + getRealSize(snackHead.snackNodeWidth) / 2 || dy > (height - getRealSize(
                boundary.boundaryPadding
            ) - getRealSize(snackHead.snackNodeWidth) / 2)
        ) {
            res = true
        }
        return res
    }

    //是否转向
    private fun isChangeDirection(): Boolean {
        return snackHead.direction != direction
    }

    //生成新食物
    private fun generateNewFood(): PointF {
        var randomX = ThreadLocalRandom.current().nextInt(getRealSize(boundary.boundaryPadding) + getRealSize(food.foodWidth) / 2, width - (getRealSize(boundary.boundaryPadding) + getRealSize(food.foodWidth) / 2))
        var randomY = ThreadLocalRandom.current().nextInt(getRealSize(boundary.boundaryPadding) + getRealSize(food.foodWidth) / 2, height - (getRealSize(boundary.boundaryPadding) + getRealSize(food.foodWidth) / 2))
        var randomPoint = PointF(randomX.toFloat(), randomY.toFloat())
        return if (isSnackTouchOneObject(randomPoint, getRealSize(food.foodWidth), snackHead)) {
            generateNewFood()
        } else {
            randomPoint
        }
    }

    //舍弃蛇尾
    private fun changeSnackTail(forward: Float) {
        var forwardDistance: Float = -1f
        when(snackTail.direction) {
            0 -> { //上
                snackTail.fromPoint.y = snackTail.fromPoint.y - forward
                if (snackTail.fromPoint.y < snackTail.toPoint.y) {
                    forwardDistance = snackTail.toPoint.y - snackTail.fromPoint.y
                }
            }
            -1 -> { //下
                snackTail.fromPoint.y = snackTail.fromPoint.y + forward
                if (snackTail.fromPoint.y > snackTail.toPoint.y) {
                    forwardDistance = snackTail.fromPoint.y - snackTail.toPoint.y
                }
            }
            1 -> { //左
                snackTail.fromPoint.x = snackTail.fromPoint.x - forward
                if (snackTail.fromPoint.x < snackTail.toPoint.x) {
                    forwardDistance = snackTail.toPoint.x - snackTail.fromPoint.x
                }
            }
            2 -> { //右
                snackTail.fromPoint.x = snackTail.fromPoint.x + forward
                if (snackTail.fromPoint.x > snackTail.toPoint.x) {
                    forwardDistance = snackTail.fromPoint.x - snackTail.toPoint.x
                }
            }
        }
        if (forwardDistance > 0) {
            if (snackTail.prev != null) {
                snackTail = snackTail.prev!!
                snackTail.next = null
            }
            changeSnackTail(forwardDistance)
        }
    }

    private fun isEatFood(forwardPointF: PointF): Boolean {
        var foodRect = Rect((food.position.x - getRealSize(food.foodWidth) / 2).toInt(),(food.position.y - getRealSize(food.foodWidth) / 2).toInt(),(food.position.x + getRealSize(food.foodWidth) / 2).toInt(),(food.position.y + getRealSize(food.foodWidth) / 2).toInt())
        var forwardRect = Rect((forwardPointF.x - getRealSize(snackHead.snackNodeWidth)).toInt(),(forwardPointF.y - getRealSize(snackHead.snackNodeWidth)).toInt(),(forwardPointF.x + getRealSize(snackHead.snackNodeWidth)).toInt(),(forwardPointF.y + getRealSize(snackHead.snackNodeWidth)).toInt())
        return isRectOverlayRect(foodRect, forwardRect)
    }

    private fun isEatSelf(toPointF: PointF, snackNode: SnackNode?): Boolean {
        if (snackNode == null) {
            return false
        }
        var snackRect = snackNode.getRect(snackHead.snackNodeWidth)
        var snackWidth = getRealSize(snackHead.snackNodeWidth)
        var pA = PointF()
        var pB = PointF()
        when(direction) {
            0 -> { //上
                pA = PointF(toPointF.x - snackWidth / 2, toPointF.y)
                pB = PointF(toPointF.x + snackWidth / 2, toPointF.y)
            }
            -1 -> { //下
                pA = PointF(toPointF.x - snackWidth / 2, toPointF.y)
                pB = PointF(toPointF.x + snackWidth / 2, toPointF.y)
            }
            1 -> { //左
                pA = PointF(toPointF.x, toPointF.y- snackWidth / 2)
                pB = PointF(toPointF.x, toPointF.y + snackWidth / 2)
            }
            2 -> { //右
                pA = PointF(toPointF.x, toPointF.y- snackWidth / 2)
                pB = PointF(toPointF.x, toPointF.y + snackWidth / 2)
            }
        }
        var res = false
        if ((pA.x > snackRect.left && pA.x < snackRect.right) && (pA.y > snackRect.top && pA.y < snackRect.bottom)) {
            res = true
        }
        if ((pB.x > snackRect.left && pB.x < snackRect.right) && (pB.y > snackRect.top && pB.y < snackRect.bottom)) {
            res = true
        }
        if (res) {
            return true
        } else {
            return isEatSelf(toPointF, snackNode.next)
        }
    }

    //长蛇是否碰触某一物体
    private fun isSnackTouchOneObject(toPointF: PointF, width: Int, snackNode: SnackNode?): Boolean {
        if (snackNode == null) {
            return false
        }
        var newPointRect = Rect((toPointF.x - width / 2).toInt(),(toPointF.y - width / 2).toInt(),(toPointF.x + width / 2).toInt(),(toPointF.y + width / 2).toInt())
        var isOverlay = isRectOverlayRect(newPointRect, snackNode.getRect(snackHead.snackNodeWidth))
        return if (isOverlay) {
            true
        } else {
            isSnackTouchOneObject(toPointF, width, snackNode.next)
        }
    }

    private fun isRectOverlayRect(r1: Rect, r2: Rect): Boolean {
        var leftPole = Math.min(r1.left, r2.left)
        var rightPole = Math.max(r1.right, r2.right)
        var topPole = Math.min(r1.top, r2.top)
        var bottomPole = Math.max(r1.bottom, r2.bottom)
        var scopeWidth = rightPole - leftPole
        var scopeHeight = bottomPole - topPole
        return (r1.width() + r2.width() > scopeWidth) && (r1.height() + r2.height() > scopeHeight)
    }

    private fun dispatchGameOver(event: GameEvent) {
        isEnd = true
        Handler(Looper.getMainLooper()).post {
            endFun?.invoke(event, totalBounds)
        }
    }

    private fun restart() {
        food.position.x = (0.5 * width).toFloat()
        food.position.y = (0.5 * height).toFloat()
        snackHead.fromPoint.x =
            getRealSize(boundary.boundaryPadding) + (getRealSize(snackHead.snackNodeWidth) / 2).toFloat()
        snackHead.fromPoint.y =
            getRealSize(boundary.boundaryPadding) + (getRealSize(snackHead.snackNodeWidth) / 2).toFloat()
        snackHead.toPoint.x =
            getRealSize(boundary.boundaryPadding) + (getRealSize(snackHead.snackNodeWidth) / 2).toFloat()
        snackHead.toPoint.y =
            getRealSize(boundary.boundaryPadding) + (getRealSize(snackHead.snackNodeWidth) / 2).toFloat()
        snackHead.next = null
        snackTail = snackHead
        direction = -1
        totalBounds = 0
        haveNotGrowUp = 0
        //最后更改标志，避免线程问题
        isEnd = false
    }


    fun dp2px(dp: Float): Float {
        var fontScale = context.resources.displayMetrics.scaledDensity
        return (dp / fontScale + 0.5f)
    }
}