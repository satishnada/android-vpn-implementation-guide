package com.satishnada.scopevpn

import android.content.Context
import com.satishnada.scopevpn.data.apps.InstalledAppsRepository
import com.satishnada.scopevpn.data.vpn.WireGuardTunnelController

class AppContainer(context: Context) {
    val installedAppsRepository = InstalledAppsRepository(context)
    val tunnelController = WireGuardTunnelController(context)
}
