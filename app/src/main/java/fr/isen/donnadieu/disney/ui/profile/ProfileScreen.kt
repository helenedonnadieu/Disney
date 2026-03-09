package fr.isen.donnadieu.disney.ui.profile

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

private val Beige100      = Color(0xFFF5F0E8)
private val Beige200      = Color(0xFFEDE4D3)
private val Beige300      = Color(0xFFD9CBBA)
private val BrownDark     = Color(0xFF4A3728)
private val BrownMid      = Color(0xFF7C5C44)
private val BrownLight    = Color(0xFFA67C5B)
private val TextPrimary   = Color(0xFF2E1F14)
private val TextSecondary = Color(0xFF8C7060)
private val ErrorRed      = Color(0xFFB85C52)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(onLogout: () -> Unit) {

    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val userId = currentUser?.uid ?: return

    var ownedFilmKeys by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var filmToDelete by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(userId) {
        FirebaseDatabase.getInstance()
            .getReference("user_films/$userId")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val owned = mutableListOf<String>()
                    for (child in snapshot.children) {
                        val status = child.child("status").getValue(String::class.java)
                        if (status == "owned") owned.add(child.key ?: "")
                    }
                    ownedFilmKeys = owned
                    isLoading = false
                }
                override fun onCancelled(error: DatabaseError) { isLoading = false }
            })
    }

    filmToDelete?.let { key ->
        AlertDialog(
            onDismissRequest = { filmToDelete = null },
            shape = RoundedCornerShape(20.dp),
            containerColor = Beige100,
            title = { Text("Retirer ce film ?", fontWeight = FontWeight.Bold, color = TextPrimary) },
            text = { Text("\"$key\" sera retiré de votre collection.", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    FirebaseDatabase.getInstance()
                        .getReference("user_films/$userId/$key")
                        .removeValue()
                    filmToDelete = null
                }) { Text("Retirer", color = ErrorRed, fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = {
                TextButton(onClick = { filmToDelete = null }) {
                    Text("Annuler", color = TextSecondary)
                }
            }
        )
    }

    // Box pour superposer le bouton fixe en bas
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Beige100)
    ) {

        // ── Contenu scrollable ─────────────────────────────────────────
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 90.dp) // espace pour le bouton fixe
        ) {

            // ── Header ─────────────────────────────────────────────────
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.verticalGradient(colors = listOf(Beige300, Beige200)))
                        .padding(top = 56.dp, bottom = 36.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val initiale = (currentUser.displayName?.firstOrNull()
                            ?: currentUser.email?.firstOrNull()
                            ?: '?').uppercaseChar().toString()

                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(CircleShape)
                                .background(BrownMid),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = initiale, fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Beige100)
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = currentUser.displayName ?: "Utilisateur",
                            fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = currentUser.email ?: "", fontSize = 13.sp, color = TextSecondary)
                    }
                }
            }

            // ── Compteur ───────────────────────────────────────────────
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Beige200)
                        .padding(vertical = 18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("📀", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "${ownedFilmKeys.size}",
                            fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = BrownDark
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${if (ownedFilmKeys.size > 1) "s" else ""}  owned film${if (ownedFilmKeys.size > 1) "s" else ""}",
                            fontSize = 14.sp, color = TextSecondary, fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // ── Titre section ──────────────────────────────────────────
            item {
                Text(
                    text = "COLLECTION",
                    fontSize = 11.sp, fontWeight = FontWeight.ExtraBold,
                    color = BrownLight, letterSpacing = 2.sp,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )
            }

            // ── Films ──────────────────────────────────────────────────
            if (isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = BrownMid)
                    }
                }
            } else if (ownedFilmKeys.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Beige200)
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Votre collection est vide.\nMarquez des films comme\n\"Own on DVD\" pour les voir ici.",
                            textAlign = TextAlign.Center, color = TextSecondary,
                            fontSize = 14.sp, lineHeight = 22.sp
                        )
                    }
                }
            } else {
                items(ownedFilmKeys) { filmKey ->
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
                                modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)).background(Beige200),
                                contentAlignment = Alignment.Center
                            ) { Text("🎬", fontSize = 16.sp) }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = filmKey, fontWeight = FontWeight.Medium, fontSize = 15.sp, color = TextPrimary)
                        }
                        IconButton(onClick = { filmToDelete = filmKey }, modifier = Modifier.size(36.dp)) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Retirer",
                                tint = ErrorRed.copy(alpha = 0.6f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // ── Bouton déconnexion fixé en bas ─────────────────────────────
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Beige100, Beige100),
                        startY = 0f, endY = 80f
                    )
                )
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            OutlinedButton(
                onClick = { auth.signOut(); onLogout() },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
                border = androidx.compose.foundation.BorderStroke(1.dp, ErrorRed.copy(alpha = 0.4f))
            ) {
                Text("Log out", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }
        }
    }
}