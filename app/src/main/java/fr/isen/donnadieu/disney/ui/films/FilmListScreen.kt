package fr.isen.donnadieu.disney.ui.films

import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import fr.isen.donnadieu.disney.R

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
    val imdbID: String? = null
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
    var sousSagas   by remember { mutableStateOf<List<SousSaga>>(emptyList()) }
    var isLoading   by remember { mutableStateOf(true) }
    var selectedFilm by remember { mutableStateOf<Film?>(null) }
    var sortByDate  by remember { mutableStateOf(false) }

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
                                        titre  = film.child("titre").getValue(String::class.java) ?: "",
                                        annee  = film.child("annee").getValue(Int::class.java) ?: 0,
                                        genre  = film.child("genre").getValue(String::class.java) ?: "",
                                        numero = film.child("numero").getValue(Int::class.java) ?: 0,
                                        imdbID = film.child("imdbID").getValue(String::class.java)
                                    ))
                                }
                                sagaList.add(SousSaga(sagaNom, films))
                            }
                        } else {
                            val films = mutableListOf<Film>()
                            for (film in franchise.child("films").children) {
                                films.add(Film(
                                    titre  = film.child("titre").getValue(String::class.java) ?: "",
                                    annee  = film.child("annee").getValue(Int::class.java) ?: 0,
                                    genre  = film.child("genre").getValue(String::class.java) ?: "",
                                    numero = film.child("numero").getValue(Int::class.java) ?: 0,
                                    imdbID = film.child("imdbID").getValue(String::class.java)
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

    // Nombre total de films
    val totalFilms = sousSagas.sumOf { it.films.size }

    Box(modifier = Modifier.fillMaxSize().background(Beige100)) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Header avec banner ────────────────────────────────────────────
            Box(modifier = Modifier.fillMaxWidth()) {

                // Banner Disney en fond réduit
                Image(
                    painter = painterResource(id = R.drawable.disney_banner),
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(140.dp),
                    contentScale = ContentScale.Crop
                )

                // Dégradé sombre pour lisibilité
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.45f),
                                    Color.Black.copy(alpha = 0.15f)
                                )
                            )
                        )
                )

                // Contenu du header par-dessus le banner
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 44.dp, start = 8.dp, end = 16.dp, bottom = 14.dp)
                ) {
                    // Ligne 1 : retour + titre
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                        Text(
                            text = franchiseName,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }

                    // Ligne 2 : compteur + bouton tri
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Badge nombre de films
                        if (!isLoading && totalFilms > 0) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White.copy(alpha = 0.20f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "$totalFilms films",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }
                        }

                        // Bouton tri
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    if (sortByDate) BrownMid
                                    else Color.White.copy(alpha = 0.20f)
                                )
                                .clickable { sortByDate = !sortByDate }
                                .padding(horizontal = 12.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = if (sortByDate) "🗓 Par date" else "↕ Par numéro",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
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
                    contentPadding = PaddingValues(bottom = 90.dp, top = 12.dp)
                ) {
                    sousSagas.forEach { saga ->

                        // ── Header sous-saga stylisé ──────────────────────────
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(BrownDark, BrownMid)
                                        )
                                    )
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = saga.nom,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White,
                                        letterSpacing = 0.5.sp
                                    )
                                    // Compteur de films dans la sous-saga
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color.White.copy(alpha = 0.18f))
                                            .padding(horizontal = 7.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "${saga.films.size} films",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White.copy(alpha = 0.9f)
                                        )
                                    }
                                }
                            }
                        }

                        // ── Films de la sous-saga ─────────────────────────────
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
    val key = film.titre
        .replace(".", "").replace("#", "")
        .replace("$", "").replace("[", "").replace("]", "")
    val currentStatus = statuses[key]
    var showMenu by remember { mutableStateOf(false) }

    val statusLabels = mapOf(
        "watched"       to "✓ Watched",
        "want_to_watch" to "♡ Watchlist",
        "owned"         to "📀 Owned",
        "want_to_sell"  to "🏷️ For Sale"
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
            .shadow(1.dp, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .clickable { onFilmClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Numéro du film
        if (film.numero != 0) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(BrownMid.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = film.numero.toString().padStart(2, '0'),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = BrownMid
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = film.titre,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = TextPrimary
            )
            if (film.genre.isNotEmpty()) {
                Text(text = film.genre, fontSize = 11.sp, color = TextSecondary)
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
                            if (auth.currentUser != null) showMenu = true
                            else onRequireLogin()
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
                                statusViewModel.setFilmStatus(film.titre, statusKey, film.imdbID)
                                //statusViewModel.setFilmStatus(film.titre, statusKey)
                                showMenu = false
                            }
                        )
                    }
                    if (currentStatus != null) {
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = {
                                Text("🗑 Remove status",
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 14.sp)
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