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
    val dataSource = remember { DataSource() }
    var selectedGender by remember { mutableStateOf("Men") }
    var searchQuery by remember { mutableStateOf("") }

    val perfumes = if (selectedGender == "Men") dataSource.loadMenPerfumes() else dataSource.loadWomenPerfumes()
    val filteredPerfumes = perfumes.filter {
        stringResource(it.nameResId).contains(searchQuery, ignoreCase = true)
    }

    val cardColor = if (selectedGender == "Men") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
    val banners = remember { dataSource.loadBanners() }
    val pagerState = rememberPagerState(pageCount = { banners.size })

    val view = LocalView.current
    val topInsetPadding = with(LocalDensity.current) {
        ViewCompat.getRootWindowInsets(view)?.systemGestureInsets?.top?.toDp() ?: 24.dp
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(7000L)
            val nextPage = (pagerState.currentPage + 1) % banners.size
            pagerState.animateScrollToPage(nextPage)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(bottom = 80.dp, start = 16.dp, end = 16.dp)
                .padding(top = topInsetPadding + 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
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
                                Text("Limited time!", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                                Text(title, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                                Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(8.dp))

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
                                        Text("Claim", color = MaterialTheme.colorScheme.onPrimary)
                                    }
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
            }

            item {
                SearchBar(searchQuery) { searchQuery = it }
            }

            item {
                GenderToggle(selectedGender) { selectedGender = it }
            }

            items(filteredPerfumes) { perfume ->
                PerfumeCard(perfume = perfume, cardColor = cardColor, navController = navController)
            }
        }

        BottomNavigationBar(
            navController = navController,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun SearchBar(value: String, onValueChange: (String) -> Unit) {
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
                        Text("Search...", color = MaterialTheme.colorScheme.outline, fontSize = 14.sp)
                    }
                    innerTextField()
                }
            }
        }
    )
}

@Composable
fun GenderToggle(selectedGender: String, onGenderChange: (String) -> Unit) {
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
                Text("$gender's", color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}

@Composable
fun PerfumeCard(perfume: Perfume, cardColor: Color, navController: NavController) {
    val name = stringResource(perfume.nameResId)
    val isFavorite = FavoriteManager.isFavorite(perfume.nameResId)

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
            Image(
                painter = painterResource(id = perfume.imageResId),
                contentDescription = name,
                modifier = Modifier
                    .size(100.dp)
                    .padding(end = 8.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Rs. ${perfume.price}",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }

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
