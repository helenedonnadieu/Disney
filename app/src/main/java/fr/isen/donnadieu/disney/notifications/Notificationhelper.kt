package fr.isen.donnadieu.disney.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import fr.isen.donnadieu.disney.R

object NotificationHelper {

    private const val CHANNEL_ID = "marketplace_channel"
    private const val CHANNEL_NAME = "Marketplace"
    private var notifId = 0

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications quand un film est mis en vente"
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    fun showMarketplaceNotification(context: Context, sellerUsername: String, filmTitre: String) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("🏷️ Nouveau film sur le marché !")
            .setContentText("$sellerUsername vend \"$filmTitre\"")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("$sellerUsername vend \"$filmTitre\""))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        manager.notify(notifId++, notification)
    }
}