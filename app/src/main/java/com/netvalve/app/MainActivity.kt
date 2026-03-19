package com.netvalve.app

import android.os.Bundle
import android.widget.TextView
import android.view.Gravity
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // This creates a simple screen so we can see the app is alive!
        val textView = TextView(this)
        textView.text = "NetValve is Running!"
        textView.textSize = 24f
        textView.gravity = Gravity.CENTER
        
        setContentView(textView)
    }
}
