package com.satishnada.scopevpn.ui.home

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.satishnada.scopevpn.domain.model.VpnConnectionState

@Composable
fun BroadcastingVpnOrb(
    connectionState: VpnConnectionState,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val active = connectionState == VpnConnectionState.CONNECTED ||
        connectionState == VpnConnectionState.CONNECTING
    val busy = connectionState.isBusy
    val primary = MaterialTheme.colorScheme.primary
    val surface = MaterialTheme.colorScheme.surface
    val outline = MaterialTheme.colorScheme.outlineVariant

    val infiniteTransition = rememberInfiniteTransition(label = "vpn-broadcast")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2_400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "broadcast-phase",
    )
    val breathe by infiniteTransition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1_200),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "core-breathe",
    )
    val restingScale by animateFloatAsState(
        targetValue = if (active) 1f else 0.94f,
        animationSpec = tween(durationMillis = 350),
        label = "resting-scale",
    )

    val description = when (connectionState) {
        VpnConnectionState.CONNECTED -> "VPN connected. Double tap to disconnect."
        VpnConnectionState.CONNECTING -> "VPN is connecting."
        VpnConnectionState.DISCONNECTING -> "VPN is disconnecting."
        VpnConnectionState.ERROR -> "VPN connection error. Double tap to retry."
        VpnConnectionState.DISCONNECTED -> "VPN disconnected. Double tap to connect."
    }

    Box(
        modifier = modifier
            .size(264.dp)
            .semantics {
                contentDescription = description
                role = Role.Button
            }
            .clickable(
                enabled = enabled && !busy,
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val minDimension = size.minDimension
            val centerRadius = minDimension * 0.22f

            if (active) {
                repeat(3) { index ->
                    val localPhase = (phase + index / 3f) % 1f
                    val radius = centerRadius + minDimension * 0.26f * localPhase
                    val alpha = (1f - localPhase) * 0.34f
                    drawCircle(
                        color = primary.copy(alpha = alpha),
                        radius = radius,
                        style = Stroke(
                            width = 3.dp.toPx() * (1f - localPhase * 0.45f),
                            cap = StrokeCap.Round,
                        ),
                    )
                }
            } else {
                repeat(3) { index ->
                    drawCircle(
                        color = outline.copy(alpha = 0.58f - index * 0.13f),
                        radius = centerRadius + (index + 1) * minDimension * 0.08f,
                        style = Stroke(width = 1.dp.toPx()),
                    )
                }
            }
        }

        Surface(
            modifier = Modifier
                .size(124.dp)
                .graphicsLayer {
                    val scale = if (active) breathe else restingScale
                    scaleX = scale
                    scaleY = scale
                },
            shape = CircleShape,
            color = if (active) primary else MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = if (active) 10.dp else 2.dp,
            shadowElevation = 8.dp,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (active) Icons.Rounded.Shield else Icons.Rounded.PowerSettingsNew,
                    contentDescription = null,
                    tint = if (active) surface else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(52.dp),
                )
                if (busy) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(94.dp)
                            .alpha(0.88f),
                        color = if (active) surface else primary,
                        strokeWidth = 3.dp,
                    )
                }
            }
        }
    }
}
