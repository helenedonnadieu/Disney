package fr.isen.donnadieu.disney.ui.films

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.database.*

private val Beige100      = Color(0xFFF5F0E8)
private val Beige200      = Color(0xFFEDE4D3)
private val Beige300      = Color(0xFFD9CBBA)
private val BrownDark     = Color(0xFF4A3728)
private val BrownMid      = Color(0xFF7C5C44)
private val BrownLight    = Color(0xFFA67C5B)
private val TextPrimary   = Color(0xFF2E1F14)
private val TextSecondary = Color(0xFF8C7060)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilmDetailScreen(film: Film, onBack: () -> Unit) {

    var ownersOnDvd by remember { mutableStateOf<List<String>>(emptyList()) }
    var ownersWantingToSell by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val filmKey = film.titre
        .replace(".", "").replace("#", "")
        .replace("$", "").replace("[", "").replace("]", "")

    LaunchedEffect(filmKey) {
        FirebaseDatabase.getInstance().reference
            .child("user_films")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val dvdList  = mutableListOf<String>()
                    val sellList = mutableListOf<String>()
                    for (userSnap in snapshot.children) {
                        val status = userSnap.child(filmKey).child("status")
                            .getValue(String::class.java)
                        val name = userSnap.child("username").getValue(String::class.java)
                            ?: userSnap.child("email").getValue(String::class.java)
                            ?: userSnap.key
                            ?: "Utilisateur inconnu"
                        when (status) {
                            "owned"        -> dvdList.add(name)
                            "want_to_sell" -> sellList.add(name)
                        }
                    }
                    ownersOnDvd         = dvdList
                    ownersWantingToSell = sellList
                    isLoading = false
                }
                override fun onCancelled(error: DatabaseError) { isLoading = false }
            })
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Beige100)
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {

            // ── Header avec dégradé + bouton retour ───────────────────
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.verticalGradient(colors = listOf(Beige300, Beige200)))
                        .padding(top = 48.dp, bottom = 32.dp, start = 20.dp, end = 20.dp)
                ) {
                    // Bouton retour
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Beige100.copy(alpha = 0.6f))
                            .size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Retour",
                            tint = BrownDark
                        )
                    }

                    // Infos film centrées
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 52.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Icône film
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(BrownMid.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🎬", fontSize = 30.sp)
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = film.titre,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            textAlign = TextAlign.Center,
                            lineHeight = 26.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (film.annee != 0) {
                                Chip(label = "📅 ${film.annee}")
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            if (film.genre.isNotEmpty()) {
                                Chip(label = "🎭 ${film.genre}")
                            }
                        }
                    }
                }
            }

            // ── Section DVD ────────────────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "POSSÈDENT CE FILM",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = BrownLight,
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator(color = BrownMid) }
                }
            } else {
                if (ownersOnDvd.isEmpty()) {
                    item { EmptyState(text = "Aucun utilisateur ne possède ce film.") }
                } else {
                    items(ownersOnDvd) { username ->
                        UserRow(username = username, emoji = "📀")
                    }
                }

                // ── Section veulent s'en débarrasser ───────────────────
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "VEULENT S'EN DÉBARRASSER",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = BrownLight,
                        letterSpacing = 2.sp,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (ownersWantingToSell.isEmpty()) {
                    item { EmptyState(text = "Aucun utilisateur ne souhaite s'en débarrasser.") }
                } else {
                    items(ownersWantingToSell) { username ->
                        UserRow(username = username, emoji = "🏷️")
                    }
                }

                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }
}

@Composable
private fun Chip(label: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(BrownMid.copy(alpha = 0.12f))
            .padding(horizontal = 12.dp, vertical = 5.dp)
    ) {
        Text(text = label, fontSize = 12.sp, color = BrownDark, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun EmptyState(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Beige200)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Text(text = text, fontSize = 13.sp, color = TextSecondary)
    }
}

@Composable
private fun UserRow(username: String, emoji: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Beige200),
            contentAlignment = Alignment.Center
        ) { Text(emoji, fontSize = 16.sp) }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = username,
            fontWeight = FontWeight.Medium,
            fontSize = 15.sp,
            color = TextPrimary
        )
    }
}