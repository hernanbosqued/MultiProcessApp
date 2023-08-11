package com.geopagos.multi_process_app

import android.os.Bundle

interface ServiceEvents{
    fun serviceStarted()
    fun serviceDisconnected()
    fun counterReceived(bundle: Bundle)
    fun stateReceived(bundle: Bundle)
}