package com.geopagos.multi_process_app

import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

const val MSG_START = 0
const val MSG_LOG = 1
const val MSG_STATE = 2
const val MSG_STOP = 3

class MyService : Service() {

    private val incomingHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_START -> sendLog(msg.replyTo, Color.Black.toArgb(), android.os.Process.myPid().toString(), "Connected")
                MSG_STATE -> sendLog(
                    msg.replyTo, msg.data.getInt("color"),
                    android.os.Process.myPid().toString() + "->" + msg.data.getString("owner").orEmpty(),
                    msg.data.getString("data").orEmpty()
                )

                MSG_STOP -> {
                    sendLog(msg.replyTo, Color.Black.toArgb(), android.os.Process.myPid().toString(), "Disconnected")
                    android.os.Process.killProcess(android.os.Process.myPid())
                }

                else -> super.handleMessage(msg)
            }
        }
    }

    private fun sendLog(messenger: Messenger, color: Int, tag: String, message: String) {
        messenger.send(
            Message().apply {
                what = MSG_LOG
                data = Bundle().apply {
                    putInt("color", color)
                    putString("log", "$tag->$message")
                }
            })
    }

    override fun onBind(intent: Intent): IBinder? {
        return Messenger(incomingHandler).binder
    }

//    override fun onCreate() {
//        super.onCreate()
//        //android.os.Debug.waitForDebugger()
//    }
}