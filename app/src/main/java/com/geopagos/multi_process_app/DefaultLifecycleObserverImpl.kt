package com.geopagos.multi_process_app

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

class DefaultLifecycleObserverImpl(
    val sendState: (String) -> Unit,
) : DefaultLifecycleObserver {
    override fun onCreate(owner: LifecycleOwner) = sendState("CREATED")
    override fun onStart(owner: LifecycleOwner) = sendState("STARTED")
    override fun onResume(owner: LifecycleOwner) = sendState("RESUMED")
    override fun onPause(owner: LifecycleOwner) = sendState("PAUSED")
    override fun onStop(owner: LifecycleOwner) = sendState("STOPPED")
    override fun onDestroy(owner: LifecycleOwner) = sendState("DESTROYED")
}