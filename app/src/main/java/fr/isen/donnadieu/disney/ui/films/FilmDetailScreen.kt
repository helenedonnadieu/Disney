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
        val db = FirebaseDatabase.getInstance().reference

        // Étape 1 : récupère les UIDs depuis /user_films
        db.child("user_films")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val dvdUids  = mutableListOf<String>()
                    val sellUids = mutableListOf<String>()

                    for (userSnap in snapshot.children) {
                        val status = userSnap.child(filmKey).child("status")
                            .getValue(String::class.java)
                        val uid = userSnap.key ?: continue
                        when (status) {
                            "owned"        -> dvdUids.add(uid)
                            "want_to_sell" -> sellUids.add(uid)
                        }
                    }

                    val allUids = (dvdUids + sellUids).distinct()

                    // Aucun résultat → on arrête
                    if (allUids.isEmpty()) {
                        isLoading = false
                        return
                    }

                    // Étape 2 : résout l'email de chaque UID via /users/{uid}/email
                    val resolvedNames = mutableMapOf<String, String>()
                    var remaining = allUids.size

                    for (uid in allUids) {
                        db.child("users").child(uid)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snap: DataSnapshot) {
                                    resolvedNames[uid] =
                                        snap.child("username").getValue(String::class.java)
                                            ?: snap.child("email").getValue(String::class.java)
                                                    ?: uid
                                    remaining--
                                    if (remaining == 0) {
                                        ownersOnDvd         = dvdUids.map { resolvedNames[it] ?: it }
                                        ownersWantingToSell = sellUids.map { resolvedNames[it] ?: it }
                                        isLoading = false
                                    }
                                }
                                override fun onCancelled(error: DatabaseError) {
                                    remaining--
                                    if (remaining == 0) isLoading = false
                                }
                            })
                    }
                }
                override fun onCancelled(error: DatabaseError) { isLoading = false }
            })
    }

    Box(modifier = Modifier.fillMaxSize().background(Beige100)) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {

            // ── Header ────────────────────────────────────────────────
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.verticalGradient(colors = listOf(Beige300, Beige200)))
                        .padding(top = 48.dp, bottom = 32.dp, start = 20.dp, end = 20.dp)
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Beige100.copy(alpha = 0.6f))
                            .size(40.dp)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour", tint = BrownDark)
                    }
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(top = 52.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(BrownMid.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) { Text("🎬", fontSize = 30.sp) }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = film.titre,
                            fontSize = 20.sp, fontWeight = FontWeight.Bold,
                            color = TextPrimary, textAlign = TextAlign.Center, lineHeight = 26.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                            if (film.annee != 0) {
                                Chip("📅 ${film.annee}")
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            if (film.genre.isNotEmpty()) Chip("🎭 ${film.genre}")
                        }
                    }
                }
            }

            // ── Section : possèdent le film ───────────────────────────
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "POSSÈDENT CE FILM",
                    fontSize = 11.sp, fontWeight = FontWeight.ExtraBold,
                    color = BrownLight, letterSpacing = 2.sp,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = BrownMid)
                    }
                }
            } else {
                if (ownersOnDvd.isEmpty()) {
                    item { EmptyState("Aucun utilisateur ne possède ce film.") }
                } else {
                    items(ownersOnDvd) { username -> UserRow(username, "📀") }
                }

                // ── Section : veulent s'en débarrasser ────────────────
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "VEULENT S'EN DÉBARRASSER",
                        fontSize = 11.sp, fontWeight = FontWeight.ExtraBold,
                        color = BrownLight, letterSpacing = 2.sp,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (ownersWantingToSell.isEmpty()) {
                    item { EmptyState("Aucun utilisateur ne souhaite s'en débarrasser.") }
                } else {
                    items(ownersWantingToSell) { username -> UserRow(username, "🏷️") }
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
        Text(text = username, fontWeight = FontWeight.Medium, fontSize = 15.sp, color = TextPrimary)
    }
}
