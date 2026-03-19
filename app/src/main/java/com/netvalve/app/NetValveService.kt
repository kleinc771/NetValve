package com.netvalve.app

import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer

class NetValveService : VpnService(), Runnable {
    private var vpnInterface: ParcelFileDescriptor? = null
    private var vpnThread: Thread? = null
    companion object {
        var throttleDelay: Long = 0 // The "Valve" tightness
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "STOP") {
            stopVpn()
            return START_NOT_STICKY
        }
        throttleDelay = intent?.getLongExtra("DELAY", 0) ?: 0
        startVpn()
        return START_STICKY
    }

    private fun startVpn() {
        if (vpnThread != null) return
        
        vpnInterface = Builder()
            .setSession("NetValve")
            .addAddress("10.0.0.2", 24)
            .addDnsServer("8.8.8.8")
            .addRoute("0.0.0.0", 0)
            .establish()

        vpnThread = Thread(this, "NetValveThread")
        vpnThread?.start()
    }

    override fun run() {
        try {
            val input = FileInputStream(vpnInterface?.fileDescriptor)
            val output = FileOutputStream(vpnInterface?.fileDescriptor)
            val buffer = ByteBuffer.allocate(32767)

            while (!Thread.interrupted()) {
                val length = input.read(buffer.array())
                if (length > 0) {
                    // --- THE VALVE LOGIC ---
                    if (throttleDelay > 0) {
                        Thread.sleep(throttleDelay) // Hold the packet!
                    }
                    output.write(buffer.array(), 0, length)
                }
                buffer.clear()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            stopVpn()
        }
    }

    private fun stopVpn() {
        vpnThread?.interrupt()
        vpnThread = null
        vpnInterface?.close()
        vpnInterface = null
        stopSelf()
    }

    override fun onDestroy() {
        stopVpn()
        super.onDestroy()
    }
}
