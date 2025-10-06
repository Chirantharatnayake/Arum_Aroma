package com.example.loginpage.Screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.loginpage.R
import com.example.loginpage.data.DataSource
import com.example.loginpage.data.FavoriteManager
import com.example.loginpage.model.Perfume
import com.example.loginpage.data.ZenQuotesFetcher
import com.example.loginpage.data.UiQuote
import kotlinx.coroutines.delay
import com.example.loginpage.util.rememberIsOnline
import coil.compose.AsyncImage
import androidx.compose.ui.platform.LocalContext
import com.example.loginpage.data.LocalPerfumeCache
import com.example.loginpage.data.LocalPerfume
import com.example.loginpage.data.PerfumeJsonLoader

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

    // --- Quotes state ---
    var quotes by remember { mutableStateOf<List<UiQuote>>(emptyList()) }
    var quotesLoading by remember { mutableStateOf(true) }
    var quotesError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        quotesLoading = true
        quotesError = null
        val fetched = try {
            ZenQuotesFetcher.fetchPerfumeQuotes(limit = 8)
        } catch (e: Exception) {
            quotesError = e.message
            emptyList()
        }
        quotes = fetched
        quotesLoading = false
    }

    // Seed the shared cache with local JSON if it's empty (so search works even before opening Order page)
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        if (LocalPerfumeCache.all().isEmpty()) {
            val baseline = PerfumeJsonLoader.load(context)
            if (baseline.isNotEmpty()) LocalPerfumeCache.update(baseline)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            )
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

                // Load logo based on the active MaterialTheme (respects in-app dark toggle)
                val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
                val logoRes = if (isDarkTheme) R.drawable.whitelogo else R.drawable.blacklogo

                // App logo and name
                Image(
                    painter = painterResource(id = logoRes),
                    contentDescription = "Arum Aroma Logo",
                    modifier = Modifier.size(100.dp)
                )
                Text(
                    "Arum Aroma",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    "Essence of Elegance",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )

                // Subtle divider to polish header area
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    thickness = 1.dp,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )

                // Welcome animation
                Spacer(modifier = Modifier.height(8.dp))
                AnimatedVisibility(
                    visible = visible.value,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut()
                ) {
                    Text(
                        "Welcome Back!",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Search bar (polished look, same behavior)
                var searchQuery by remember { mutableStateOf("") }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    tonalElevation = 2.dp,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    "Search...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                textStyle = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            Icons.Default.Mic,
                            contentDescription = "Voice Search",
                            tint = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                // Inline search results from Order page data (LocalPerfumeCache)
                if (searchQuery.length >= 2) {
                    val matches = remember(searchQuery) {
                        LocalPerfumeCache.all()
                            .filter { it.name.contains(searchQuery, ignoreCase = true) }
                            .take(8)
                    }
                    if (matches.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.fillMaxWidth()) {
                                matches.forEach { lp ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { navController.navigate("detail/${lp.id}") }
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (lp.imageResId != 0) {
                                            Image(
                                                painter = painterResource(id = lp.imageResId),
                                                contentDescription = lp.name,
                                                modifier = Modifier
                                                    .size(48.dp)
                                                    .clip(RoundedCornerShape(8.dp)),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else if (!lp.imageUrl.isNullOrBlank()) {
                                            AsyncImage(
                                                model = lp.imageUrl,
                                                contentDescription = lp.name,
                                                modifier = Modifier
                                                    .size(48.dp)
                                                    .clip(RoundedCornerShape(8.dp)),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .size(48.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                            ) {}
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                lp.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            val gender = lp.gender?.replaceFirstChar { it.uppercase() }
                                            if (!gender.isNullOrBlank()) {
                                                Text(
                                                    gender,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.outline
                                                )
                                            }
                                        }

                                        Text(
                                            "Rs. ${lp.price.toInt()}",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                                }
                            }
                        }
                    }
                }

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
                                Text(
                                    "Limited time!",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    subtitle,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
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
                            Spacer(modifier = Modifier.width(12.dp))
                            Image(
                                painter = painterResource(id = imageRes),
                                contentDescription = "Banner Image",
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(10.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }

                // Pager dots indicator
                DotsIndicator(
                    totalDots = banners.size,
                    selectedIndex = pagerState.currentPage,
                    modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                )

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

                // --- Fragrance Quotes section ---
                Spacer(modifier = Modifier.height(30.dp))
                SectionHeader("Fragrance Quotes")
                when {
                    quotesLoading -> {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 100.dp),
                            shape = RoundedCornerShape(12.dp),
                            tonalElevation = 2.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(22.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Fetching inspiration…", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                    quotesError != null -> {
                        AssistChip(
                            onClick = {},
                            label = { Text("Quotes unavailable: ${quotesError}") },
                            leadingIcon = { Icon(Icons.Default.Warning, contentDescription = null) }
                        )
                    }
                    quotes.isEmpty() -> {
                        Text("No quotes available right now.", color = MaterialTheme.colorScheme.outline)
                    }
                    else -> {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(quotes) { q -> QuoteCard(q) }
                        }
                    }
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
                    Text(
                        "Shop Now",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onTertiary
                    )
                }
            }
        }

        // Bottom Nav bar
        BottomNavigationBar(
            navController = navController,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun PerfumeCardWithFavorite(
    perfume: Perfume,
    navController: NavController
) {
    val name = stringResource(id = perfume.nameResId)

    val isFav by remember {
        derivedStateOf { FavoriteManager.favoritePerfumes.contains(perfume.nameResId) }
    }
    val favColor by animateColorAsState(
        if (isFav) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
        label = "favColorAnim"
    )

    val isOnline = rememberIsOnline()

    Card(
        modifier = Modifier
            .width(160.dp)
            .height(240.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clickable { navController.navigate("detail/${perfume.id}") }
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Image area with favorite placed safely in the corner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(10.dp))
            ) {
                Image(
                    painter = painterResource(id = perfume.imageResId),
                    contentDescription = name,
                    modifier = Modifier
                        .fillMaxSize(),
                    contentScale = ContentScale.Fit
                )

                // Heart icon, contained and not overlapping text area
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    tonalElevation = 2.dp,
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(28.dp)
                ) {
                    IconButton(onClick = { FavoriteManager.toggleFavorite(perfume.nameResId) }, enabled = isOnline) {
                        Icon(
                            imageVector = if (isFav) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = favColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Name (single line to keep uniform card height)
            Text(
                name,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Fixed-height price area so every card matches height even with discounts
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                contentAlignment = Alignment.Center
            ) {
                if (perfume.originalPrice != null && perfume.originalPrice > perfume.price) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "Rs. ${perfume.originalPrice}",
                            style = MaterialTheme.typography.labelSmall.copy(textDecoration = TextDecoration.LineThrough),
                            color = MaterialTheme.colorScheme.outline,
                            maxLines = 1,
                            overflow = TextOverflow.Clip
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
                        ) {
                            Text(
                                "Rs. ${perfume.price}",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Clip
                            )
                        }
                    }
                } else {
                    Text(
                        "Rs. ${perfume.price}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Clip
                    )
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, bottom = 12.dp)
    ) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .height(3.dp)
                .width(56.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                        )
                    )
                )
        )
    }
}

@Composable
fun DotsIndicator(totalDots: Int, selectedIndex: Int, modifier: Modifier = Modifier) {
    if (totalDots <= 1) return
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalDots) { index ->
            val isSelected = index == selectedIndex
            val size = if (isSelected) 8.dp else 6.dp
            val color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
            Box(
                modifier = Modifier
                    .padding(horizontal = 3.dp)
                    .size(size)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController, modifier: Modifier = Modifier) {
    val items = listOf("Home", "Favorites", "Cart", "Profile")
    val icons = listOf(Icons.Filled.Home, Icons.Filled.Favorite, Icons.Filled.ShoppingCart, Icons.Filled.Person)
    val routes = listOf("home", "favorites", "cart", "profile")

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    // Subtle top divider for nav bar
    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        )
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier
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
}

@Composable
fun QuoteCard(q: UiQuote) {
    val gradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f),
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)
        )
    )
    Card(
        modifier = Modifier
            .width(260.dp)
            .height(140.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(14.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.FormatQuote, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Inspiration",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "\u201C${q.text}\u201D",
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "— ${q.author}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
