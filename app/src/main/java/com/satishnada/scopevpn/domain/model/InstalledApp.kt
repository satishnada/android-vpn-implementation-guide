package com.satishnada.scopevpn.domain.model

data class InstalledApp(
    val label: String,
    val packageName: String,
    val isSystemApp: Boolean,
    val isManualEntry: Boolean = false,
)
