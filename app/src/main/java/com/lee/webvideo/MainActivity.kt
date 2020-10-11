package com.lee.webvideo

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btn1.setOnClickListener {
            startActivity(Intent(this, WebVideoActivity::class.java))
        }
        btn2.setOnClickListener {
            startActivity(Intent(this, ExoVideoActivity::class.java))
        }
    }

}