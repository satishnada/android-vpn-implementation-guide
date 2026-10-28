package com.satishnada.scopevpn.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.satishnada.scopevpn.domain.model.AppRoutingPolicy
import com.satishnada.scopevpn.domain.model.InstalledApp
import com.satishnada.scopevpn.domain.model.RoutingMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppFilterSheet(
    policy: AppRoutingPolicy,
    apps: List<InstalledApp>,
    isLoadingApps: Boolean,
    reconnectRequired: Boolean,
    onDismiss: () -> Unit,
    onModeChanged: (RoutingMode) -> Unit,
    onPackageToggled: (String) -> Unit,
    onSelectVisiblePackages: (Set<String>, Boolean) -> Unit,
    onAddManualPackage: (String) -> Unit,
    onApplyAndReconnect: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var query by remember { mutableStateOf("") }
    var appKind by remember { mutableStateOf(AppKind.ALL) }
    var manualPackage by remember { mutableStateOf("") }

    val filteredApps = remember(apps, query, appKind) {
        apps.filter { app ->
            val queryMatches = query.isBlank() ||
                app.label.contains(query, ignoreCase = true) ||
                app.packageName.contains(query, ignoreCase = true)
            val kindMatches = when (appKind) {
                AppKind.ALL -> true
                AppKind.USER -> !app.isSystemApp
                AppKind.SYSTEM -> app.isSystemApp
            }
            queryMatches && kindMatches
        }
    }
    val visiblePackages = filteredApps.mapTo(mutableSetOf(), InstalledApp::packageName)
    val allVisibleSelected = visiblePackages.isNotEmpty() &&
        policy.selectedPackages.containsAll(visiblePackages)
    val appSelectionEnabled = policy.mode != RoutingMode.ALL_APPS

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp),
        ) {
            Text(
                text = "App-wise VPN routing",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Choose an allowlist or denylist. Android applies the list when a new tunnel is established.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(18.dp))
            Column(modifier = Modifier.selectableGroup()) {
                RoutingMode.entries.forEach { mode ->
                    RoutingModeRow(
                        mode = mode,
                        selected = policy.mode == mode,
                        onSelected = { onModeChanged(mode) },
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                enabled = appSelectionEnabled,
                singleLine = true,
                label = { Text("Search apps or package names") },
                leadingIcon = {
                    Icon(Icons.Rounded.Search, contentDescription = null)
                },
            )

            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppKind.entries.forEach { kind ->
                    FilterChip(
                        selected = appKind == kind,
                        onClick = { appKind = kind },
                        enabled = appSelectionEnabled,
                        label = { Text(kind.label) },
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${policy.selectedPackages.size} selected",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.weight(1f),
                )
                TextButton(
                    onClick = {
                        onSelectVisiblePackages(visiblePackages, !allVisibleSelected)
                    },
                    enabled = appSelectionEnabled && visiblePackages.isNotEmpty(),
                ) {
                    Text(if (allVisibleSelected) "Clear visible" else "Select visible")
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp, max = 330.dp),
            ) {
                if (isLoadingApps) {
                    item {
                        Text(
                            text = "Loading launchable apps...",
                            modifier = Modifier.padding(vertical = 24.dp),
                        )
                    }
                } else if (filteredApps.isEmpty()) {
                    item {
                        Text(
                            text = "No visible apps match this filter.",
                            modifier = Modifier.padding(vertical = 24.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    items(filteredApps, key = InstalledApp::packageName) { app ->
                        InstalledAppRow(
                            app = app,
                            checked = app.packageName in policy.selectedPackages,
                            enabled = appSelectionEnabled,
                            onToggle = { onPackageToggled(app.packageName) },
                        )
                        HorizontalDivider()
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Package not shown? Add it manually",
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = "The list intentionally uses launcher visibility instead of requesting access to every installed package.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = manualPackage,
                onValueChange = { manualPackage = it },
                modifier = Modifier.fillMaxWidth(),
                enabled = appSelectionEnabled,
                singleLine = true,
                label = { Text("com.example.app") },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            onAddManualPackage(manualPackage)
                            manualPackage = ""
                        },
                        enabled = appSelectionEnabled && manualPackage.isNotBlank(),
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = "Add package")
                    }
                },
            )

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = if (reconnectRequired) onApplyAndReconnect else onDismiss,
                modifier = Modifier.fillMaxWidth(),
                enabled = policy.validationError() == null,
            ) {
                Text(if (reconnectRequired) "Apply and reconnect" else "Done")
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun RoutingModeRow(
    mode: RoutingMode,
    selected: Boolean,
    onSelected: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onSelected,
                role = Role.RadioButton,
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = null)
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = mode.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = mode.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun InstalledAppRow(
    app: InstalledApp,
    checked: Boolean,
    enabled: Boolean,
    onToggle: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onToggle)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = { onToggle() },
            enabled = enabled,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = app.label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = buildString {
                    append(app.packageName)
                    if (app.isManualEntry) append("  |  manual")
                    else if (app.isSystemApp) append("  |  system")
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private enum class AppKind(val label: String) {
    ALL("All"),
    USER("User"),
    SYSTEM("System"),
}
