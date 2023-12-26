package com.satish.vpnguide.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.app.NotificationCompat
import com.satish.vpnguide.Logger
import com.satish.vpnguide.R
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer

class LocalVpnService : VpnService() {

    private val log = Logger("AskVpnPermissionFragment")
    private var vpnThread: Thread? = null
    private var vpnInterface: ParcelFileDescriptor? = null
    private val CHANNEL_ID = "VPN_CHANNEL_ID"
    private val NOTIFICATION_ID = 1

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        log.v("onStartCommand: ${intent?.action}")
        log.v("onStartCommand: isVpnRunning - $isVpnRunning")
        // Check if the service is started with a specific action
        intent?.let {
            when (it.action) {
                "STOP_VPN_SERVICE" -> {
                    if (isVpnRunning) {
                        isVpnRunning = false
                        stopVpn()
                    }
                }
                "START_VPN_SERVICE" -> {
                    if (!isVpnRunning) {
                        startForeground(NOTIFICATION_ID, createNotification())
                        startVpn()
                    }
                }
            }
        }

        // Return the appropriate service restart behavior
        return START_STICKY
    }

    private fun createNotificationChannel() {
        log.v("createNotificationChannel")
        // Create a notification channel for Android Oreo and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "VPN Channel"
            val descriptionText = "VPN Service Channel"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        log.v("createNotification")
        // Create notification channel (if targeting Android Oreo and above)
        createNotificationChannel()

        // Build the notification using NotificationCompat.Builder
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("VPN Service")
            .setContentText("VPN service is running")
            .setSmallIcon(R.drawable.ic_logo)
            .setOngoing(true)

        // Add actions to the notification (e.g., stop VPN service)
        addNotificationActions(builder)

        // Return the built notification
        return builder.build()
    }

    private fun addNotificationActions(builder: NotificationCompat.Builder) {
        log.v("addNotificationActions")
        // Create an intent for stopping the VPN service
        val stopIntent = Intent(this, LocalVpnService::class.java).apply {
            action = "STOP_VPN_SERVICE"
        }
        // Add FLAG_MUTABLE or FLAG_IMMUTABLE based on your use case
        val stopPendingIntent: PendingIntent =
            PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE)

        // Add the stop action to the notification
        builder.addAction(
            R.drawable.ic_logo,
            "Stop VPN",
            stopPendingIntent
        )
    }

    private fun startVpn() {
        log.v("startVpn")
        vpnThread = Thread {
            try {
                // Start your VPN logic here
                establishVpnConnection()

                // Your VPN logic should go here
                while (true) {
                    // Read data from the VPN interface
                    val input = FileInputStream(vpnInterface?.fileDescriptor)
                    val buffer = ByteBuffer.allocate(4096)
                    val length = input.read(buffer.array())
                    if (length > 0) {
                        // Process the received data

                        // Write data back to the VPN interface (echo)
                        val output = FileOutputStream(vpnInterface?.fileDescriptor)
                        output.write(buffer.array(), 0, length)
                        output.close()
                    }
                    input.close()
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        vpnThread?.start()
    }

    private fun establishVpnConnection() {
        log.v("establishVpnConnection")
        val builder = Builder()
        vpnInterface = builder.setSession("LocalVPNService")
            .addAddress("10.0.0.2", 32)
            .addRoute("0.0.0.0", 0)
            .establish()

        isVpnRunning = true
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun stopVpn() {
        log.v("stopVpn")
        if (isVpnRunning) {
            isVpnRunning = false
            vpnThread?.interrupt()
            vpnThread = null
            vpnInterface?.close()
        }
    }

    override fun onRevoke() {
        super.onRevoke()
    }

    companion object {
        var isVpnRunning = false
    }
}