package com.satishnada.scopevpn.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = ScopeBlue,
    secondary = ScopeMint,
    tertiary = ScopeNavy,
    background = ScopeSurfaceLight,
    surface = ScopeSurfaceLight,
)

private val DarkColors = darkColorScheme(
    primary = ScopeBlueDark,
    secondary = ScopeMint,
    background = ScopeSurfaceDark,
    surface = ScopeSurfaceDark,
)

@Composable
fun ScopeVpnTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = ScopeTypography,
        content = content,
    )
}
