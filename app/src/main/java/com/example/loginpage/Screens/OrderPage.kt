package com.example.loginpage.Screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
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
import com.example.loginpage.data.DataSource
import com.example.loginpage.data.FavoriteManager
import com.example.loginpage.model.Perfume
import kotlinx.coroutines.delay

@Composable
fun OrderPage(navController: NavController) {
    // Load perfume data and states
    val dataSource = remember { DataSource() }
    var selectedGender by remember { mutableStateOf("Men") }
    var searchQuery by rememberSaveable { mutableStateOf("") }

    // Load perfumes based on gender selection
    val perfumes = if (selectedGender == "Men") dataSource.loadMenPerfumes() else dataSource.loadWomenPerfumes()

    // Filter perfumes by search query
    val filteredPerfumes = perfumes.filter {
        stringResource(it.nameResId).startsWith(searchQuery, ignoreCase = true)
    }

    // Set card color depending on gender
    val cardColor = if (selectedGender == "Men") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer

    // Load promotional banners
    val banners = remember { dataSource.loadBanners() }
    val pagerState = rememberPagerState(pageCount = { banners.size })

    // Handle system top padding
    val view = LocalView.current
    val topInsetPadding = with(LocalDensity.current) {
        ViewCompat.getRootWindowInsets(view)?.systemGestureInsets?.top?.toDp() ?: 24.dp
    }

    // Auto-scroll the banner every 7 seconds
    LaunchedEffect(Unit) {
        while (true) {
            delay(7000L)
            val nextPage = (pagerState.currentPage + 1) % banners.size
            pagerState.animateScrollToPage(nextPage)
        }
    }

    // Main layout container
    Box(modifier = Modifier.fillMaxSize()) {
        // Scrollable content list
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(bottom = 80.dp, start = 16.dp, end = 16.dp)
                .padding(top = topInsetPadding + 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Banner carousel
            item {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .padding(vertical = 8.dp)
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
                            .shadow(6.dp, RoundedCornerShape(16.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Limited time!",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error
                                )

                                Text(
                                    title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Text(
                                    subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Show "Applied" or "Claim" button conditionally
                                if (page == 0) {
                                    Text(
                                        text = "Applied",
                                        color = MaterialTheme.colorScheme.outline,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )
                                } else {
                                    Button(
                                        onClick = { navController.navigate("detail/${banner.perfumeId}") },
                                        colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary)
                                    ) {
                                        Text(
                                            "Claim",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Banner image
                            Image(
                                painter = painterResource(id = imageRes),
                                contentDescription = "Banner Image",
                                modifier = Modifier.size(100.dp),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }

            // Search bar
            item {
                SearchBar(searchQuery) { searchQuery = it }
            }

            // Gender toggle buttons
            item {
                GenderToggle(selectedGender) { selectedGender = it }
            }

            // Display filtered perfumes
            items(filteredPerfumes) { perfume ->
                PerfumeCard(perfume = perfume, cardColor = cardColor, navController = navController)
            }
        }

        // Bottom navigation bar
        BottomNavigationBar(
            navController = navController,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun SearchBar(value: String, onValueChange: (String) -> Unit) {
    // Custom search bar with placeholder and icon
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
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
                    if (value.isBlank()) {
                        Text(
                            "Search...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    innerTextField()
                }
            }
        }
    )
}

@Composable
fun GenderToggle(selectedGender: String, onGenderChange: (String) -> Unit) {
    // Toggle buttons for switching between Men and Women perfumes
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        listOf("Men", "Women").forEach { gender ->
            Button(
                onClick = { onGenderChange(gender) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedGender == gender) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.surfaceDim
                ),
                modifier = Modifier.padding(horizontal = 6.dp)
            ) {
                Text(
                    "$gender's",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
fun PerfumeCard(perfume: Perfume, cardColor: Color, navController: NavController) {
    val name = stringResource(perfume.nameResId)
    val isFavorite = FavoriteManager.isFavorite(perfume.nameResId)

    // Individual perfume card layout
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(12.dp))
            .clickable { navController.navigate("detail/${perfume.id}") },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            // Perfume image
            Image(
                painter = painterResource(id = perfume.imageResId),
                contentDescription = name,
                modifier = Modifier
                    .size(100.dp)
                    .padding(end = 8.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Perfume name and price
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    name,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Rs. ${perfume.price}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }

            // Favorite button toggle
            IconButton(onClick = {
                FavoriteManager.toggleFavorite(perfume.nameResId)
            }) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}
