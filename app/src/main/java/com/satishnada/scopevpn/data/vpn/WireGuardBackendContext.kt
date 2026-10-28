package com.satishnada.scopevpn.data.vpn

import android.content.ComponentName
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import com.wireguard.android.backend.GoBackend

/**
 * GoBackend starts its bundled VpnService class directly. This context redirects that explicit
 * request to our subclass, which adds the foreground notification required for a long-lived VPN.
 */
internal class WireGuardBackendContext(context: Context) :
    ContextWrapper(context.applicationContext) {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun startService(service: Intent): ComponentName? {
        if (service.component?.className != GoBackend.VpnService::class.java.name) {
            return super.startService(service)
        }

        val redirectedIntent = Intent(service).setClass(
            this,
            ScopeWireGuardVpnService::class.java,
        )
        return baseContext.startForegroundService(redirectedIntent)
    }
}
