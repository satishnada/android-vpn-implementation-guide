package com.satishnada.scopevpn.ui.home

import android.os.SystemClock
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.FileOpen
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.satishnada.scopevpn.domain.model.RoutingMode
import com.satishnada.scopevpn.domain.model.VpnConnectionState
import kotlinx.coroutines.delay
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onImportConfig: () -> Unit,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    onOpenFilters: () -> Unit,
    onDismissFilters: () -> Unit,
    onRoutingModeChanged: (RoutingMode) -> Unit,
    onPackageToggled: (String) -> Unit,
    onSelectVisiblePackages: (Set<String>, Boolean) -> Unit,
    onAddManualPackage: (String) -> Unit,
    onApplyAndReconnect: () -> Unit,
    onMessageShown: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.message) {
        val message = uiState.message ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        onMessageShown()
    }

    if (uiState.isFilterSheetVisible) {
        AppFilterSheet(
            policy = uiState.routingPolicy,
            apps = uiState.installedApps,
            isLoadingApps = uiState.isLoadingApps,
            reconnectRequired = uiState.reconnectRequired,
            onDismiss = onDismissFilters,
            onModeChanged = onRoutingModeChanged,
            onPackageToggled = onPackageToggled,
            onSelectVisiblePackages = onSelectVisiblePackages,
            onAddManualPackage = onAddManualPackage,
            onApplyAndReconnect = onApplyAndReconnect,
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "ScopeVPN",
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "Real WireGuard tunnel demo",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 8.dp,
                bottom = 40.dp,
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item {
                ConnectionHero(
                    uiState = uiState,
                    onConnect = onConnect,
                    onDisconnect = onDisconnect,
                )
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    TransferCard(
                        title = "Downloaded",
                        value = formatBytes(uiState.statistics.receivedBytes),
                        icon = { Icon(Icons.Rounded.Download, contentDescription = null) },
                        modifier = Modifier.weight(1f),
                    )
                    TransferCard(
                        title = "Uploaded",
                        value = formatBytes(uiState.statistics.transmittedBytes),
                        icon = { Icon(Icons.Rounded.Upload, contentDescription = null) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                ProfileCard(uiState)
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
                RoutingCard(uiState, onOpenFilters)
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedButton(
                        onClick = onImportConfig,
                        enabled = uiState.canImportConfig,
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.Rounded.FileOpen, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Import .conf")
                    }
                    FilledTonalButton(
                        onClick = onOpenFilters,
                        enabled = !uiState.connectionState.isBusy,
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.Rounded.Apps, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("App filter")
                    }
                }
            }

            if (uiState.reconnectRequired) {
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onApplyAndReconnect,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Rounded.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Apply app filter and reconnect")
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = "Configuration secrets are not written to disk by this demo. " +
                        "Use a server-issued client profile and never commit private keys.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun ConnectionHero(
    uiState: HomeUiState,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
) {
    var now by remember { mutableLongStateOf(SystemClock.elapsedRealtime()) }
    LaunchedEffect(uiState.connectedAtElapsedRealtime, uiState.connectionState) {
        while (uiState.connectionState == VpnConnectionState.CONNECTED) {
            now = SystemClock.elapsedRealtime()
            delay(1_000L)
        }
    }

    val title = when (uiState.connectionState) {
        VpnConnectionState.DISCONNECTED -> "Ready to connect"
        VpnConnectionState.CONNECTING -> "Establishing secure tunnel"
        VpnConnectionState.CONNECTED -> "Connection protected"
        VpnConnectionState.DISCONNECTING -> "Closing tunnel"
        VpnConnectionState.ERROR -> "Connection needs attention"
    }
    val subtitle = when (uiState.connectionState) {
        VpnConnectionState.CONNECTED -> {
            val started = uiState.connectedAtElapsedRealtime
            if (started == null) "Connected" else "Connected for ${formatDuration(now - started)}"
        }
        VpnConnectionState.CONNECTING -> "Authorizing interface, routes, DNS and peer"
        VpnConnectionState.DISCONNECTING -> "Returning traffic to the system network"
        VpnConnectionState.ERROR -> "Review the message, profile and endpoint"
        VpnConnectionState.DISCONNECTED -> uiState.profile?.let {
            "${it.name} is loaded"
        } ?: "Import a WireGuard client profile to begin"
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        BroadcastingVpnOrb(
            connectionState = uiState.connectionState,
            enabled = uiState.profile != null || uiState.connectionState == VpnConnectionState.CONNECTED,
            onClick = if (uiState.connectionState == VpnConnectionState.CONNECTED) {
                onDisconnect
            } else {
                onConnect
            },
        )
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(14.dp))
        Button(
            onClick = if (uiState.connectionState == VpnConnectionState.CONNECTED) {
                onDisconnect
            } else {
                onConnect
            },
            enabled = !uiState.connectionState.isBusy &&
                (uiState.profile != null || uiState.connectionState == VpnConnectionState.CONNECTED),
            colors = if (uiState.connectionState == VpnConnectionState.CONNECTED) {
                ButtonDefaults.filledTonalButtonColors()
            } else {
                ButtonDefaults.buttonColors()
            },
        ) {
            Icon(Icons.Rounded.Security, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (uiState.connectionState == VpnConnectionState.CONNECTED) "Disconnect" else "Connect")
        }
    }
}

@Composable
private fun TransferCard(
    title: String,
    value: String,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                icon()
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun ProfileCard(uiState: HomeUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "Tunnel profile",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(10.dp))
            val profile = uiState.profile
            if (profile == null) {
                Text(
                    text = "No configuration imported",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                DetailRow("Name", profile.name)
                DetailRow("Endpoint", profile.endpoint ?: "Not available")
                DetailRow("Address", profile.addressSummary)
                DetailRow("Peers", profile.peerCount.toString())
                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
                DetailRow("Backend", uiState.backendVersion)
            }
        }
    }
}

@Composable
private fun RoutingCard(
    uiState: HomeUiState,
    onOpenFilters: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        onClick = onOpenFilters,
        enabled = !uiState.connectionState.isBusy,
        colors = CardDefaults.cardColors(
            containerColor = if (uiState.reconnectRequired) {
                MaterialTheme.colorScheme.tertiaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainerLow
            },
        ),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Apps, contentDescription = null)
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Per-app routing",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = uiState.routingPolicy.summary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (uiState.reconnectRequired) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Filter changed. Reconnect to apply it to Android's VPN interface.",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                )
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(82.dp),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
    }
}

private fun formatDuration(elapsedMillis: Long): String {
    val totalSeconds = (elapsedMillis.coerceAtLeast(0L) / 1_000L)
    val hours = totalSeconds / 3_600L
    val minutes = (totalSeconds % 3_600L) / 60L
    val seconds = totalSeconds % 60L
    return if (hours > 0) {
        String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }
}

private fun formatBytes(bytes: Long): String {
    val safeBytes = bytes.coerceAtLeast(0L)
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    var value = safeBytes.toDouble()
    var unitIndex = 0
    while (value >= 1_024.0 && unitIndex < units.lastIndex) {
        value /= 1_024.0
        unitIndex++
    }
    return if (unitIndex == 0) {
        "$safeBytes ${units[unitIndex]}"
    } else {
        String.format(Locale.US, "%.1f %s", value, units[unitIndex])
    }
}
