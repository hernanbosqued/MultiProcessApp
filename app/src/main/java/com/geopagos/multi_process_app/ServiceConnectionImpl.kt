package com.geopagos.multi_process_app

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder

class ServiceConnectionImpl(private val serviceEvents: ServiceEvents) : ServiceConnection {
    var service: IBinder? = null

    override fun onServiceConnected(name: ComponentName, service: IBinder?) {
        this.service = service
        serviceEvents.serviceConnected()
    }

    override fun onServiceDisconnected(name: ComponentName) {
       this.service = null
       serviceEvents.serviceDisconnected()
    }
}
