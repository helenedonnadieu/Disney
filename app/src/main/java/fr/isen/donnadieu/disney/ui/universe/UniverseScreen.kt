package fr.isen.donnadieu.disney.ui.universe

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

data class Franchise(val nom: String = "")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UniverseScreen(onFranchiseClick: (String) -> Unit) {
    var franchises by remember { mutableStateOf<List<Franchise>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val db = FirebaseDatabase.getInstance().getReference("categories")
        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Franchise>()
                // Parcourt chaque catégorie
                for (category in snapshot.children) {
                    // Parcourt chaque franchise dans la catégorie
                    val franchisesSnap = category.child("franchises")
                    for (franchise in franchisesSnap.children) {
                        val nom = franchise.child("nom").getValue(String::class.java) ?: ""
                        if (nom.isNotEmpty()) list.add(Franchise(nom))
                    }
                }
                franchises = list
                isLoading = false
            }

            override fun onCancelled(error: DatabaseError) {
                isLoading = false
            }
        })
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Univers Disney") })
        }
    ) { innerPadding ->
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(franchises) { franchise ->
                    FranchiseCard(
                        franchise = franchise,
                        onClick = { onFranchiseClick(franchise.nom) }
                    )
                }
            }
        }
    }
}

@Composable
fun FranchiseCard(franchise: Franchise, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = franchise.nom,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}