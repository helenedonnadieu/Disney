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

    var ownedFilmKeys by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var filmToDelete by remember { mutableStateOf<String?>(null) }

    // ÉTAT POUR LE PSEUDO RÉCUPÉRÉ EN BASE DE DONNÉES
    var userPseudo by remember { mutableStateOf("Chargement...") }

    // Couleurs depuis les ressources
    val beige100 = colorResource(R.color.Beige100)
    val beige200 = colorResource(R.color.Beige200)
    val beige300 = colorResource(R.color.Beige300)
    val brownMid = colorResource(R.color.BrownMid)
    val textPrimary = colorResource(R.color.TextPrimary)
    val textSecondary = colorResource(R.color.TextSecondary)
    val brownLight = colorResource(R.color.BrownLight)
    val brownDark = colorResource(R.color.BrownDark)
    val errorRed = Color(0xFFB85C52)

    LaunchedEffect(userId) {
        val db = FirebaseDatabase.getInstance()

        // 1. RÉCUPÉRATION DU PSEUDO (Vérifiez si votre clé est "pseudo" ou "username")
        db.getReference("users/$userId/pseudo")
            .get().addOnSuccessListener { snapshot ->
                val fetchedPseudo = snapshot.getValue(String::class.java)
                userPseudo = fetchedPseudo ?: (currentUser.displayName ?: "Utilisateur")
            }.addOnFailureListener {
                userPseudo = currentUser.displayName ?: "Utilisateur"
            }

        // 2. RÉCUPÉRATION DES FILMS
        db.getReference("user_films/$userId")
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

    // Dialogue de suppression
    filmToDelete?.let { key ->
        AlertDialog(
            onDismissRequest = { filmToDelete = null },
            shape = RoundedCornerShape(20.dp),
            containerColor = beige100,
            title = { Text("Retirer ce film ?", fontWeight = FontWeight.Bold, color = textPrimary) },
            text = { Text("\"$key\" sera retiré de votre collection.", color = textSecondary) },
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
            // ── Header (Pseudo & Email) ──
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.verticalGradient(colors = listOf(beige300, beige200)))
                        .padding(top = 56.dp, bottom = 36.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {

                        // L'initiale basée sur le pseudo récupéré
                        val initiale = userPseudo.firstOrNull()?.uppercaseChar()?.toString() ?: "?"

                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(CircleShape)
                                .background(brownMid),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initiale,
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Bold,
                                color = beige100
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // AFFICHAGE DU PSEUDO (Variable userPseudo)
                        Text(
                            text = userPseudo,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Email en petit (facultatif)
                        Text(
                            text = currentUser.email ?: "",
                            fontSize = 13.sp,
                            color = textSecondary
                        )
                    }
                }
            }

            // ── Compteur ──
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(beige200)
                        .padding(vertical = 18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("📀", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "${ownedFilmKeys.size}",
                            fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = brownDark
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "owned film${if (ownedFilmKeys.size > 1) "s" else ""}",
                            fontSize = 14.sp, color = textSecondary, fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            item {
                Text(
                    text = "COLLECTION",
                    fontSize = 11.sp, fontWeight = FontWeight.ExtraBold,
                    color = brownLight, letterSpacing = 2.sp,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )
            }

            // ── Liste des films ──
            if (isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = brownMid)
                    }
                }
            } else if (ownedFilmKeys.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)
                            .clip(RoundedCornerShape(16.dp)).background(beige200).padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Votre collection est vide.",
                            textAlign = TextAlign.Center, color = textSecondary,
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
                                modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)).background(beige200),
                                contentAlignment = Alignment.Center
                            ) { Text("🎬", fontSize = 16.sp) }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = filmKey, fontWeight = FontWeight.Medium, fontSize = 15.sp, color = textPrimary)
                        }
                        IconButton(onClick = { filmToDelete = filmKey }, modifier = Modifier.size(36.dp)) {
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