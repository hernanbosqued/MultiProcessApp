package com.geopagos.multi_process_app

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

class DefaultLifecycleObserverImpl(
    private val owner: String,
    private val color: Color,
    private val sendState: (String, Color, String) -> Unit,
) : DefaultLifecycleObserver {
    override fun onCreate(owner: LifecycleOwner) = sendState(this.owner, color, "CREATED")
    override fun onStart(owner: LifecycleOwner) = sendState(this.owner, color, "STARTED")
    override fun onResume(owner: LifecycleOwner) = sendState(this.owner, color, "RESUMED")
    override fun onPause(owner: LifecycleOwner) = sendState(this.owner, color, "PAUSED")
    override fun onStop(owner: LifecycleOwner) = sendState(this.owner, color, "STOPPED")
    override fun onDestroy(owner: LifecycleOwner) = sendState(this.owner, color, "DESTROYED")
}