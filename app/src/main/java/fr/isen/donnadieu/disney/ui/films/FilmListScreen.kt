package fr.isen.donnadieu.disney.ui.films

import androidx.compose.foundation.clickable
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

data class Film(
    val titre: String = "",
    val annee: Int = 0,
    val genre: String = "",
    val numero: Int = 0
)

data class SousSaga(
    val nom: String = "",
    val films: List<Film> = emptyList()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilmListScreen(franchiseName: String, onBack: () -> Unit) {

    val statusViewModel: FilmStatusViewModel = viewModel()
    var sousSagas by remember { mutableStateOf<List<SousSaga>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedFilm by remember { mutableStateOf<Film?>(null) } // ← AJOUT

    // ← AJOUT : si un film est sélectionné, afficher son détail
    selectedFilm?.let { film ->
        FilmDetailScreen(film = film, onBack = { selectedFilm = null })
        return
    }

    LaunchedEffect(franchiseName) {
        val db = FirebaseDatabase.getInstance().getReference("categories")
        db.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val sagaList = mutableListOf<SousSaga>()

                for (category in snapshot.children) {
                    for (franchise in category.child("franchises").children) {
                        val nom = franchise.child("nom").getValue(String::class.java) ?: ""
                        if (nom != franchiseName) continue

                        val sousSagasSnap = franchise.child("sous_sagas")
                        if (sousSagasSnap.exists()) {
                            for (sousSaga in sousSagasSnap.children) {
                                val sagaNom = sousSaga.child("nom").getValue(String::class.java) ?: ""
                                val films = mutableListOf<Film>()
                                for (film in sousSaga.child("films").children) {
                                    films.add(Film(
                                        titre = film.child("titre").getValue(String::class.java) ?: "",
                                        annee = film.child("annee").getValue(Int::class.java) ?: 0,
                                        genre = film.child("genre").getValue(String::class.java) ?: "",
                                        numero = film.child("numero").getValue(Int::class.java) ?: 0
                                    ))
                                }
                                sagaList.add(SousSaga(sagaNom, films))
                            }
                        } else {
                            val films = mutableListOf<Film>()
                            for (film in franchise.child("films").children) {
                                films.add(Film(
                                    titre = film.child("titre").getValue(String::class.java) ?: "",
                                    annee = film.child("annee").getValue(Int::class.java) ?: 0,
                                    genre = film.child("genre").getValue(String::class.java) ?: "",
                                    numero = film.child("numero").getValue(Int::class.java) ?: 0
                                ))
                            }
                            sagaList.add(SousSaga(franchiseName, films))
                        }
                    }
                }

                sousSagas = sagaList
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
                title = { Text(franchiseName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                sousSagas.forEach { saga ->
                    item {
                        Text(
                            text = saga.nom,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                        )
                        HorizontalDivider()
                    }
                    items(saga.films) { film ->
                        FilmCard(
                            film = film,
                            statusViewModel = statusViewModel,
                            onFilmClick = { selectedFilm = film } // ← AJOUT
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FilmCard(
    film: Film,
    statusViewModel: FilmStatusViewModel,
    onFilmClick: () -> Unit // ← AJOUT
) {
    val statuses by statusViewModel.userFilmStatuses.collectAsState()
    val key = film.titre.replace(".", "").replace("#", "").replace("$", "").replace("[", "").replace("]", "")
    val currentStatus = statuses[key]
    var showMenu by remember { mutableStateOf(false) }

    val statusLabels = mapOf(
        "watched"       to " Watched ",
        "want_to_watch" to " Want to watch ",
        "owned"         to " Own on DVD",
        "want_to_sell"  to " Want to get rid of "
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onFilmClick() }, // ← AJOUT
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${film.numero}. ${film.titre}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    if (film.genre.isNotEmpty()) {
                        Text(
                            text = film.genre,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (film.annee != 0) {
                    Text(
                        text = film.annee.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box {
                OutlinedButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (currentStatus != null) statusLabels[currentStatus] ?: "Statut"
                        else "＋ Ajouter un statut"
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    statusLabels.forEach { (key, label) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                statusViewModel.setFilmStatus(film.titre, key)
                                showMenu = false
                            }
                        )
                    }
                    if (currentStatus != null) {
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("🗑 Supprimer le statut", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                statusViewModel.removeFilmStatus(film.titre)
                                showMenu = false
                            }
                        )
                    }
                }
            }
        }
    }
}