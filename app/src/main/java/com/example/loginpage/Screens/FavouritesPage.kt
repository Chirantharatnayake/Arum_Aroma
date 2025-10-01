// file: com/example/loginpage/Screens/FavouritesScreen.kt
package com.example.loginpage.Screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.loginpage.data.*

@Composable
fun FavouritesScreen(navController: NavController) {
    val context = LocalContext.current

    // ---- Local (resource-based) favorites ----
    val dataSource = remember { DataSource() }
    val allPerfumes = remember { dataSource.loadMenPerfumes() + dataSource.loadWomenPerfumes() }

    // Recompute when FavoriteManager.favoritePerfumes changes
    val localFavorites by remember {
        derivedStateOf { allPerfumes.filter { FavoriteManager.isFavorite(it.nameResId) } }
    }

    // ---- Remote favorites (from SharedPreferences keys) ----
    var remoteFavorites by remember { mutableStateOf<List<LocalPerfume>>(emptyList()) }

    LaunchedEffect(Unit) {
        // load remote list (GitHub; fallback to local file)
        val remoteAll = GitHubPerfumeFetcher.fetch(context).ifEmpty {
            PerfumeJsonLoader.load(context)
        }
        val keys = FavoriteManager.getAllFavoriteKeys(context)
        val ids = keys.mapNotNull { k ->
            if (k.startsWith("remote:")) k.substringAfter("remote:").toIntOrNull() else null
        }.toSet()
        remoteFavorites = remoteAll.filter { ids.contains(it.id) }
    }

    // Top inset padding
    val view = LocalView.current
    val topPadding = with(LocalDensity.current) {
        ViewCompat.getRootWindowInsets(view)?.systemGestureInsets?.top?.toDp() ?: 24.dp
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(top = topPadding + 12.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Favourites",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = "Heart",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(28.dp)
                )
            }

            val nothingToShow = localFavorites.isEmpty() && remoteFavorites.isEmpty()

            if (nothingToShow) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No favourites yet.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 80.dp, start = 16.dp, end = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Local favorites section
                    items(localFavorites, key = { it.id }) { perfume ->
                        FavouritePerfumeCardLocal(
                            perfume = perfume,
                            onClick = { navController.navigate("detail/${perfume.id}") },
                            onUnfavorite = {
                                FavoriteManager.toggleFavorite(perfume.nameResId)
                                // derivedStateOf will refresh the list automatically
                            }
                        )
                    }

                    // Remote favorites section
                    items(remoteFavorites, key = { it.id }) { p ->
                        FavouritePerfumeCardRemote(
                            perfume = p,
                            onUnfavorite = {
                                val key = "remote:${p.id}"
                                FavoriteManager.toggleFavoriteKey(context, key)
                                // remove it from current list
                                remoteFavorites = remoteFavorites.filterNot { it.id == p.id }
                            }
                        )
                    }
                }
            }
        }

        BottomNavigationBar(
            navController = navController,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

/* -------- Cards used in the Favourites screen -------- */

@Composable
private fun FavouritePerfumeCardLocal(
    perfume: com.example.loginpage.model.Perfume,
    onClick: () -> Unit,
    onUnfavorite: () -> Unit
) {
    val name = stringResource(perfume.nameResId)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(12.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = perfume.imageResId),
                contentDescription = name,
                modifier = Modifier
                    .size(100.dp)
                    .padding(end = 12.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Rs. ${perfume.price}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onUnfavorite) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = "Unfavorite",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun FavouritePerfumeCardRemote(
    perfume: LocalPerfume,
    onUnfavorite: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (perfume.imageResId != 0) {
                Image(
                    painter = painterResource(id = perfume.imageResId),
                    contentDescription = perfume.name,
                    modifier = Modifier.size(100.dp)
                )
            } else {
                AsyncImage(
                    model = perfume.imageUrl,
                    contentDescription = perfume.name,
                    modifier = Modifier.size(100.dp),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = perfume.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Rs. ${perfume.price}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onUnfavorite) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = "Unfavorite",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
