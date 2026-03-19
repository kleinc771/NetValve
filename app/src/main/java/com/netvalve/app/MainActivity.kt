package com.netvalve.app

import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat

class MainActivity : AppCompatActivity() {
    private var isVpnActive = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val valveSwitch = findViewById<SwitchCompat>(R.id.valve_switch)
        val speedSlider = findViewById<SeekBar>(R.id.speed_slider)
        val speedText = findViewById<TextView>(R.id.speed_text)

        valveSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                val intent = VpnService.prepare(this)
                if (intent != null) {
                    startActivityForResult(intent, 0)
                } else {
                    onActivityResult(0, RESULT_OK, null)
                }
            } else {
                stopValveService()
            }
        }

        speedSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                speedText.text = "Speed: $progress%"
                if (isVpnActive) {
                    // 100% speed = 0ms delay, 0% speed = 100ms delay
                    val delay = (100 - progress).toLong()
                    updateValveSpeed(delay)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun updateValveSpeed(delay: Long) {
        val intent = Intent(this, NetValveService::class.java)
        intent.putExtra("DELAY", delay)
        startService(intent)
    }

    private fun stopValveService() {
        isVpnActive = false
        val stopIntent = Intent(this, NetValveService::class.java)
        stopIntent.action = "STOP"
        startService(stopIntent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            isVpnActive = true
            val startIntent = Intent(this, NetValveService::class.java)
            startService(startIntent)
        }
    }
}
