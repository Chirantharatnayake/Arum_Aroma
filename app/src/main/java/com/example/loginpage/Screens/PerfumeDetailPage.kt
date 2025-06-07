package com.example.loginpage.Screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.ViewCompat
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.loginpage.data.DataSource
import com.example.loginpage.data.FavoriteManager
import com.example.loginpage.data.CartManager

@Composable
fun PerfumeDetailScreen(navController: NavController, perfumeId: Int) {
    // Fetch perfume details by ID
    val perfume = remember { DataSource().getPerfumeById(perfumeId) }
    val scrollState = rememberScrollState()

    // If perfume not found, show fallback message
    if (perfume == null) {
        Text("Perfume not found.", modifier = Modifier.padding(16.dp))
        return
    }

    // Load string resources and favorite state
    val name = stringResource(id = perfume.nameResId)
    val description = stringResource(id = perfume.descriptionResId)
    val isFavorite = remember { mutableStateOf(FavoriteManager.isFavorite(perfume.nameResId)) }

    // Determine gender tag based on ID
    val genderTag = if (perfumeId <= 14) "Men's" else "Women's"
    val tagColor = if (genderTag == "Men's") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary

    // Handle top padding (status bar safe area)
    val view = LocalView.current
    val topPadding = with(LocalDensity.current) {
        ViewCompat.getRootWindowInsets(view)?.systemGestureInsets?.top?.toDp() ?: 24.dp
    }

    // Main container
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 20.dp)
                .padding(top = topPadding + 12.dp, bottom = 80.dp)
        ) {
            // Back arrow and heading
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                IconButton(onClick = { navController.navigate("order") }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Perfume Details",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Perfume image card
            Card(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .shadow(10.dp, RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp)),
            ) {
                Image(
                    painter = painterResource(id = perfume.imageResId),
                    contentDescription = name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Perfume name
            Text(
                text = name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Gender label
            Text(
                text = genderTag,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .background(color = tagColor, shape = RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Perfume description
            Text(
                text = description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Perfume price
            Text(
                text = "Price: Rs. ${perfume.price}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.tertiary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Favorite button
            Button(
                onClick = {
                    FavoriteManager.toggleFavorite(perfume.nameResId)
                    isFavorite.value = !isFavorite.value
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFavorite.value) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(
                    imageVector = if (isFavorite.value) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isFavorite.value) "Remove from Favorites" else "Add to Favorites",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Add to cart button
            Button(
                onClick = {
                    CartManager.addToCart(perfume)
                    navController.navigate("cart")
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = "Add to Cart",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onTertiary
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Bottom navigation bar
        BottomNavigationBarPerfume(
            navController = navController,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun BottomNavigationBarPerfume(navController: NavController, modifier: Modifier = Modifier) {
    val items = listOf("Home", "Favorites", "Cart", "Profile")
    val icons = listOf(Icons.Filled.Home, Icons.Filled.Favorite, Icons.Filled.ShoppingCart, Icons.Filled.Person)
    val routes = listOf("home", "favorites", "cart", "profile")

    // Bottom navigation layout
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier.fillMaxWidth().height(70.dp)
    ) {
        val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

        // Navigation items
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                icon = { Icon(icons[index], contentDescription = item) },
                label = { Text(item) },
                selected = currentRoute == routes[index],
                onClick = {
                    navController.navigate(routes[index]) {
                        popUpTo("home") { inclusive = false }
                        launchSingleTop = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.outline,
                    unselectedTextColor = MaterialTheme.colorScheme.outline
                )
            )
        }
    }
}
