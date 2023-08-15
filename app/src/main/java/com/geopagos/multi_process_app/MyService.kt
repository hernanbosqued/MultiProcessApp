package com.geopagos.multi_process_app

import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import kotlinx.coroutines.Job

const val MSG_START = 0
const val MSG_COUNTER = 1
const val MSG_STATE = 2

class MyService : Service() {

//    private var counter = 0
    private val job = Job()

    private val incomingHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                //MSG_START -> createCounter(msg.replyTo)
                MSG_STATE -> {
                    msg.replyTo.send(
                        Message().apply {
                            what = MSG_STATE
                            data = Bundle().apply {
                                putInt("color", msg.data.getInt("color"))
                                putString("log", "${msg.data.getString("owner")}->${msg.data.getString("data")}")
                            }
                        })
                }
                else -> super.handleMessage(msg)
            }
        }
    }

//    private fun createCounter(messenger: Messenger) {
//        CoroutineScope(Dispatchers.IO + job).launch {
//            while (this.isActive) {
//                messenger.send(
//                    Message().apply {
//                        what = MSG_COUNTER
//                        data = Bundle().apply {
//                            putString("log", (counter++).toString())
//                            putString("pid", android.os.Process.myPid().toString())
//                        }
//                    })
//
//                delay(1000)
//            }
//        }
//    }

    override fun onBind(intent: Intent): IBinder? {
        return Messenger(incomingHandler).binder
    }

//    override fun onCreate() {
//        super.onCreate()
//        //android.os.Debug.waitForDebugger()
//    }

    override fun onDestroy() {
        job.cancel()
        super.onDestroy()
    }
}