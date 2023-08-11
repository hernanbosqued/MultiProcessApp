package com.geopagos.multi_process_app

import android.os.Handler
import android.os.Looper
import android.os.Message

class HandlerImpl(private val serviceEvents: ServiceEvents): Handler(Looper.getMainLooper()) {
    override fun handleMessage(msg: Message) {
        when (msg.what) {
            MSG_COUNTER -> serviceEvents.counterReceived(msg.data)
            MSG_STATE -> serviceEvents.stateReceived(msg.data)
            else -> super.handleMessage(msg)
        }
    }
}