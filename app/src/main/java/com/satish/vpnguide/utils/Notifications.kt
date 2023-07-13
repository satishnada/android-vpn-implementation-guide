
package com.satish.vpnguide.utils

import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.satish.vpnguide.ui.MainActivity
import com.satish.vpnguide.R

private const val IMPORTANCE_NONE = 0

enum class NotificationChannels(val title: String, val importance: Int) {
    ACTIVITY("Activity", IMPORTANCE_NONE),
}

sealed class NotificationPrototype(
    val id: Int,
    val channel: NotificationChannels,
    val autoCancel: Boolean = false,
    val create: (ctx: Context) -> NotificationCompat.Builder
)

class MonitorNotification(
    working: Boolean,
): NotificationPrototype(1, NotificationChannels.ACTIVITY,
    create = { ctx ->
        val notificationBuilder = NotificationCompat.Builder(ctx)
        notificationBuilder.setSmallIcon(R.drawable.ic_vpn_logo)
        notificationBuilder.priority = NotificationCompat.PRIORITY_MAX
        notificationBuilder.setVibrate(LongArray(0))
        notificationBuilder.setOngoing(true)
        notificationBuilder.setContentTitle(ctx.getString(R.string.universal_status_processing))
        val intentActivity = Intent(ctx, MainActivity::class.java)
        intentActivity.putExtra("notification", true)
        val piActivity = ctx.getPendingIntentForActivity(intentActivity, 0)
        notificationBuilder.setContentIntent(piActivity)
    }
)
