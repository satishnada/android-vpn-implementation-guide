package com.satishnada.scopevpn.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.satishnada.scopevpn.AppContainer
import com.satishnada.scopevpn.data.apps.InstalledAppsRepository
import com.satishnada.scopevpn.data.vpn.WireGuardTunnelController
import com.satishnada.scopevpn.domain.model.AppRoutingPolicy
import com.satishnada.scopevpn.domain.model.InstalledApp
import com.satishnada.scopevpn.domain.model.RoutingMode
import com.satishnada.scopevpn.domain.model.TunnelStatistics
import com.satishnada.scopevpn.domain.model.VpnConnectionState
import com.satishnada.scopevpn.domain.validation.isValidAndroidPackageName
import com.wireguard.android.backend.Tunnel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class HomeViewModel(
    private val tunnelController: WireGuardTunnelController,
    private val installedAppsRepository: InstalledAppsRepository,
) : ViewModel() {
    private val operationMutex = Mutex()
    private var statisticsJob: Job? = null

    private val _uiState = MutableStateFlow(
        HomeUiState(
            backendVersion = runCatching(tunnelController::backendVersion)
                .getOrDefault("WireGuard userspace"),
        ),
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        restoreControllerState()
        loadInstalledApps()
    }

    fun importConfig(bytes: ByteArray, displayName: String) {
        viewModelScope.launch {
            if (!_uiState.value.canImportConfig) {
                showError("Disconnect before importing a different configuration.")
                return@launch
            }

            runCatching {
                withContext(Dispatchers.IO) {
                    tunnelController.importConfig(bytes, displayName)
                }
            }.onSuccess { profile ->
                _uiState.update {
                    it.copy(
                        profile = profile,
                        message = "Configuration imported. Private keys are kept in memory only.",
                    )
                }
            }.onFailure(::handleFailure)
        }
    }

    fun connect() {
        val validationError = _uiState.value.routingPolicy.validationError()
        if (validationError != null) {
            showError(validationError)
            return
        }
        if (_uiState.value.profile == null) {
            showError("Import a WireGuard client configuration first.")
            return
        }

        viewModelScope.launch {
            operationMutex.withLock {
                _uiState.update {
                    it.copy(connectionState = VpnConnectionState.CONNECTING, message = null)
                }
                runCatching {
                    tunnelController.connect(_uiState.value.routingPolicy)
                }.onSuccess { state ->
                    updateAfterTunnelState(state)
                }.onFailure(::handleFailure)
            }
        }
    }

    fun disconnect() {
        viewModelScope.launch {
            operationMutex.withLock {
                _uiState.update {
                    it.copy(connectionState = VpnConnectionState.DISCONNECTING, message = null)
                }
                runCatching { tunnelController.disconnect() }
                    .onSuccess { state -> updateAfterTunnelState(state) }
                    .onFailure(::handleFailure)
            }
        }
    }

    fun applyFiltersAndReconnect() {
        val validationError = _uiState.value.routingPolicy.validationError()
        if (validationError != null) {
            showError(validationError)
            return
        }

        viewModelScope.launch {
            operationMutex.withLock {
                _uiState.update {
                    it.copy(
                        connectionState = VpnConnectionState.CONNECTING,
                        isFilterSheetVisible = false,
                        message = "Re-establishing the tunnel with the new app filter...",
                    )
                }
                runCatching {
                    tunnelController.reconnect(_uiState.value.routingPolicy)
                }.onSuccess { state ->
                    updateAfterTunnelState(state)
                }.onFailure(::handleFailure)
            }
        }
    }

    fun setRoutingMode(mode: RoutingMode) {
        _uiState.update { state ->
            state.copy(routingPolicy = state.routingPolicy.withMode(mode))
        }
    }

    fun togglePackage(packageName: String) {
        _uiState.update { state ->
            state.copy(routingPolicy = state.routingPolicy.toggle(packageName))
        }
    }

    fun selectVisiblePackages(packageNames: Set<String>, selected: Boolean) {
        _uiState.update { state ->
            val updatedPackages = state.routingPolicy.selectedPackages.toMutableSet().apply {
                if (selected) addAll(packageNames) else removeAll(packageNames)
            }
            state.copy(
                routingPolicy = state.routingPolicy.copy(
                    selectedPackages = updatedPackages.toSet(),
                ),
            )
        }
    }

    fun addManualPackage(value: String) {
        val packageName = value.trim()
        if (!isValidAndroidPackageName(packageName)) {
            showError("Enter a valid Android package name, for example com.example.app.")
            return
        }

        _uiState.update { state ->
            val exists = state.installedApps.any { it.packageName == packageName }
            val apps = if (exists) {
                state.installedApps
            } else {
                state.installedApps.plus(
                    InstalledApp(
                        label = packageName.substringAfterLast('.'),
                        packageName = packageName,
                        isSystemApp = false,
                        isManualEntry = true,
                    ),
                ).sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.label })
            }

            state.copy(
                installedApps = apps,
                routingPolicy = state.routingPolicy.copy(
                    selectedPackages = state.routingPolicy.selectedPackages + packageName,
                ),
                message = if (exists) {
                    "$packageName selected."
                } else {
                    "$packageName added as a manual entry."
                },
            )
        }
    }

    fun setFilterSheetVisible(visible: Boolean) {
        _uiState.update { it.copy(isFilterSheetVisible = visible) }
    }

    fun onVpnPermissionDenied() {
        _uiState.update {
            it.copy(
                connectionState = VpnConnectionState.DISCONNECTED,
                message = "Android VPN permission was not granted.",
            )
        }
    }

    fun showError(message: String) {
        _uiState.update { it.copy(message = message) }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }

    private fun restoreControllerState() {
        val backendState = runCatching(tunnelController::currentState)
            .getOrDefault(Tunnel.State.DOWN)
        val connectionState = backendState.toUiState()
        _uiState.update {
            it.copy(
                connectionState = connectionState,
                profile = tunnelController.currentProfile(),
                activePolicy = tunnelController.currentPolicy(),
                routingPolicy = tunnelController.currentPolicy() ?: it.routingPolicy,
                connectedAtElapsedRealtime = tunnelController.connectedAtElapsedRealtime(),
            )
        }
        if (connectionState == VpnConnectionState.CONNECTED) startStatisticsPolling()
    }

    private fun loadInstalledApps() {
        viewModelScope.launch {
            runCatching { installedAppsRepository.loadLaunchableApps() }
                .onSuccess { apps ->
                    _uiState.update { it.copy(installedApps = apps, isLoadingApps = false) }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoadingApps = false,
                            message = error.message ?: "Unable to load installed apps.",
                        )
                    }
                }
        }
    }

    private fun updateAfterTunnelState(state: Tunnel.State) {
        val uiConnectionState = state.toUiState()
        _uiState.update {
            it.copy(
                connectionState = uiConnectionState,
                activePolicy = tunnelController.currentPolicy(),
                connectedAtElapsedRealtime = tunnelController.connectedAtElapsedRealtime(),
                statistics = if (uiConnectionState == VpnConnectionState.CONNECTED) {
                    it.statistics
                } else {
                    TunnelStatistics()
                },
                message = if (uiConnectionState == VpnConnectionState.CONNECTED) {
                    "VPN connected with ${it.routingPolicy.summary.lowercase()}."
                } else {
                    "VPN disconnected."
                },
            )
        }

        if (uiConnectionState == VpnConnectionState.CONNECTED) {
            startStatisticsPolling()
        } else {
            statisticsJob?.cancel()
            statisticsJob = null
        }
    }

    private fun startStatisticsPolling() {
        statisticsJob?.cancel()
        statisticsJob = viewModelScope.launch {
            while (isActive && _uiState.value.connectionState == VpnConnectionState.CONNECTED) {
                runCatching { tunnelController.statistics() }
                    .onSuccess { stats -> _uiState.update { it.copy(statistics = stats) } }
                delay(STATISTICS_REFRESH_MS)
            }
        }
    }

    private fun handleFailure(error: Throwable) {
        val backendState = runCatching { tunnelController.currentState() }
            .getOrDefault(Tunnel.State.DOWN)
        val actualConnectionState = backendState.toUiState()

        statisticsJob?.cancel()
        statisticsJob = null
        _uiState.update {
            it.copy(
                connectionState = if (actualConnectionState.isConnected) {
                    actualConnectionState
                } else {
                    VpnConnectionState.ERROR
                },
                activePolicy = tunnelController.currentPolicy(),
                connectedAtElapsedRealtime = tunnelController.connectedAtElapsedRealtime(),
                message = error.message ?: "The VPN operation failed.",
            )
        }

        if (actualConnectionState.isConnected) startStatisticsPolling()
    }

    class Factory(
        private val container: AppContainer,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(HomeViewModel::class.java))
            return HomeViewModel(
                tunnelController = container.tunnelController,
                installedAppsRepository = container.installedAppsRepository,
            ) as T
        }
    }

    private companion object {
        const val STATISTICS_REFRESH_MS = 1_000L
    }
}

private fun Tunnel.State.toUiState(): VpnConnectionState = when (this) {
    Tunnel.State.UP -> VpnConnectionState.CONNECTED
    Tunnel.State.DOWN -> VpnConnectionState.DISCONNECTED
    Tunnel.State.TOGGLE -> VpnConnectionState.DISCONNECTED
}
