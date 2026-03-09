package fr.isen.donnadieu.disney.ui.films

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.database.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilmDetailScreen(film: Film, onBack: () -> Unit) {

    var ownersOnDvd by remember { mutableStateOf<List<String>>(emptyList()) }
    var ownersWantingToSell by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Même logique de clé que dans FilmStatusViewModel
    val filmKey = film.titre
        .replace(".", "").replace("#", "")
        .replace("$", "").replace("[", "").replace("]", "")

    LaunchedEffect(filmKey) {
        val db = FirebaseDatabase.getInstance().reference

        // Structure : /users/{userId}/films/{filmKey}/status
        //             /users/{userId}/username  (ou email)
        db.child("users").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val dvdList  = mutableListOf<String>()
                val sellList = mutableListOf<String>()

                for (userSnap in snapshot.children) {
                    val status = userSnap
                        .child("films")
                        .child(filmKey)
                        .child("status")
                        .getValue(String::class.java)

                    val name = userSnap.child("username").getValue(String::class.java)
                        ?: userSnap.child("email").getValue(String::class.java)
                        ?: userSnap.key
                        ?: "Utilisateur inconnu"

                    when (status) {
                        "owned"         -> dvdList.add(name)
                        "want_to_sell"  -> sellList.add(name)
                    }
                }

                ownersOnDvd         = dvdList
                ownersWantingToSell = sellList
                isLoading = false
            }

            override fun onCancelled(error: DatabaseError) {
                isLoading = false
            }
        })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(film.titre) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            // ── Infos du film ──────────────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = film.titre,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (film.genre.isNotEmpty()) {
                    Text(text = "Genre : ${film.genre}", style = MaterialTheme.typography.bodyMedium)
                }
                if (film.annee != 0) {
                    Text(text = "Année : ${film.annee}", style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
            }

            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else {

                // ── Section 1 : Possèdent le film en DVD/Blu-ray ──────────
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "📀 Possèdent ce film (DVD / Blu-ray)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }

                if (ownersOnDvd.isEmpty()) {
                    item {
                        Text(
                            text = "Aucun utilisateur ne possède ce film.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                } else {
                    items(ownersOnDvd) { username ->
                        UserRow(username = username, emoji = "📀")
                    }
                }

                // ── Section 2 : Veulent s'en débarrasser ─────────────────
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "🏷️ Veulent s'en débarrasser",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }

                if (ownersWantingToSell.isEmpty()) {
                    item {
                        Text(
                            text = "Aucun utilisateur ne souhaite s'en débarrasser.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                } else {
                    items(ownersWantingToSell) { username ->
                        UserRow(username = username, emoji = "👤")
                    }
                }
            }
        }
    }
}

@Composable
private fun UserRow(username: String, emoji: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = emoji, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = username, fontWeight = FontWeight.Medium)
        }
    }
}