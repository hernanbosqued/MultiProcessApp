package com.geopagos.multi_process_app

import android.os.Bundle

interface ServiceEvents{
    fun startService()
    fun stopService()
    fun logReceived(bundle: Bundle)
    fun serviceConnected()
    fun serviceDisconnected()
}