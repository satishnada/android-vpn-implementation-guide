package com.satishnada.scopevpn.domain.model

data class TunnelProfile(
    val name: String,
    val endpoint: String?,
    val addressSummary: String,
    val peerCount: Int,
)

data class TunnelStatistics(
    val receivedBytes: Long = 0L,
    val transmittedBytes: Long = 0L,
)

enum class VpnConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    DISCONNECTING,
    ERROR,
    ;

    val isBusy: Boolean
        get() = this == CONNECTING || this == DISCONNECTING

    val isConnected: Boolean
        get() = this == CONNECTED
}
