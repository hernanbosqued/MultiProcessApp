package com.geopagos.multi_process_app

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MyViewModel : ViewModel(), ServiceEvents {
    private val data: MutableList<String> = mutableListOf()

    private val log = MutableLiveData<List<String>>()
    fun log(): LiveData<List<String>> = log

    private val messages = MutableLiveData<Pair<Int, Bundle>>()
    fun messages(): LiveData<Pair<Int, Bundle>> = messages

    override fun counterReceived(bundle: Bundle){
        val counter = bundle.getString("log").orEmpty()
        val pid = bundle.getString("pid").orEmpty()
        log("$pid->$counter")
    }

    override fun stateReceived(bundle: Bundle) {
        val message = bundle.getString("log").orEmpty()
        log(message)
    }

    override fun serviceStarted(){
        sendMessage(MSG_START)
        log("Connected")
    }

    override fun serviceDisconnected(){
        log("Connected")
    }

    fun sendState(state: String) = sendMessage(MSG_STATE, state)

    private fun sendMessage(what: Int, data: String? = null) {
        messages.value = what to Bundle().apply {
            data?.let { putString("data", it) }
        }
    }

    private fun log(message: String) = data.add(message).also { log.value = data.reversed() }
}