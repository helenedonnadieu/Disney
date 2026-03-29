package fr.isen.donnadieu.disney.ui.films

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FilmStatusViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance()

    private val _userFilmStatuses = MutableStateFlow<Map<String, String>>(emptyMap())
    val userFilmStatuses: StateFlow<Map<String, String>> = _userFilmStatuses

    init {
        loadUserStatuses()
    }

    private fun loadUserStatuses() {
        val userId = auth.currentUser?.uid ?: return
        db.getReference("user_films/$userId")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val map = mutableMapOf<String, String>()
                    for (child in snapshot.children) {
                        val status = child.child("status").getValue(String::class.java) ?: ""
                        if (status.isNotEmpty()) map[child.key ?: ""] = status
                    }
                    _userFilmStatuses.value = map
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun setFilmStatus(filmTitre: String, status: String, imdbID: String? = null) {
        val userId = auth.currentUser?.uid ?: return
        val key = filmTitre.replace(".", "").replace("#", "").replace("$", "").replace("[", "").replace("]", "")
        val ref = db.getReference("user_films/$userId/$key")
        ref.child("status").setValue(status)
        if (!imdbID.isNullOrBlank() && imdbID != "N/A") {
            ref.child("imdbID").setValue(imdbID)
        }

        // ← AJOUTER CE BLOC
        if (status == "want_to_sell") {
            db.getReference("users/$userId/username").get().addOnSuccessListener { snap ->
                val username = snap.getValue(String::class.java) ?: "Quelqu'un"
                val event = mapOf(
                    "filmTitre"      to filmTitre,
                    "sellerUsername" to username,
                    "sellerId"       to userId,
                    "timestamp"      to System.currentTimeMillis()
                )
                db.getReference("marketplace_events").push().setValue(event)
            }
        }
    }

    fun removeFilmStatus(filmTitre: String) {
        val userId = auth.currentUser?.uid ?: return
        val key = filmTitre.replace(".", "").replace("#", "").replace("$", "").replace("[", "").replace("]", "")
        db.getReference("user_films/$userId/$key").removeValue()
    }
}