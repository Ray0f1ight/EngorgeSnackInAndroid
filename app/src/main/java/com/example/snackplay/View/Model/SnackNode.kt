package com.example.snackplay.View.Model

import android.graphics.PointF
import android.graphics.Rect

data class SnackNode(
    var fromPoint: PointF,
    var toPoint: PointF,
    val direction: Int,   //方向 0 上 -1 下  1 左 2 右
    var next: SnackNode? = null,
    var prev: SnackNode? = null,
    var snackNodeWidth: Int = 8  //节点直径
) : GameObject() {

    //根据直径生成矩形 snackWidth为实际尺寸
    fun getRect(snackWidth: Int): Rect {
        var fromRect = Rect(
            (fromPoint.x - snackWidth / 2).toInt(),
            (fromPoint.y - snackWidth / 2).toInt(),
            (fromPoint.x + snackWidth / 2).toInt(),
            (fromPoint.y + snackWidth / 2).toInt()
        )
        var toRect = Rect(
            (toPoint.x - snackWidth / 2).toInt(),
            (toPoint.y - snackWidth / 2).toInt(),
            (toPoint.x + snackWidth / 2).toInt(),
            (toPoint.y + snackWidth / 2).toInt()
        )
        return Rect(
            Math.min(fromRect.left, toRect.left),
            Math.min(fromRect.top, toRect.top),
            Math.max(fromRect.right, toRect.right),
            Math.max(fromRect.bottom, toRect.bottom)
        )
    }
}
