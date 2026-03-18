package com.netvalve.app

import android.net.VpnService
import android.content.Intent
import android.os.ParcelFileDescriptor

class NetValveVpnService : VpnService() {
    private var vpnInterface: ParcelFileDescriptor? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val builder = Builder()
        builder.setSession("NetValve")
        builder.addAddress("10.0.0.2", 24)
        builder.addDnsServer("8.8.8.8")
        builder.addRoute("0.0.0.0", 0)
        
        vpnInterface = builder.establish()
        return START_STICKY
    }

    override fun onDestroy() {
        vpnInterface?.close()
        super.onDestroy()
    }
}
