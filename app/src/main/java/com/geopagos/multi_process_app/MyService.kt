package com.geopagos.multi_process_app

import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

const val MSG_START = 0
const val MSG_COUNTER = 1

class MyService : Service() {

    private var counter = 0
    private val hash = this.hashCode().toString()
    private val job = Job()

    private val incomingHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_START -> createCounter(msg.replyTo)
                else -> super.handleMessage(msg)
            }
        }
    }

    private fun createCounter(messenger: Messenger) {
        CoroutineScope(Dispatchers.IO + job).launch {
            while (this.isActive) {
                messenger.send(
                    Message().apply {
                        what = MSG_COUNTER
                        data = Bundle().apply {
                            putString("log", (counter++).toString())
                            putString("pid", android.os.Process.myPid().toString())
                        }
                    })

                delay(1000)
            }
        }
    }

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