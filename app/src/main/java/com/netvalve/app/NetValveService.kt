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
    private var allowedPackage: String? = null

    companion object {
        var throttleDelay: Long = 0
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "STOP") {
            stopVpn()
            return START_NOT_STICKY
        }
        throttleDelay = intent?.getLongExtra("DELAY", 0) ?: 0
        allowedPackage = intent?.getStringExtra("ALLOWED_APP")
        startVpn()
        return START_STICKY
    }

    private fun startVpn() {
        if (vpnThread != null) return
        
        val builder = Builder()
            .setSession("NetValve")
            .addAddress("10.0.0.2", 24)
            .addDnsServer("8.8.8.8")
            .addRoute("0.0.0.0", 0)

        // --- THE TARGET LOGIC (FIXED) ---
        val pkg = allowedPackage
        if (!pkg.isNullOrEmpty()) {
            try {
                builder.addAllowedApplication(pkg)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        vpnInterface = builder.establish()
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
                    if (throttleDelay > 0) {
                        Thread.sleep(throttleDelay)
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
