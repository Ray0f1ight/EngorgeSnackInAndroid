package com.example.snackplay

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var et = findViewById<EditText>(R.id.et_diff)
        findViewById<Button>(R.id.tvStart).setOnClickListener {
            startActivity(Intent(this, GameActivity::class.java).also {
                if (!et.text?.toString().isNullOrEmpty()){
                    var diff: Int = try {
                        et.text.toString().toInt()
                    } catch (e: Exception) {
                        1
                    }
                    if (diff < 1) {
                        diff = 1
                    }
                    it.putExtra("difficult", diff)
                }
            })
        }
    }
}