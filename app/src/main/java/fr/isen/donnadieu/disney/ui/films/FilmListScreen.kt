package fr.isen.donnadieu.disney.ui.films

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

private val Beige100      = Color(0xFFF5F0E8)
private val Beige200      = Color(0xFFEDE4D3)
private val Beige300      = Color(0xFFD9CBBA)
private val BrownDark     = Color(0xFF4A3728)
private val BrownMid      = Color(0xFF7C5C44)
private val BrownLight    = Color(0xFFA67C5B)
private val TextPrimary   = Color(0xFF2E1F14)
private val TextSecondary = Color(0xFF8C7060)


data class Film(
    val titre: String = "",
    val annee: Int = 0,
    val genre: String = "",
    val numero: Int = 0,
    val imdbID: String? = null   // ← ajoute cette ligne
)

data class SousSaga(
    val nom: String = "",
    val films: List<Film> = emptyList()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilmListScreen(
    franchiseName: String,
    onBack: () -> Unit,
    onRequireLogin: () -> Unit
) {
    val statusViewModel: FilmStatusViewModel = viewModel()
    var sousSagas by remember { mutableStateOf<List<SousSaga>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedFilm by remember { mutableStateOf<Film?>(null) }
    var sortByDate by remember { mutableStateOf(false) }

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
                                        numero = film.child("numero").getValue(Int::class.java) ?: 0,
                                        imdbID = film.child("imdbID").getValue(String::class.java)  // ← ajoute ça

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
                                    numero = film.child("numero").getValue(Int::class.java) ?: 0,
                                    imdbID = film.child("imdbID").getValue(String::class.java)  // ← ajoute ça
                                ))
                            }
                            sagaList.add(SousSaga(franchiseName, films))
                        }
                    }
                }
                sousSagas = sagaList
                isLoading = false
            }
            override fun onCancelled(error: DatabaseError) { isLoading = false }
        })
    }

    Box(modifier = Modifier.fillMaxSize().background(Beige100)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(colors = listOf(Beige300, Beige200)))
                    .padding(top = 48.dp, bottom = 16.dp, start = 8.dp, end = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = BrownDark)
                        }
                        Text(
                            text = franchiseName,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    // Bouton de tri
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (sortByDate) BrownMid else Beige300)
                            .clickable { sortByDate = !sortByDate }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (sortByDate) "🗓 Date" else "Sort by Date",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (sortByDate) Color.White else BrownMid
                        )
                    }
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BrownMid)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    sousSagas.forEach { saga ->
                        item {
                            Text(
                                text = saga.nom.uppercase(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = BrownLight,
                                letterSpacing = 2.sp,
                                modifier = Modifier.padding(top = 16.dp, bottom = 6.dp)
                            )
                        }
                        items(
                            if (sortByDate) saga.films.sortedByDescending { it.annee }
                            else saga.films.sortedBy { it.numero }
                        ) { film ->
                            FilmCard(
                                film = film,
                                statusViewModel = statusViewModel,
                                onFilmClick = { selectedFilm = film },
                                onRequireLogin = onRequireLogin
                            )
                        }
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
    onFilmClick: () -> Unit,
    onRequireLogin: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val statuses by statusViewModel.userFilmStatuses.collectAsState()
    val key = film.titre.replace(".", "").replace("#", "").replace("$", "").replace("[", "").replace("]", "")
    val currentStatus = statuses[key]
    var showMenu by remember { mutableStateOf(false) }

    val statusLabels = mapOf(
        "watched"       to "✓ Watched",
        "want_to_watch" to "♡ Watchlist",
        "owned"         to "發 Owned",
        "want_to_sell"  to "鴫 For Sale"
    )

    val statusColor = when (currentStatus) {
        "watched"       -> Color(0xFF5A8A6A)
        "want_to_watch" -> Color(0xFF6A7A8A)
        "owned"         -> BrownMid
        "want_to_sell"  -> Color(0xFFB85C52)
        else            -> Color.Transparent
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .clickable { onFilmClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = film.titre,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = TextPrimary
            )
            if (film.genre.isNotEmpty()) {
                Text(text = film.genre, fontSize = 12.sp, color = TextSecondary)
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (film.annee != 0) {
                Text(
                    text = film.annee.toString(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrownLight
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            Box {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (currentStatus != null) statusColor.copy(alpha = 0.12f)
                            else Beige200
                        )
                        .clickable {
                            if (auth.currentUser != null) {
                                showMenu = true
                            } else {
                                onRequireLogin()
                            }
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (currentStatus != null)
                            statusLabels[currentStatus] ?: "Status"
                        else "＋ Status",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (currentStatus != null) statusColor else BrownMid
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    statusLabels.forEach { (statusKey, label) ->
                        DropdownMenuItem(
                            text = { Text(label, fontSize = 14.sp) },
                            onClick = {
                                statusViewModel.setFilmStatus(film.titre, statusKey)
                                showMenu = false
                            }
                        )
                    }
                    if (currentStatus != null) {
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "🗑 Remove status",
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 14.sp
                                )
                            },
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