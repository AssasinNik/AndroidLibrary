package com.cherenkov.broadcastreceiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import kotlin.random.Random


//Пример статического ресивера который при условии что устройство заряжается будет отправлять уведомление о зарядке
class ChargingStateReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        Log.d("ChargingReceiver", "Received action: $action")

        val isCharging = when (action) {
            Intent.ACTION_POWER_CONNECTED -> true
            Intent.ACTION_POWER_DISCONNECTED -> false
            else -> return
        }
        Toast.makeText(context, "Ресивер сработал!", Toast.LENGTH_SHORT).show()
        showNotification(context, isCharging)
    }

    private fun showNotification(context: Context, isCharging: Boolean) {
        val channelId = "charging_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            channelId,
            "Зарядка устройства",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Уведомления о подключении/отключении зарядки"
        }
        notificationManager.createNotificationChannel(channel)

        // Текст уведомления
        val title = if (isCharging) "🔌 Зарядка подключена" else "🔋 Зарядка отключена"
        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle(title)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Для API < 26
            .setAutoCancel(true)
            .build()

        // Покажите уведомление
        notificationManager.notify(Random.nextInt(100), notification)
    }
}