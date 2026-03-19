package com.netvalve.app

import android.os.Bundle
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val valveSwitch = findViewById<SwitchCompat>(R.id.valve_switch)
        val speedSlider = findViewById<SeekBar>(R.id.speed_slider)
        val speedText = findViewById<TextView>(R.id.speed_text)

        valveSwitch.setOnCheckedChangeListener { _, isChecked ->
            val status = if (isChecked) "VALVE OPEN: Flowing" else "VALVE CLOSED: Blocked"
            Toast.makeText(this, status, Toast.LENGTH_SHORT).show()
        }

        speedSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                speedText.text = "Speed: $progress%"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                Toast.makeText(this@MainActivity, "Flow set to ${seekBar?.progress}%", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
