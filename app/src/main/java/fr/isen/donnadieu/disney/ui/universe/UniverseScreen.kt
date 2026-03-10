package fr.isen.donnadieu.disney.ui.universe

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

data class Franchise(val nom: String = "")

@Composable
fun UniverseScreen(onFranchiseClick: (String) -> Unit) {
    var franchises by remember { mutableStateOf<List<Franchise>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredFranchises = remember(franchises, searchQuery) {
        if (searchQuery.isBlank()) franchises
        else franchises.filter { it.nom.contains(searchQuery, ignoreCase = true) }
    }

    LaunchedEffect(Unit) {
        val db = FirebaseDatabase.getInstance().getReference("categories")
        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Franchise>()
                for (category in snapshot.children) {
                    for (franchise in category.child("franchises").children) {
                        val nom = franchise.child("nom").getValue(String::class.java) ?: ""
                        if (nom.isNotEmpty()) list.add(Franchise(nom))
                    }
                }
                franchises = list
                isLoading = false
            }
            override fun onCancelled(error: DatabaseError) { isLoading = false }
        })
    }

    Box(modifier = Modifier.fillMaxSize().background(Beige100)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // --- HEADER ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(colors = listOf(Beige300, Beige200)))
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.disney_banner),
                        contentDescription = "Disney Universe Banner",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentScale = ContentScale.Crop
                    )

                    // --- BARRE DE RECHERCHE ---
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp, bottom = 10.dp, end = 16.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = {
                                Text(
                                    text = "Rechercher…",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = BrownLight,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontSize = 13.sp,
                                color = Color.Black
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = BrownMid,
                                unfocusedBorderColor = Beige300,
                                cursorColor = BrownMid,
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black
                            ),
                            modifier = Modifier.width(200.dp)
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
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(filteredFranchises) { franchise ->
                        FranchiseCard(
                            franchise = franchise,
                            onClick = { onFranchiseClick(franchise.nom) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FranchiseCard(franchise: Franchise, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Beige200),
                contentAlignment = Alignment.Center
            ) {
                when (franchise.nom) {
                    "Star Wars","Indiana Jones" -> {
                        Image(painter = painterResource(id = R.drawable.lucas_film), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    }
                    "Marvel Cinematic Universe" -> {
                        Image(painter = painterResource(id = R.drawable.marvel_studio), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    }
                    "Anna et Viktor","Hamilton","Les Football Kings","Lili, la Petite Sorcière","Risto Räppääjä","Les Instables"-> {
                        Image(painter = painterResource(id = R.drawable.buena_vista), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    }
                    "The Last Warrior"-> {
                        Image(painter = painterResource(id = R.drawable.disney), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    }
                    "X-Men Cinematic Universe","Spider-Man","Blade","Les Quatre Fantastiques","Men in Black" -> {
                        Image(painter = painterResource(id = R.drawable.marvel), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    }
                    "Les Muppets"-> {
                        Image(painter = painterResource(id = R.drawable.the_muppets_studio), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    }
                    "Charlie Chan","Baaghi","Les Cavaliers de la Sauge Pourprée","Quirt & Flagg"-> {
                        Image(painter = painterResource(id = R.drawable.fox_film), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    }
                    "Ernest","Sexy Dance"-> {
                        Image(painter = painterResource(id = R.drawable.touch_stone), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    }
                    "Trois Couleurs"-> {
                        Image(painter = painterResource(id = R.drawable.miramax), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    }
                    "Highlander","Halloween","Scary Movie","Scream","Spy Kids" -> {
                        Image(painter = painterResource(id = R.drawable.dimension_film), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    }
                    "Pirates des Caraïbes","Flubber","Kingsman","La Coccinelle","Tron","Benjamin Gates","Dexter Riley","Freaky Friday","L'Incroyable Voyage","La Montagne Ensorcelée","Le Monde de Narnia","Les Petits Champions","Shaggy Dog","Super Noël" -> {
                        Image(painter = painterResource(id = R.drawable.walt_disney), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    }
                    "La Planète des Singes", "Alien et Predator", "Die Hard", "Avatar","Alvin et les Chipmunks","Big Mamma","Cisco Kid","Docteur Dolittle","Flicka","Hercule Poirot","Independence Day","Jones Family","Journal d'un Dégonflé","Kigsman","L'Inspecteur Hornleigh","La Malédiction","La Mouche", "La Nuit au Musée", "Le Labyrinthe","Maman, J'ai Raté l'Avion","Michael Shayne","Mr. Belvédère","Mr. Moto","Percy Jackson","Porky's","Taken","Treize à la Douzaine"-> {
                        Image(painter = painterResource(id = R.drawable.century_fox), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    }
                    else -> {
                        Text(text = "✨", fontSize = 18.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = franchise.nom,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = TextPrimary
            )
        }
        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = BrownLight,
            modifier = Modifier.size(20.dp)
        )
    }
}