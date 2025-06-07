package com.example.loginpage.Screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.loginpage.R
import com.example.loginpage.data.DataSource
import com.example.loginpage.data.FavoriteManager
import com.example.loginpage.model.Perfume
import kotlinx.coroutines.delay

val StylishFont = FontFamily.Default

@Composable
fun HomePage(navController: NavController) {
    // Load data
    val newArrivals = remember { DataSource().loadNewArrivals() }
    val discountedPerfumes = remember { DataSource().loadDiscountedPerfumes() }
    val banners = remember { DataSource().loadBanners() }

    // Pager setup for rotating banners
    val pagerState = rememberPagerState(pageCount = { banners.size })

    // Auto-scroll banner every 3 seconds
    LaunchedEffect(Unit) {
        while (true) {
            delay(3000)
            val nextPage = (pagerState.currentPage + 1) % banners.size
            pagerState.animateScrollToPage(nextPage, animationSpec = tween(600))
        }
    }

    // Animated Welcome Message setup
    val visible = remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible.value = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surfaceVariant)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 80.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                // Load logo based on dark mode
                val isDarkTheme = isSystemInDarkTheme()
                val logoRes = if (isDarkTheme) R.drawable.whitelogo else R.drawable.blacklogo

                // App logo and name
                Image(
                    painter = painterResource(id = logoRes),
                    contentDescription = "Arum Aroma Logo",
                    modifier = Modifier.size(100.dp)
                )
                Text("Arum Aroma", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.onBackground)
                Text("Essence of Elegance", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)

                // Welcome animation
                Spacer(modifier = Modifier.height(8.dp))
                AnimatedVisibility(
                    visible = visible.value,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut()
                ) {
                    Text("Welcome Back!", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Search bar
                BasicTextField(
                    value = "",
                    onValueChange = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    decorationBox = { innerTextField ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.outline)
                            Spacer(modifier = Modifier.width(8.dp))
                            Box {
                                Text("Search...", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                                innerTextField()
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Banner pager with promotion and CTA
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .padding(vertical = 16.dp)
                ) { page ->
                    val banner = banners[page]
                    val title = stringResource(id = banner.titleResId)
                    val subtitle = stringResource(id = banner.subtitleResId)
                    val imageRes = banner.imageResId

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                            .shadow(4.dp, RoundedCornerShape(16.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Limited time!", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                                Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { navController.navigate("detail/${banner.perfumeId}") },
                                    colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary)
                                ) {
                                    Text("Claim", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimary)
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Image(
                                painter = painterResource(id = imageRes),
                                contentDescription = "Banner Image",
                                modifier = Modifier.size(100.dp),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Homepage description text
                Text(
                    text = stringResource(id = R.string.homepage_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )

                // Product sections
                Spacer(modifier = Modifier.height(30.dp))
                SectionHeader("New Arrivals")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(newArrivals) { perfume -> PerfumeCardWithFavorite(perfume, navController) }
                }

                Spacer(modifier = Modifier.height(30.dp))
                SectionHeader("Discounted Perfumes")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(discountedPerfumes) { perfume -> PerfumeCardWithFavorite(perfume, navController) }
                }

                // CTA button to order page
                Spacer(modifier = Modifier.height(30.dp))
                Button(
                    onClick = { navController.navigate("order") },
                    colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.tertiary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp)
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Shop Now", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onTertiary)
                }
            }
        }

        // Bottom Nav bar
        BottomNavigationBar(navController = navController, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
fun PerfumeCardWithFavorite(perfume: Perfume, navController: NavController) {
    val name = stringResource(id = perfume.nameResId)
    val isFav = remember { mutableStateOf(FavoriteManager.isFavorite(perfume.nameResId)) }

    // Animate icon color and size based on favorite state
    val favColor by animateColorAsState(if (isFav.value) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline)
    val scale by animateFloatAsState(if (isFav.value) 1.2f else 1f)

    Card(
        modifier = Modifier
            .width(160.dp)
            .height(230.dp)
            .shadow(8.dp, RoundedCornerShape(12.dp))
            .clickable { navController.navigate("detail/${perfume.id}") },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                // Perfume image
                Image(
                    painter = painterResource(id = perfume.imageResId),
                    contentDescription = name,
                    modifier = Modifier
                        .size(100.dp)
                        .padding(bottom = 10.dp),
                    contentScale = ContentScale.Crop
                )

                // Name & price
                Text(name, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)

                if (perfume.originalPrice != null && perfume.originalPrice > perfume.price) {
                    // Show original + discounted price
                    Text("Rs. ${perfume.originalPrice}", style = MaterialTheme.typography.labelSmall.copy(textDecoration = TextDecoration.LineThrough), color = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Rs. ${perfume.price}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                } else {
                    Text("Rs. ${perfume.price}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                }
            }

            // Favorite toggle button
            Icon(
                imageVector = if (isFav.value) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                contentDescription = "Favorite",
                tint = favColor,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size((22.dp * scale).coerceAtMost(30.dp))
                    .clickable {
                        FavoriteManager.toggleFavorite(perfume.nameResId)
                        isFav.value = !isFav.value
                    }
            )
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        color = MaterialTheme.colorScheme.onBackground,
        fontSize = 22.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, bottom = 12.dp)
    )
}

@Composable
fun BottomNavigationBar(navController: NavController, modifier: Modifier = Modifier) {
    val items = listOf("Home", "Favorites", "Cart", "Profile")
    val icons = listOf(Icons.Filled.Home, Icons.Filled.Favorite, Icons.Filled.ShoppingCart, Icons.Filled.Person)
    val routes = listOf("home", "favorites", "cart", "profile")

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
    ) {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                icon = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = icons[index], contentDescription = item, modifier = Modifier.size(24.dp))
                        Text(text = item, style = MaterialTheme.typography.labelSmall)
                    }
                },
                selected = currentRoute == routes[index],
                onClick = {
                    navController.navigate(routes[index]) {
                        popUpTo("home") { inclusive = false }
                        launchSingleTop = true
                    }
                },
                alwaysShowLabel = true,
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
