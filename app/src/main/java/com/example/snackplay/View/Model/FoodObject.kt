package com.example.snackplay.View.Model

import android.graphics.PointF

data class FoodObject(
    var position: PointF,
    var foodWidth: Int = 10  //食物直径
) : GameObject() {


}