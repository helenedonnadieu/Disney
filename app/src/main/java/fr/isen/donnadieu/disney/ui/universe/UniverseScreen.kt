package fr.isen.donnadieu.disney.ui.universe

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
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

private val CategoryColors = mapOf(
    "Grandes Sagas"                          to Color(0xFFD4A017),
    "Autres Franchises Disney"               to Color(0xFF1565C0),
    "Autres Franchises 20th Century Studios" to Color(0xFFC62828),
    "Autres Franchises Marvel"               to Color(0xFFB71C1C),
    "Touchstone"                             to Color(0xFF2E7D32),
    "Dimension"                              to Color(0xFF6A1B9A),
    "Franchises Internationales"             to Color(0xFF00838F)
)

data class Franchise(val nom: String = "", val categorie: String = "")
data class CategorieGroup(val nom: String, val franchises: List<Franchise>)

@Composable
fun UniverseScreen(onFranchiseClick: (String) -> Unit) {
    var categories  by remember { mutableStateOf<List<CategorieGroup>>(emptyList()) }
    var isLoading   by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var searchFocused by remember { mutableStateOf(false) }

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

            // ── Header compact ────────────────────────────────────────────────
            Box(modifier = Modifier.fillMaxWidth()) {

                // Banner réduit à 120dp
                Image(
                    painter = painterResource(id = R.drawable.disney_banner),
                    contentDescription = "Disney Universe Banner",
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    contentScale = ContentScale.Crop
                )

                // Dégradé sombre en bas du banner pour lisibilité
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.35f))
                            )
                        )
                )

                // Barre de recherche qui chevauche le banner (overlap)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .align(Alignment.BottomCenter)
                        .offset(y = 26.dp)  // ← dépasse sur le contenu en dessous
                ) {
                    val borderColor by animateColorAsState(
                        targetValue = if (searchFocused) BrownMid else Color.Transparent,
                        animationSpec = tween(200), label = "border"
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(8.dp, RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = if (searchFocused) BrownMid else BrownLight,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontSize = 14.sp,
                                color = BrownDark,
                                fontWeight = FontWeight.Medium
                            ),
                            decorationBox = { inner ->
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        "Rechercher une franchise…",
                                        fontSize = 14.sp,
                                        color = TextSecondary
                                    )
                                }
                                inner()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .onFocusChanged { searchFocused = it.isFocused }
                        )
                        if (searchQuery.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Beige300)
                                    .clickable { searchQuery = "" },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("×", fontSize = 14.sp, color = BrownDark, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Espace pour compenser l'overlap de la searchbar
            Spacer(modifier = Modifier.height(38.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BrownMid)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(bottom = 90.dp, top = 4.dp)
                ) {
                    filteredCategories.forEach { group ->
                        val accentColor = CategoryColors[group.nom] ?: BrownMid

                        item {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(4.dp).height(18.dp)
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
                                Spacer(modifier = Modifier.width(8.dp))
                                // Nombre de franchises dans la catégorie
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(accentColor.copy(alpha = 0.12f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${group.franchises.size}",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = accentColor
                                    )
                                }
                            }
                        }

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
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Animation au clic : légère réduction de taille
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(100),
        label = "scale"
    )
    // Animation couleur de fond au clic
    val bgColor by animateColorAsState(
        targetValue = if (isPressed) accentColor.copy(alpha = 0.06f) else Color.White,
        animationSpec = tween(100),
        label = "bg"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .shadow(if (isPressed) 0.dp else 2.dp, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            // Logo avec fond teinté
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.10f)),
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

        // Flèche avec fond coloré
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(accentColor.copy(alpha = if (isPressed) 0.20f else 0.10f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}