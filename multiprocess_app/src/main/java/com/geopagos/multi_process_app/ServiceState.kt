package com.geopagos.multi_process_app

sealed class ServiceState {
    object START : ServiceState()
    object STOP : ServiceState()
}