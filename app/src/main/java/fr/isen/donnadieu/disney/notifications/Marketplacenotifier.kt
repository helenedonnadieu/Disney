package fr.isen.donnadieu.disney.notifications

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

object MarketplaceNotifier {

    /**
     * À appeler au démarrage de l'app (dans MainActivity.onCreate ou après login).
     * Vérifie les événements marketplace non vus et affiche une notif pour chacun.
     */
    fun checkPendingNotifications(context: Context) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseDatabase.getInstance()

        // Récupère la dernière fois que l'utilisateur a ouvert l'app
        db.getReference("users/$currentUserId/lastSeen").get().addOnSuccessListener { lastSeenSnap ->
            val lastSeen = lastSeenSnap.getValue(Long::class.java) ?: 0L

            // Cherche les événements marketplace plus récents que lastSeen
            db.getReference("marketplace_events")
                .orderByChild("timestamp")
                .startAfter(lastSeen.toDouble())
                .get()
                .addOnSuccessListener { snapshot ->
                    for (eventSnap in snapshot.children) {
                        val sellerId       = eventSnap.child("sellerId").getValue(String::class.java) ?: continue
                        val sellerUsername = eventSnap.child("sellerUsername").getValue(String::class.java) ?: "Quelqu'un"
                        val filmTitre      = eventSnap.child("filmTitre").getValue(String::class.java) ?: "un film"

                        // Ne pas notifier l'utilisateur de ses propres ventes
                        if (sellerId == currentUserId) continue

                        NotificationHelper.showMarketplaceNotification(context, sellerUsername, filmTitre)
                    }

                    // Met à jour lastSeen avec maintenant
                    db.getReference("users/$currentUserId/lastSeen")
                        .setValue(System.currentTimeMillis())
                }
        }
    }
}