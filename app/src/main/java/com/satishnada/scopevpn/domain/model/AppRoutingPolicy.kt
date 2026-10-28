package com.satishnada.scopevpn.domain.model

enum class RoutingMode(
    val title: String,
    val description: String,
) {
    ALL_APPS(
        title = "All apps",
        description = "Route every app through the VPN.",
    ),
    ONLY_SELECTED(
        title = "Only selected apps",
        description = "Only checked apps use the VPN.",
    ),
    EXCLUDE_SELECTED(
        title = "Exclude selected apps",
        description = "Checked apps bypass the VPN.",
    ),
}

data class AppRoutingPolicy(
    val mode: RoutingMode = RoutingMode.ALL_APPS,
    val selectedPackages: Set<String> = emptySet(),
) {
    fun validationError(): String? = when {
        mode == RoutingMode.ONLY_SELECTED && selectedPackages.isEmpty() ->
            "Select at least one app for the Only selected apps mode."
        else -> null
    }

    fun toggle(packageName: String): AppRoutingPolicy {
        val updated = selectedPackages.toMutableSet().apply {
            if (!add(packageName)) remove(packageName)
        }
        return copy(selectedPackages = updated.toSet())
    }

    fun withMode(newMode: RoutingMode): AppRoutingPolicy = copy(mode = newMode)

    val summary: String
        get() = when (mode) {
            RoutingMode.ALL_APPS -> "All apps use the VPN"
            RoutingMode.ONLY_SELECTED ->
                "${selectedPackages.size} selected app${selectedPackages.size.pluralSuffix()} use the VPN"
            RoutingMode.EXCLUDE_SELECTED ->
                "${selectedPackages.size} selected app${selectedPackages.size.pluralSuffix()} bypass the VPN"
        }
}

private fun Int.pluralSuffix(): String = if (this == 1) "" else "s"
