package com.satishnada.scopevpn.data.vpn

import android.content.Context
import android.os.SystemClock
import com.satishnada.scopevpn.domain.model.AppRoutingPolicy
import com.satishnada.scopevpn.domain.model.TunnelProfile
import com.satishnada.scopevpn.domain.model.TunnelStatistics
import com.wireguard.android.backend.GoBackend
import com.wireguard.android.backend.Tunnel
import com.wireguard.config.Config
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream

class WireGuardTunnelController(context: Context) {
    private val backend = GoBackend(WireGuardBackendContext(context))
    private val tunnel = ScopeTunnel()
    private val stateLock = Any()

    private var baseConfig: Config? = null
    private var profile: TunnelProfile? = null
    private var activePolicy: AppRoutingPolicy? = null
    private var connectedAtElapsedRealtime: Long? = null

    fun importConfig(bytes: ByteArray, displayName: String): TunnelProfile {
        require(bytes.isNotEmpty()) { "The selected configuration is empty." }
        require(bytes.size <= MAX_CONFIG_BYTES) { "Configuration files must be smaller than 256 KB." }

        val parsed = ByteArrayInputStream(bytes).use { input -> Config.parse(input) }
        require(parsed.getPeers().isNotEmpty()) { "A client configuration needs at least one peer." }
        require(parsed.getPeers().any { it.getEndpoint().isPresent }) {
            "A mobile client configuration needs at least one peer endpoint."
        }
        require(parsed.getPeers().any { it.getAllowedIps().isNotEmpty() }) {
            "A client configuration needs at least one AllowedIPs route."
        }

        val importedProfile = TunnelProfile(
            name = displayName.removeSuffix(".conf").ifBlank { "Imported tunnel" },
            endpoint = parsed.getPeers()
                .firstNotNullOfOrNull { peer -> peer.getEndpoint().orElse(null)?.toString() },
            addressSummary = parsed.getInterface().getAddresses()
                .joinToString(limit = 2, truncated = "...") { it.toString() }
                .ifBlank { "Address supplied during tunnel setup" },
            peerCount = parsed.getPeers().size,
        )

        synchronized(stateLock) {
            check(backend.getState(tunnel) == Tunnel.State.DOWN) {
                "Disconnect before importing a different configuration."
            }
            baseConfig = parsed
            profile = importedProfile
            activePolicy = null
            connectedAtElapsedRealtime = null
        }
        return importedProfile
    }

    suspend fun connect(policy: AppRoutingPolicy): Tunnel.State = withContext(Dispatchers.IO) {
        val config = synchronized(stateLock) {
            requireNotNull(baseConfig) { "Import a WireGuard configuration first." }
        }.withRoutingPolicy(policy)

        val result = backend.setState(tunnel, Tunnel.State.UP, config)
        if (result == Tunnel.State.UP) {
            synchronized(stateLock) {
                activePolicy = policy
                connectedAtElapsedRealtime = SystemClock.elapsedRealtime()
            }
        }
        result
    }

    suspend fun disconnect(): Tunnel.State = withContext(Dispatchers.IO) {
        val result = backend.setState(tunnel, Tunnel.State.DOWN, null)
        synchronized(stateLock) {
            activePolicy = null
            connectedAtElapsedRealtime = null
        }
        result
    }

    suspend fun reconnect(policy: AppRoutingPolicy): Tunnel.State = withContext(Dispatchers.IO) {
        backend.setState(tunnel, Tunnel.State.DOWN, null)
        synchronized(stateLock) {
            activePolicy = null
            connectedAtElapsedRealtime = null
        }
        val config = synchronized(stateLock) {
            requireNotNull(baseConfig) { "Import a WireGuard configuration first." }
        }.withRoutingPolicy(policy)

        val result = backend.setState(tunnel, Tunnel.State.UP, config)
        synchronized(stateLock) {
            activePolicy = if (result == Tunnel.State.UP) policy else null
            connectedAtElapsedRealtime =
                if (result == Tunnel.State.UP) SystemClock.elapsedRealtime() else null
        }
        result
    }

    suspend fun statistics(): TunnelStatistics = withContext(Dispatchers.IO) {
        val statistics = backend.getStatistics(tunnel)
        TunnelStatistics(
            receivedBytes = statistics.totalRx(),
            transmittedBytes = statistics.totalTx(),
        )
    }

    fun currentState(): Tunnel.State = backend.getState(tunnel)

    fun backendVersion(): String = backend.getVersion()

    fun currentProfile(): TunnelProfile? = synchronized(stateLock) { profile }

    fun currentPolicy(): AppRoutingPolicy? = synchronized(stateLock) { activePolicy }

    fun connectedAtElapsedRealtime(): Long? =
        synchronized(stateLock) { connectedAtElapsedRealtime }

    private companion object {
        const val MAX_CONFIG_BYTES = 256 * 1024
    }
}
