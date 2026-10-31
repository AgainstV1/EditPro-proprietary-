package com.editpro.studio.mcp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

/**
 * Keeps the MCP server alive while the user is away from the editor.
 *
 * Without a foreground service, Android tears the process down shortly after the editor leaves the
 * screen — which is precisely when an agent on the same tablet needs the socket. This service does
 * nothing except hold a notification; the socket itself lives in [McpServer], owned by the view
 * model.
 *
 * Every step is defensive: a failure to post the notification must not take the app down, because
 * the editing session is more important than the agent endpoint.
 */
class McpService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        goForeground()
        runCatching { AgentHost.current()?.startMcpServer() }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        goForeground()
        runCatching { AgentHost.current()?.startMcpServer() }
        return START_STICKY
    }

    override fun onDestroy() {
        // The socket is owned by the view model, not the service, so it is left running if the
        // process survives. If the process dies, everything dies with it, which is correct.
        super.onDestroy()
    }

    private fun goForeground() {
        runCatching {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && manager != null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Agent access",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Keeps the local MCP server reachable by an AI agent."
                    setShowBadge(false)
                }
                manager.createNotificationChannel(channel)
            }

            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_notify_sync)
                .setContentTitle("Agent access is on")
                .setContentText("An AI agent can edit this project over MCP")
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setSilent(true)
                .build()

            startForeground(NOTIFICATION_ID, notification)
        }
    }

    companion object {
        private const val CHANNEL_ID = "editpro_agent_access"
        private const val NOTIFICATION_ID = 4101

        fun start(context: Context) {
            runCatching {
                ContextCompat.startForegroundService(
                    context,
                    Intent(context, McpService::class.java)
                )
            }
        }

        fun stop(context: Context) {
            runCatching { context.stopService(Intent(context, McpService::class.java)) }
        }
    }
}
