package fr.isen.donnadieu.disney.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import fr.isen.donnadieu.disney.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(onLogout: () -> Unit) {

    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val userId = currentUser?.uid ?: return

    var ownedFilmKeys     by remember { mutableStateOf<List<String>>(emptyList()) }
    var watchlistFilmKeys by remember { mutableStateOf<List<String>>(emptyList()) }
    var forSaleFilmKeys   by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading         by remember { mutableStateOf(true) }
    var filmToDelete      by remember { mutableStateOf<Pair<String, String>?>(null) } // Pair(key, status)
    var userPseudo        by remember { mutableStateOf<String?>(null) }
    var selectedTab       by remember { mutableStateOf(0) }

    val beige100     = colorResource(R.color.Beige100)
    val beige200     = colorResource(R.color.Beige200)
    val beige300     = colorResource(R.color.Beige300)
    val brownMid     = colorResource(R.color.BrownMid)
    val textPrimary  = colorResource(R.color.TextPrimary)
    val textSecondary= colorResource(R.color.TextSecondary)
    val brownLight   = colorResource(R.color.BrownLight)
    val brownDark    = colorResource(R.color.BrownDark)
    val errorRed     = Color(0xFFB85C52)

    val displayName = userPseudo
        ?: currentUser.email?.substringBefore("@")
        ?: "Utilisateur"

    val initiale = displayName.firstOrNull()?.uppercaseChar()?.toString() ?: "?"

    val tabs = listOf(
        Triple("📀", "Owned",     ownedFilmKeys.size),
        Triple("♡",  "Watchlist", watchlistFilmKeys.size),
        Triple("🏷️", "For Sale",  forSaleFilmKeys.size)
    )

    val currentList = when (selectedTab) {
        0 -> ownedFilmKeys
        1 -> watchlistFilmKeys
        2 -> forSaleFilmKeys
        else -> emptyList()
    }

    val currentEmoji = when (selectedTab) {
        0 -> "🎬"; 1 -> "♡"; 2 -> "🏷️"; else -> "🎬"
    }

    LaunchedEffect(userId) {
        val db = FirebaseDatabase.getInstance()

        db.getReference("users/$userId/username")
            .get().addOnSuccessListener { snapshot ->
                val fetched = snapshot.getValue(String::class.java)
                userPseudo = fetched?.takeIf { it.isNotBlank() }
                    ?: currentUser.displayName?.takeIf { it.isNotBlank() }
            }.addOnFailureListener {
                userPseudo = currentUser.displayName?.takeIf { it.isNotBlank() }
            }

        db.getReference("user_films/$userId")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val owned     = mutableListOf<String>()
                    val watchlist = mutableListOf<String>()
                    val forSale   = mutableListOf<String>()
                    for (child in snapshot.children) {
                        val status = child.child("status").getValue(String::class.java)
                        val key = child.key ?: continue
                        when (status) {
                            "owned"         -> owned.add(key)
                            "want_to_watch" -> watchlist.add(key)
                            "want_to_sell"  -> forSale.add(key)
                        }
                    }
                    ownedFilmKeys     = owned
                    watchlistFilmKeys = watchlist
                    forSaleFilmKeys   = forSale
                    isLoading = false
                }
                override fun onCancelled(error: DatabaseError) { isLoading = false }
            })
    }

    filmToDelete?.let { (key, status) ->
        AlertDialog(
            onDismissRequest = { filmToDelete = null },
            shape = RoundedCornerShape(20.dp),
            containerColor = beige100,
            title = { Text("Retirer ce film ?", fontWeight = FontWeight.Bold, color = textPrimary) },
            text = { Text("\"$key\" sera retiré de votre liste.", color = textSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    FirebaseDatabase.getInstance()
                        .getReference("user_films/$userId/$key")
                        .removeValue()
                    filmToDelete = null
                }) {
                    Text("Retirer", color = errorRed, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { filmToDelete = null }) {
                    Text("Annuler", color = textSecondary)
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(beige100)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            // ── Header ──
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.verticalGradient(colors = listOf(beige300, beige200)))
                        .padding(top = 56.dp, bottom = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(CircleShape)
                                .background(brownMid),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = initiale, fontSize = 30.sp, fontWeight = FontWeight.Bold, color = beige100)
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(text = displayName, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }

            // ── Onglets ──
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tabs.forEachIndexed { index, (emoji, label, count) ->
                        val isSelected = selectedTab == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) brownMid else beige200)
                                .clickable { selectedTab = index }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = emoji, fontSize = 18.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$count",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isSelected) beige100 else brownDark
                                )
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isSelected) beige100.copy(alpha = 0.8f) else textSecondary
                                )
                            }
                        }
                    }
                }
            }

            // ── Liste ──
            if (isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = brownMid)
                    }
                }
            } else if (currentList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)
                            .clip(RoundedCornerShape(16.dp)).background(beige200).padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (selectedTab) {
                                0 -> "Votre collection est vide."
                                1 -> "Votre watchlist est vide."
                                2 -> "Aucun film à vendre."
                                else -> ""
                            },
                            textAlign = TextAlign.Center, color = textSecondary,
                            fontSize = 14.sp, lineHeight = 22.sp
                        )
                    }
                }
            } else {
                items(currentList) { filmKey ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 4.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White)
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)).background(beige200),
                                contentAlignment = Alignment.Center
                            ) { Text(currentEmoji, fontSize = 16.sp) }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = filmKey, fontWeight = FontWeight.Medium, fontSize = 15.sp, color = textPrimary)
                        }
                        IconButton(
                            onClick = { filmToDelete = Pair(filmKey, tabs[selectedTab].second) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Retirer",
                                tint = errorRed.copy(alpha = 0.6f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // ── Bouton Log out ──
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Brush.verticalGradient(colors = listOf(Color.Transparent, beige100, beige100), startY = 0f, endY = 80f))
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            OutlinedButton(
                onClick = { auth.signOut(); onLogout() },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = errorRed),
                border = androidx.compose.foundation.BorderStroke(1.dp, errorRed.copy(alpha = 0.4f))
            ) {
                Text("Log out", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }
        }
    }
}