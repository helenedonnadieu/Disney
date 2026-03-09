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

    var ownersWantingToSell by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Même logique de clé que dans FilmStatusViewModel
    val filmKey = film.titre
        .replace(".", "").replace("#", "")
        .replace("$", "").replace("[", "").replace("]", "")

    LaunchedEffect(filmKey) {
        val db = FirebaseDatabase.getInstance().reference

        // Structure attendue : /users/{userId}/films/{filmKey}/status
        //                      /users/{userId}/username  (ou email)
        db.child("users").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val result = mutableListOf<String>()

                for (userSnap in snapshot.children) {
                    val status = userSnap
                        .child("films")
                        .child(filmKey)
                        .child("status")
                        .getValue(String::class.java)

                    if (status == "want_to_sell") {
                        val name = userSnap.child("username").getValue(String::class.java)
                            ?: userSnap.child("email").getValue(String::class.java)
                            ?: userSnap.key
                            ?: "Utilisateur inconnu"
                        result.add(name)
                    }
                }

                ownersWantingToSell = result
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
            // Infos du film
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
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Section utilisateurs
            item {
                Text(
                    text = "get rid of it",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            when {
                isLoading -> item {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                ownersWantingToSell.isEmpty() -> item {
                    Text(
                        text = "utilisateur wants to get rid of it.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                else -> items(ownersWantingToSell) { username ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "👤", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = username, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}


