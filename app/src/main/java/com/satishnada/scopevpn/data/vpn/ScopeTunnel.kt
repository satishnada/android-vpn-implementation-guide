package com.satishnada.scopevpn.data.vpn

import com.wireguard.android.backend.Tunnel

internal class ScopeTunnel(
    private val tunnelName: String = "scopevpn",
) : Tunnel {
    override fun getName(): String = tunnelName

    override fun onStateChange(newState: Tunnel.State) = Unit
}
