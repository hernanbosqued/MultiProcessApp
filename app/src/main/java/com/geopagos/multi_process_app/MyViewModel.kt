package com.geopagos.multi_process_app

import android.os.Bundle
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MyViewModel : ViewModel(), ServiceEvents {
    private val data: MutableList<Pair<Color, String>> = mutableListOf()

    private val log = MutableLiveData<List<Pair<Color, String>>>()
    fun log(): LiveData<List<Pair<Color, String>>> = log

    private val messages = MutableLiveData<Pair<Int, Bundle>>()
    fun messages(): LiveData<Pair<Int, Bundle>> = messages

    override fun counterReceived(bundle: Bundle) {
        val counter = bundle.getString("log").orEmpty()
        val pid = bundle.getString("pid").orEmpty()
        log(Color.Gray, "$pid->$counter")
    }

    override fun stateReceived(bundle: Bundle) {
        val color = bundle.getInt("color")
        val message = bundle.getString("log").orEmpty()
        log(Color(color), message)
    }

    override fun serviceStarted() {
        sendMessage(MSG_START)
        log(Color.Black, "Connected")
    }

    override fun serviceDisconnected() {
        log(Color.Black, "Disconnected")
    }

    fun sendState(owner: String, color: Color, state: String) = sendMessage(MSG_STATE, Bundle().apply {
        putString("owner", owner)
        putInt("color", color.toArgb())
        putString("data", state)
    })

    private fun sendMessage(what: Int, bundle: Bundle = Bundle()) {
        messages.value = what to bundle
    }

    private fun log(color: Color, message: String) = data.add(color to message).also { log.value = data.reversed() }
}