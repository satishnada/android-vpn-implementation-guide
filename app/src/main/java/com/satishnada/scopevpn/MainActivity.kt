package com.satishnada.scopevpn

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.net.Uri
import android.net.VpnService
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.satishnada.scopevpn.ui.home.HomeScreen
import com.satishnada.scopevpn.ui.home.HomeViewModel
import com.satishnada.scopevpn.ui.theme.ScopeVpnTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream

class MainActivity : ComponentActivity() {
    private val viewModel: HomeViewModel by viewModels {
        HomeViewModel.Factory((application as ScopeVpnApplication).container)
    }

    private var pendingVpnAction = PendingVpnAction.CONNECT

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (!granted) {
            viewModel.showError(
                "Notification permission was not granted. The VPN can still run, but " +
                    "Android may limit how its ongoing notification is displayed.",
            )
        }
        launchVpnPermissionDialogIfNeeded()
    }

    private val vpnPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            runPendingVpnAction()
        } else {
            viewModel.onVpnPermissionDenied()
        }
    }

    private val configFileLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri == null) return@registerForActivityResult

        lifecycleScope.launch {
            val result = runCatching {
                withContext(Dispatchers.IO) {
                    val bytes = contentResolver.openInputStream(uri)?.use { input ->
                        input.readBytesWithLimit(MAX_CONFIG_IMPORT_BYTES)
                    } ?: error("Unable to read the selected file.")
                    bytes to queryDisplayName(uri)
                }
            }

            result.onSuccess { (bytes, displayName) ->
                viewModel.importConfig(bytes, displayName)
            }.onFailure { error ->
                viewModel.showError(error.message ?: "Unable to import the configuration.")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ScopeVpnTheme {
                val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

                HomeScreen(
                    uiState = uiState,
                    onImportConfig = { configFileLauncher.launch(arrayOf("*/*")) },
                    onConnect = { requestVpnAuthorization(PendingVpnAction.CONNECT) },
                    onDisconnect = viewModel::disconnect,
                    onOpenFilters = { viewModel.setFilterSheetVisible(true) },
                    onDismissFilters = { viewModel.setFilterSheetVisible(false) },
                    onRoutingModeChanged = viewModel::setRoutingMode,
                    onPackageToggled = viewModel::togglePackage,
                    onSelectVisiblePackages = viewModel::selectVisiblePackages,
                    onAddManualPackage = viewModel::addManualPackage,
                    onApplyAndReconnect = {
                        requestVpnAuthorization(PendingVpnAction.RECONNECT)
                    },
                    onMessageShown = viewModel::clearMessage,
                )
            }
        }
    }

    private fun requestVpnAuthorization(action: PendingVpnAction) {
        pendingVpnAction = action
        val needsNotificationPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS,
            ) != PackageManager.PERMISSION_GRANTED

        if (needsNotificationPermission) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            launchVpnPermissionDialogIfNeeded()
        }
    }

    private fun launchVpnPermissionDialogIfNeeded() {
        val authorizationIntent = VpnService.prepare(this)
        if (authorizationIntent == null) {
            runPendingVpnAction()
        } else {
            vpnPermissionLauncher.launch(authorizationIntent)
        }
    }

    private fun runPendingVpnAction() {
        when (pendingVpnAction) {
            PendingVpnAction.CONNECT -> viewModel.connect()
            PendingVpnAction.RECONNECT -> viewModel.applyFiltersAndReconnect()
        }
    }

    private fun queryDisplayName(uri: Uri): String {
        return contentResolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME),
            null,
            null,
            null,
        )?.use { cursor ->
            if (!cursor.moveToFirst()) return@use null
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0) cursor.getString(index) else null
        } ?: "wireguard.conf"
    }

    private enum class PendingVpnAction {
        CONNECT,
        RECONNECT,
    }

    private companion object {
        const val MAX_CONFIG_IMPORT_BYTES = 256 * 1024
    }
}

private fun InputStream.readBytesWithLimit(maxBytes: Int): ByteArray {
    val output = ByteArrayOutputStream(minOf(maxBytes, DEFAULT_BUFFER_SIZE))
    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
    var total = 0

    while (true) {
        val count = read(buffer)
        if (count < 0) break
        total += count
        require(total <= maxBytes) { "Configuration files must be smaller than 256 KB." }
        output.write(buffer, 0, count)
    }
    return output.toByteArray()
}
