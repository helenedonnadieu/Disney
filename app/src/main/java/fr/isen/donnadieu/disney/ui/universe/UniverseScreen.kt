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

// ── Couleurs par catégorie ────────────────────────────────────────────────────
private val CategoryColors = mapOf(
    "Grandes Sagas"                      to Color(0xFFD4A017), // Doré
    "Autres Franchises Disney"           to Color(0xFF1565C0), // Bleu Disney
    "Autres Franchises 20th Century Studios" to Color(0xFFC62828), // Rouge Fox
    "Autres Franchises Marvel"           to Color(0xFFB71C1C), // Rouge Marvel
    "Touchstone"                         to Color(0xFF2E7D32), // Vert
    "Dimension"                          to Color(0xFF6A1B9A), // Violet
    "Franchises Internationales"         to Color(0xFF00838F)  // Teal
)

data class Franchise(val nom: String = "", val categorie: String = "")
data class CategorieGroup(val nom: String, val franchises: List<Franchise>)

@Composable
fun UniverseScreen(onFranchiseClick: (String) -> Unit) {
    var categories by remember { mutableStateOf<List<CategorieGroup>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }

    // Si recherche active → liste plate filtrée, sinon → groupes par catégorie
    val filteredCategories = remember(categories, searchQuery) {
        if (searchQuery.isBlank()) categories
        else categories.map { group ->
            group.copy(franchises = group.franchises.filter {
                it.nom.contains(searchQuery, ignoreCase = true)
            })
        }.filter { it.franchises.isNotEmpty() }
    }

    LaunchedEffect(Unit) {
        val db = FirebaseDatabase.getInstance().getReference("categories")
        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val groups = mutableListOf<CategorieGroup>()
                for (category in snapshot.children) {
                    val catNom = category.child("categorie").getValue(String::class.java) ?: ""
                    val list = mutableListOf<Franchise>()
                    for (franchise in category.child("franchises").children) {
                        val nom = franchise.child("nom").getValue(String::class.java) ?: ""
                        if (nom.isNotEmpty()) list.add(Franchise(nom, catNom))
                    }
                    if (list.isNotEmpty()) groups.add(CategorieGroup(catNom, list))
                }
                categories = groups
                isLoading = false
            }
            override fun onCancelled(error: DatabaseError) { isLoading = false }
        })
    }

    Box(modifier = Modifier.fillMaxSize().background(Beige100)) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Header ────────────────────────────────────────────────────────
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
                        modifier = Modifier.fillMaxWidth().height(180.dp),
                        contentScale = ContentScale.Crop
                    )
                    // ── Barre de recherche pleine largeur ─────────────────────
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text("Rechercher une franchise…", fontSize = 13.sp, color = TextSecondary)
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null,
                                tint = BrownLight, modifier = Modifier.size(18.dp))
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, color = BrownDark),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = BrownMid,
                            unfocusedBorderColor = Beige300,
                            cursorColor = BrownMid
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    )
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
                    contentPadding = PaddingValues(bottom = 90.dp, top = 8.dp)
                ) {
                    filteredCategories.forEach { group ->
                        val accentColor = CategoryColors[group.nom] ?: BrownMid

                        // ── Label catégorie ───────────────────────────────────
                        item {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(4.dp)
                                        .height(18.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(accentColor)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = group.nom.uppercase(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = accentColor,
                                    letterSpacing = 1.5.sp
                                )
                            }
                        }

                        // ── Franchises de la catégorie ────────────────────────
                        items(group.franchises) { franchise ->
                            FranchiseCard(
                                franchise = franchise,
                                accentColor = accentColor,
                                onClick = { onFranchiseClick(franchise.nom) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FranchiseCard(franchise: Franchise, accentColor: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            // ── Logo avec bordure colorée ─────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accentColor.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                when (franchise.nom) {
                    "Star Wars", "Indiana Jones" ->
                        Image(painterResource(R.drawable.lucas_film), null,
                            contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    "Marvel Cinematic Universe" ->
                        Image(painterResource(R.drawable.marvel_studio), null,
                            contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    "Anna et Viktor","Hamilton","Les Football Kings","Lili, la Petite Sorcière",
                    "Risto Räppääjä","Les Instables" ->
                        Image(painterResource(R.drawable.buena_vista), null,
                            contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    "The Last Warrior" ->
                        Image(painterResource(R.drawable.disney), null,
                            contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    "X-Men Cinematic Universe","Spider-Man","Blade",
                    "Les Quatre Fantastiques","Men in Black" ->
                        Image(painterResource(R.drawable.marvel), null,
                            contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    "Les Muppets" ->
                        Image(painterResource(R.drawable.the_muppets_studio), null,
                            contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    "Charlie Chan","Baaghi","Les Cavaliers de la Sauge Pourprée","Quirt & Flagg" ->
                        Image(painterResource(R.drawable.fox_film), null,
                            contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    "Ernest","Sexy Dance" ->
                        Image(painterResource(R.drawable.touch_stone), null,
                            contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    "Trois Couleurs" ->
                        Image(painterResource(R.drawable.miramax), null,
                            contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    "Highlander","Halloween","Scary Movie","Scream","Spy Kids" ->
                        Image(painterResource(R.drawable.dimension_film), null,
                            contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    "Pirates des Caraïbes","Flubber","Kingsman","La Coccinelle","Tron",
                    "Benjamin Gates","Dexter Riley","Freaky Friday","L'Incroyable Voyage",
                    "La Montagne Ensorcelée","Le Monde de Narnia","Les Petits Champions",
                    "Shaggy Dog","Super Noël" ->
                        Image(painterResource(R.drawable.walt_disney), null,
                            contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    "La Planète des Singes","Alien et Predator","Die Hard","Avatar",
                    "Alvin et les Chipmunks","Big Mamma","Cisco Kid","Docteur Dolittle",
                    "Flicka","Hercule Poirot","Independence Day","Jones Family",
                    "Journal d'un Dégonflé","L'Inspecteur Hornleigh","La Malédiction",
                    "La Mouche","La Nuit au Musée","Le Labyrinthe","Maman, J'ai Raté l'Avion",
                    "Michael Shayne","Mr. Belvédère","Mr. Moto","Percy Jackson","Porky's",
                    "Taken","Treize à la Douzaine" ->
                        Image(painterResource(R.drawable.century_fox), null,
                            contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    else -> Text("✨", fontSize = 18.sp)
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
            tint = accentColor.copy(alpha = 0.6f),
            modifier = Modifier.size(20.dp)
        )
    }
}