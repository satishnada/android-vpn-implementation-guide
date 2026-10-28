package com.satishnada.scopevpn.ui.home

import com.satishnada.scopevpn.domain.model.AppRoutingPolicy
import com.satishnada.scopevpn.domain.model.InstalledApp
import com.satishnada.scopevpn.domain.model.TunnelProfile
import com.satishnada.scopevpn.domain.model.TunnelStatistics
import com.satishnada.scopevpn.domain.model.VpnConnectionState

data class HomeUiState(
    val connectionState: VpnConnectionState = VpnConnectionState.DISCONNECTED,
    val profile: TunnelProfile? = null,
    val routingPolicy: AppRoutingPolicy = AppRoutingPolicy(),
    val activePolicy: AppRoutingPolicy? = null,
    val installedApps: List<InstalledApp> = emptyList(),
    val statistics: TunnelStatistics = TunnelStatistics(),
    val connectedAtElapsedRealtime: Long? = null,
    val backendVersion: String = "WireGuard userspace",
    val isLoadingApps: Boolean = true,
    val isFilterSheetVisible: Boolean = false,
    val message: String? = null,
) {
    val reconnectRequired: Boolean
        get() = connectionState.isConnected && activePolicy != routingPolicy

    val canImportConfig: Boolean
        get() = !connectionState.isConnected && !connectionState.isBusy
}
