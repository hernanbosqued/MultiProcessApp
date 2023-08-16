package com.geopagos.multi_process_app

import android.os.Bundle
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MyViewModel : ViewModel(), ServiceEvents {
    private val data: MutableList<Pair<Color, String>> = mutableListOf()

    private val _log = MutableLiveData<List<Pair<Color, String>>>()
    val log: LiveData<List<Pair<Color, String>>> = _log

    private val _messages = MutableLiveData<Pair<Int, Bundle>>()
    val messages: LiveData<Pair<Int, Bundle>> = _messages

    private val _startService = MutableLiveData<Boolean>()
    val startService: LiveData<Boolean> = _startService

    private val _stopService = MutableLiveData<Boolean>()
    val stopService: LiveData<Boolean> = _stopService

    override fun logReceived(bundle: Bundle) {
        val color = bundle.getInt("color")
        val message = bundle.getString("log").orEmpty()
        log(Color(color), message)
    }

    override fun startService() {
        _startService.value = true
        _stopService.value = false
    }

    override fun serviceConnected() {
        sendMessage(MSG_START)
    }

    override fun stopService() {
        sendMessage(MSG_STOP)
    }

    override fun serviceDisconnected() {
        _stopService.value = true
        _startService.value = false
    }

    fun sendState(owner: String, color: Color, state: String) = sendMessage(MSG_STATE, Bundle().apply {
        putString("owner", owner)
        putInt("color", color.toArgb())
        putString("data", state)
    })

    private fun sendMessage(what: Int, bundle: Bundle = Bundle()) {
        _messages.value = what to bundle
    }

    private fun log(color: Color, message: String) = data.add(color to message).also { _log.value = data.reversed() }
}