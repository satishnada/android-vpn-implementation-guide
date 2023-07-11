

package com.satish.vpnguide.service

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.satish.vpnguide.utils.Logger
import com.satish.vpnguide.utils.NotificationChannels
import com.satish.vpnguide.utils.NotificationPrototype

object NotificationService {

    private val log = Logger("Notification")
    @SuppressLint("StaticFieldLeak")
    private val context = ContextService
    private val notificationManager by lazy {
        context.requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    private var useChannels: Boolean = false

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            log.v("Creating notification channels")
            NotificationChannels.values().forEach {
                createNotificationChannel(it)
            }
            useChannels = true
        }
    }

    fun show(notification: NotificationPrototype) {
        val builder = notification.create(context.requireContext())
        if (useChannels) builder.setChannelId(notification.channel.name)
        val n = builder.build()
        if (notification.autoCancel) n.flags = Notification.FLAG_AUTO_CANCEL
        notificationManager.notify(notification.id, n)
    }

    fun build(notification: NotificationPrototype): Notification {
        val builder = notification.create(context.requireContext())
        if (useChannels) builder.setChannelId(notification.channel.name)
        val n = builder.build()
        if (notification.autoCancel) n.flags = Notification.FLAG_AUTO_CANCEL
        return n
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channel: NotificationChannels) {
        val mChannel = NotificationChannel(
            channel.name,
            channel.title,
            channel.importance
        )
        notificationManager.createNotificationChannel(mChannel)
    }

}