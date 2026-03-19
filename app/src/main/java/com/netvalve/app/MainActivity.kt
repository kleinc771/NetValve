package com.netvalve.app

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.VpnService
import android.os.Bundle
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat

class MainActivity : AppCompatActivity() {
    private var isVpnActive = false
    private var selectedPackage: String? = null
    private var currentDelay: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val valveSwitch = findViewById<SwitchCompat>(R.id.valve_switch)
        val speedSlider = findViewById<SeekBar>(R.id.speed_slider)
        val speedText = findViewById<TextView>(R.id.speed_text)
        val selectAppBtn = findViewById<Button>(R.id.select_app_btn)
        val selectedAppText = findViewById<TextView>(R.id.selected_app_text)

        selectAppBtn.setOnClickListener {
            showAppPicker(selectedAppText)
        }

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
                currentDelay = (100 - progress).toLong()
                if (isVpnActive) {
                    updateValveService()
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun showAppPicker(textView: TextView) {
        val pm = packageManager
        val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        val appNames = packages.map { it.loadLabel(pm).toString() }.toTypedArray()
        val packageNames = packages.map { it.packageName }.toTypedArray()

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select Target App")
        builder.setItems(appNames) { _, which ->
            selectedPackage = packageNames[which]
            textView.text = "Target: ${appNames[which]}"
            if (isVpnActive) {
                // Restart service with new app
                stopValveService()
                startValveService()
            }
        }
        builder.show()
    }

    private fun updateValveService() {
        val intent = Intent(this, NetValveService::class.java)
        intent.putExtra("DELAY", currentDelay)
        intent.putExtra("ALLOWED_APP", selectedPackage)
        startService(intent)
    }

    private fun startValveService() {
        isVpnActive = true
        updateValveService()
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
            startValveService()
        }
    }
}
