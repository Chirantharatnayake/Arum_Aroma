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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.ViewCompat
import androidx.navigation.NavController
import com.example.loginpage.data.DataSource
import com.example.loginpage.data.FavoriteManager
import com.example.loginpage.model.Perfume

@Composable
fun FavouritesScreen(navController: NavController) {
    // Load all perfumes and filter only favorites
    val dataSource = remember { DataSource() }
    val allPerfumes = dataSource.loadMenPerfumes() + dataSource.loadWomenPerfumes()
    val favoritePerfumes = allPerfumes.filter { FavoriteManager.isFavorite(it.nameResId) }

    // Get system top inset (e.g., status bar) for safe top padding
    val view = LocalView.current
    val topPadding = with(LocalDensity.current) {
        ViewCompat.getRootWindowInsets(view)?.systemGestureInsets?.top?.toDp() ?: 24.dp
    }

    // Main screen layout
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(top = topPadding + 12.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Header row with title and heart icon
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

            // If no favorites found, show placeholder message
            if (favoritePerfumes.isEmpty()) {
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
                // List of favorite perfume cards
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 80.dp, start = 16.dp, end = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(favoritePerfumes) { perfume ->
                        FavouritePerfumeCard(perfume) {
                            // Navigate to detail page on card click
                            navController.navigate("detail/${perfume.id}")
                        }
                    }
                }
            }
        }

        // Bottom navigation bar stays fixed
        BottomNavigationBar(
            navController = navController,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun FavouritePerfumeCard(perfume: Perfume, onClick: () -> Unit) {
    val name = stringResource(perfume.nameResId)

    // Card layout for each favorite perfume
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(12.dp))
            .clickable { onClick() }, // Trigger passed onClick lambda
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Perfume image
            Image(
                painter = painterResource(id = perfume.imageResId),
                contentDescription = name,
                modifier = Modifier
                    .size(100.dp)
                    .padding(end = 12.dp)
            )

            // Perfume name and price
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

            // Unfavorite button (heart icon)
            IconButton(onClick = {
                FavoriteManager.toggleFavorite(perfume.nameResId) // Toggle favorite status
            }) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = "Unfavorite",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
