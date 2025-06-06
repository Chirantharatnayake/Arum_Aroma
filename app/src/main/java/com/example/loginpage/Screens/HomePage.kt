package com.example.loginpage.Screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.loginpage.R
import com.example.loginpage.data.DataSource
import com.example.loginpage.data.FavoriteManager
import com.example.loginpage.model.Perfume
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.navigation.compose.currentBackStackEntryAsState
import kotlinx.coroutines.delay
import androidx.compose.foundation.isSystemInDarkTheme


val StylishFont = FontFamily.Default

@Composable
fun HomePage(navController: NavController) {
    val newArrivals = remember { DataSource().loadNewArrivals() }
    val discountedPerfumes = remember { DataSource().loadDiscountedPerfumes() }
    val banners = remember { DataSource().loadBanners() }

    val pagerState = rememberPagerState(pageCount = { banners.size })

    LaunchedEffect(Unit) {
        while (true) {
            delay(3000)
            val nextPage = (pagerState.currentPage + 1) % banners.size
            pagerState.animateScrollToPage(nextPage, animationSpec = tween(600))
        }
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

                val isDarkTheme = isSystemInDarkTheme()
                val logoRes = if (isDarkTheme) R.drawable.whitelogo else R.drawable.blacklogo

                Image(
                    painter = painterResource(id = logoRes),
                    contentDescription = "Arum Aroma Logo",
                    modifier = Modifier.size(100.dp)
                )
                Text("Arum Aroma", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Text("Essence of Elegance", fontSize = 14.sp, color = MaterialTheme.colorScheme.outline)

                Spacer(modifier = Modifier.height(8.dp))

                BasicTextField(
                    value = "",
                    onValueChange = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    decorationBox = { innerTextField ->
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.outline)
                            Spacer(modifier = Modifier.width(8.dp))
                            Box {
                                Text("Search...", color = MaterialTheme.colorScheme.outline, fontSize = 14.sp)
                                innerTextField()
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))

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
                                Text("Limited time!", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                                Text(title, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                                Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        navController.navigate("detail/${banner.perfumeId}")
                                    },
                                    colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary)
                                ) {
                                    Text("Claim", color = MaterialTheme.colorScheme.onPrimary)
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
                Text(
                    text = stringResource(id = R.string.homepage_description),
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.outline
                )

                Spacer(modifier = Modifier.height(30.dp))
                SectionHeader("New Arrivals")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(newArrivals) { perfume ->
                        PerfumeCardWithFavorite(perfume, navController)
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))
                SectionHeader("Discounted Perfumes")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(discountedPerfumes) { perfume ->
                        PerfumeCardWithFavorite(perfume, navController)
                    }
                }

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
                    Text("Shop Now", fontSize = 18.sp, color = MaterialTheme.colorScheme.onTertiary)
                }
            }
        }

        BottomNavigationBar(navController = navController, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
fun PerfumeCardWithFavorite(perfume: Perfume, navController: NavController) {
    val name = stringResource(id = perfume.nameResId)
    val isFav = remember { mutableStateOf(FavoriteManager.isFavorite(perfume.nameResId)) }

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
                Image(
                    painter = painterResource(id = perfume.imageResId),
                    contentDescription = name,
                    modifier = Modifier
                        .size(100.dp)
                        .padding(bottom = 10.dp),
                    contentScale = ContentScale.Crop
                )
                Text(name, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)

                if (perfume.originalPrice != null && perfume.originalPrice > perfume.price) {
                    Text(
                        text = "Rs. ${perfume.originalPrice}",
                        color = MaterialTheme.colorScheme.outline,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        style = androidx.compose.ui.text.TextStyle(textDecoration = TextDecoration.LineThrough)
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Rs. ${perfume.price}",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        "Rs. ${perfume.price}",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp
                    )
                }
            }

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

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp) // slightly increased height for better vertical centering
    ) {
        val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

        items.forEachIndexed { index, item ->
            NavigationBarItem(
                icon = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Icon(
                            imageVector = icons[index],
                            contentDescription = item,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(text = item, fontSize = 12.sp)
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

