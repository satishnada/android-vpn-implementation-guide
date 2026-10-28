package com.satishnada.scopevpn

import android.app.Application

class ScopeVpnApplication : Application() {
    val container: AppContainer by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        AppContainer(applicationContext)
    }
}
