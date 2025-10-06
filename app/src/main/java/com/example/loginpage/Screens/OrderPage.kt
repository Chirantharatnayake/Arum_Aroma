package com.example.loginpage.Screens

import android.os.Build
import android.util.Log
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.ViewCompat
import androidx.navigation.NavController
import com.example.loginpage.R
import com.example.loginpage.data.GitHubPerfumeFetcher
import com.example.loginpage.data.LocalPerfume
import com.example.loginpage.data.LocalPerfumeCache
import com.example.loginpage.data.PerfumeJsonLoader
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val TAG = "OrderPageGit"

/** Local banner model with perfume images. */
data class BannerUi(
    val title: String,
    val subtitle: String,
    @DrawableRes val imageRes: Int
)

private fun currentMonth(): Int = try {
    SimpleDateFormat("M", Locale.US).format(Date()).toInt()
} catch (_: Exception) { 1 }

private fun detectSeasonByMonthAndLat(month: Int, latitude: Double): String {
    // Northern hemisphere mapping
    fun seasonFor(m: Int): String = when (m) {
        12, 1, 2 -> "Winter"
        3, 4, 5 -> "Spring"
        6, 7, 8 -> "Summer"
        else -> "Autumn"
    }
    return if (latitude >= 0) {
        seasonFor(month)
    } else {
        // Southern hemisphere ~6-month offset
        val shifted = ((month + 6 - 1) % 12) + 1
        seasonFor(shifted)
    }
}

private fun recommendForSeason(all: List<LocalPerfume>, season: String, limit: Int = 6): List<LocalPerfume> {
    val kws = when (season) {
        "Winter" -> listOf("amber", "vanilla", "oud", "spice", "spicy", "leather", "noir", "intense", "warm", "wood")
        "Summer" -> listOf("aqua", "marine", "blue", "citrus", "fresh", "cool", "sport", "ocean", "light")
        "Spring" -> listOf("rose", "bloom", "floral", "garden", "petal", "lily", "blossom", "spring")
        else -> listOf("wood", "cedar", "sandal", "musk", "fig", "smoke", "smoky", "amber")
    }
    val prioritized = all.filter { p ->
        val n = p.name.lowercase(Locale.getDefault())
        kws.any { kw -> n.contains(kw) }
    }
    val result = mutableListOf<LocalPerfume>()
    result += prioritized.take(limit)
    if (result.size < limit) {
        // Top up with anything else not already included
        result += all.filter { it !in result }.take(limit - result.size)
    }
    return result.take(limit)
}

@Composable
fun OrderPage(navController: NavController) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()

    // Online/Offline state
    val isOnline = rememberIsOnline()

    // UI state
    var selectedGender by remember { mutableStateOf("Men") }
    var searchQuery by rememberSaveable { mutableStateOf("") }

    // Data state
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var itemsState by remember { mutableStateOf<List<LocalPerfume>>(emptyList()) }
    var source by remember { mutableStateOf("NONE") }

    // Season state
    var season by remember { mutableStateOf<String?>(null) }

    // ---- Perfume banners with actual perfume images ----
    val banners = remember {
        listOf(
            BannerUi(
                title = "New Arrivals",
                subtitle = "Fresh picks curated for you",
                imageRes = R.drawable.mperfume1
            ),
            BannerUi(
                title = "Top Sellers",
                subtitle = "Fan favorites this week",
                imageRes = R.drawable.mperfume12
            ),
            BannerUi(
                title = "Exclusive Deals",
                subtitle = "Limited-time offers—don't miss out",
                imageRes = R.drawable.wperfume12
            )
        )
    }
    val pagerState = rememberPagerState(pageCount = { banners.size })

    // Safe area padding
    val view = LocalView.current
    val topInsetPadding = with(LocalDensity.current) {
        ViewCompat.getRootWindowInsets(view)?.systemGestureInsets?.top?.toDp() ?: 24.dp
    }

    // Auto-scroll banners
    LaunchedEffect(Unit) {
        while (true) {
            delay(7000L)
            val nextPage = (pagerState.currentPage + 1) % banners.size
            pagerState.animateScrollToPage(nextPage)
        }
    }

    // ---- Load data (Local baseline + GitHub when online) ----
    LaunchedEffect(isOnline) {
        loading = true
        error = null

        val combined = mutableListOf<LocalPerfume>()
        val local = PerfumeJsonLoader.load(context)
        if (local.isNotEmpty()) {
            combined += local
            Log.i(TAG, "Loaded ${local.size} perfumes from LOCAL baseline")
        }

        if (isOnline) {
            // GitHub JSON
            val remote = GitHubPerfumeFetcher.fetch(context)
            if (remote.isNotEmpty()) {
                combined += remote
                Log.i(TAG, "Loaded ${remote.size} perfumes from GITHUB (online)")
            } else {
                Log.w(TAG, "GitHub JSON returned empty")
            }
            source = buildString {
                append("LOCAL")
                if (remote.isNotEmpty()) append("+GitHub")
            }
        } else {
            source = "LOCAL(Offline)"
            Log.i(TAG, "Offline: using LOCAL only")
        }

        // Deduplicate by id OR by normalized (name+gender), preserve first occurrence
        val seenIds = HashSet<Int>()
        val seenNameGender = HashSet<String>()
        val deduped = ArrayList<LocalPerfume>(combined.size)
        combined.forEach { p ->
            val keyNameGender =
                (p.name.trim().lowercase()) + "|" + ((p.gender ?: "").trim().lowercase())
            val isDuplicate = (!seenIds.add(p.id)) || (!seenNameGender.add(keyNameGender))
            if (!isDuplicate) deduped.add(p)
        }
        val removed = combined.size - deduped.size
        if (removed > 0) Log.d(TAG, "Dedup: removed $removed duplicates; final=${deduped.size}")

        itemsState = deduped
        LocalPerfumeCache.update(deduped)

        Log.d(TAG, "SOURCE=$source, totalCombined=${combined.size}")
        loading = false
    }

    // Initialize season using local month-based detection (no external API)
    LaunchedEffect(Unit) {
        val lat = 58.7984 // keep northern hemisphere reference
        season = detectSeasonByMonthAndLat(currentMonth(), lat)
    }

    // Enhanced gender-based color schemes - Blue for Men, Pink for Women
    val menThemeColors = GenderThemeColors(
        primary = if (isDark) Color(0xFF4FC3F7) else Color(0xFF1976D2),
        primaryContainer = if (isDark) Color(0xFF0D47A1) else Color(0xFFE3F2FD),
        onPrimary = Color.White,
        onPrimaryContainer = if (isDark) Color.White else Color(0xFF0D47A1)
    )

    val womenThemeColors = GenderThemeColors(
        primary = if (isDark) Color(0xFFFF80AB) else Color(0xFFE91E63),
        primaryContainer = if (isDark) Color(0xFFAD1457) else Color(0xFFFCE4EC),
        onPrimary = Color.White,
        onPrimaryContainer = if (isDark) Color.White else Color(0xFFAD1457)
    )

    val currentTheme = if (selectedGender == "Men") menThemeColors else womenThemeColors

    // Filter lists strictly by gender + search (no mixing)
    val menPerfumes = remember(itemsState, searchQuery) {
        itemsState.filter { p ->
            (p.gender ?: "").equals("men", ignoreCase = true) &&
                    p.name.contains(searchQuery, ignoreCase = true)
        }
    }
    val womenPerfumes = remember(itemsState, searchQuery) {
        itemsState.filter { p ->
            (p.gender ?: "").equals("women", ignoreCase = true) &&
                    p.name.contains(searchQuery, ignoreCase = true)
        }
    }

    // Select the list to display based on toggle
    val itemsToShow = if (selectedGender == "Men") menPerfumes else womenPerfumes

    // Precompute season recommendations
    val seasonRecommended = remember(itemsState, season) {
        if (!itemsState.isNullOrEmpty() && season != null) recommendForSeason(itemsState, season!!) else emptyList()
    }
    val seasonMenTwo = remember(seasonRecommended) {
        seasonRecommended.filter { (it.gender ?: "").equals("men", ignoreCase = true) }.take(2)
    }
    val seasonWomenTwo = remember(seasonRecommended) {
        seasonRecommended.filter { (it.gender ?: "").equals("women", ignoreCase = true) }.take(2)
    }
    val seasonTwoEach = remember(seasonMenTwo, seasonWomenTwo) {
        (seasonMenTwo + seasonWomenTwo).distinctBy { it.id }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp, start = 16.dp, end = 16.dp)
                .padding(top = topInsetPadding + 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item { ProfessionalHeader() }

            // Enhanced Banner carousel with perfume images
            item {
                EnhancedBannerCarousel(
                    banners = banners,
                    pagerState = pagerState,
                    currentTheme = currentTheme
                )
            }

            // Enhanced Search Bar
            item {
                EnhancedSearchBar(
                    searchQuery = searchQuery,
                    onValueChange = { searchQuery = it },
                    currentTheme = currentTheme
                )
            }

            // Gender Toggle (switch between Men/Women)
            item {
                EnhancedGenderToggle(
                    selectedGender = selectedGender,
                    onGenderChange = { selectedGender = it },
                    menTheme = menThemeColors,
                    womenTheme = womenThemeColors
                )
            }

            // Season recommendation section (only when we have season and items)
            if (!loading && season != null && seasonTwoEach.isNotEmpty()) {
                item { SeasonRecommendationHeader(season = season!!) }
                items(seasonTwoEach) { lp ->
                    // Get seasonal highlight color based on current season - more visible in both light and dark modes
                    val seasonalCardColor = when (season!!) {
                        "Winter" -> if (isDark) Color(0xFF2196F3).copy(alpha = 0.25f) else Color(0xFFE3F2FD)
                        "Summer" -> if (isDark) Color(0xFFFF9800).copy(alpha = 0.25f) else Color(0xFFFFF3E0)
                        "Spring" -> if (isDark) Color(0xFF4CAF50).copy(alpha = 0.25f) else Color(0xFFE8F5E8)
                        "Autumn" -> if (isDark) Color(0xFFFF5722).copy(alpha = 0.25f) else Color(0xFFFBE9E7)
                        else -> if (isDark) Color(0xFF9C27B0).copy(alpha = 0.25f) else Color(0xFFF3E5F5)
                    }

                    RemotePerfumeCard(
                        perfume = lp,
                        cardColor = seasonalCardColor,
                        navController = navController
                    )
                }
            }

            // Loading / Error / Selected gender list
            when {
                loading -> {
                    item { LoadingState(currentTheme = currentTheme) }
                }
                error != null -> {
                    item { ErrorState(error = error!!) }
                }
                itemsToShow.isEmpty() -> {
                    item { EmptyState() }
                }
                else -> {
                    item {
                        GenderSectionHeader(
                            title = if (selectedGender == "Men") "Men's Fragrances" else "Women's Fragrances",
                            colors = if (selectedGender == "Men") menThemeColors else womenThemeColors
                        )
                    }
                    items(itemsToShow) { lp ->
                        RemotePerfumeCard(
                            perfume = lp,
                            cardColor = if (selectedGender == "Men") menThemeColors.primaryContainer else womenThemeColors.primaryContainer,
                            navController = navController
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

// Data class for theme colors
data class GenderThemeColors(
    val primary: Color,
    val primaryContainer: Color,
    val onPrimary: Color,
    val onPrimaryContainer: Color
)

@Composable
fun ProfessionalHeader() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Discover Fragrances",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Find your perfect scent from our curated collection",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun EnhancedBannerCarousel(
    banners: List<BannerUi>,
    pagerState: androidx.compose.foundation.pager.PagerState,
    currentTheme: GenderThemeColors
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) { page ->
            val banner = banners[page]

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                currentTheme.primary.copy(alpha = 0.9f),
                                currentTheme.primaryContainer.copy(alpha = 0.8f)
                            )
                        )
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Limited Time Offer!",
                            style = MaterialTheme.typography.labelMedium,
                            color = currentTheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            banner.title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = currentTheme.onPrimary
                        )
                        Text(
                            banner.subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = currentTheme.onPrimary.copy(alpha = 0.9f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = currentTheme.onPrimary.copy(alpha = 0.2f)
                            ),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                text = if (page == 0) "Applied" else "Claim Now",
                                color = currentTheme.onPrimary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Enhanced perfume image display
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.size(120.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.White.copy(alpha = 0.9f))
                        ) {
                            Image(
                                painter = androidx.compose.ui.res.painterResource(id = banner.imageRes),
                                contentDescription = "Perfume ${banner.title}",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EnhancedSearchBar(
    searchQuery: String,
    onValueChange: (String) -> Unit,
    currentTheme: GenderThemeColors
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        BasicTextField(
            value = searchQuery,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            decorationBox = { inner ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search",
                        tint = currentTheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        if (searchQuery.isBlank()) {
                            Text(
                                "Search for fragrances...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                        inner()
                    }
                }
            }
        )
    }
}

@Composable
fun LoadingState(currentTheme: GenderThemeColors) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                color = currentTheme.primary,
                strokeWidth = 3.dp,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                "Loading fragrances...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun ErrorState(error: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Text(
            text = error,
            color = MaterialTheme.colorScheme.onErrorContainer,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(16.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun EmptyState() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "No fragrances found",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Text(
                "Try adjusting your search or changing the category",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun SeasonRecommendationHeader(season: String) {
    val seasonColors = when (season) {
        "Winter" -> listOf(Color(0xFF1565C0), Color(0xFF0D47A1))
        "Summer" -> listOf(Color(0xFFFF6F00), Color(0xFFFF8F00))
        "Spring" -> listOf(Color(0xFF388E3C), Color(0xFF4CAF50))
        "Autumn" -> listOf(Color(0xFFD84315), Color(0xFFFF5722))
        else -> listOf(Color(0xFF6A1B9A), Color(0xFF8E24AA))
    }
    val seasonEmoji = when (season) {
        "Winter" -> "❄️"
        "Summer" -> "☀️"
        "Spring" -> "🌸"
        "Autumn" -> "🍂"
        else -> "🌿"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = seasonColors + seasonColors.map { it.copy(alpha = 0.7f) },
                        start = androidx.compose.ui.geometry.Offset(0f, 0f),
                        end = androidx.compose.ui.geometry.Offset.Infinite
                    )
                )
        ) {
            // Decorative overlay pattern
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(Color.White.copy(alpha = 0.1f), Color.Transparent),
                            radius = 800f
                        )
                    )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Seasonal icon/emoji section
                Card(
                    modifier = Modifier.size(60.dp),
                    shape = CircleShape,
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.2f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = seasonEmoji, style = MaterialTheme.typography.headlineMedium, fontSize = 28.sp)
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Perfect for $season",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        fontSize = 22.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Handpicked seasonal fragrances",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color.White.copy(alpha = 0.8f))
                    )
                }

                // Decorative element
                Card(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f))
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "✨", fontSize = 18.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun GenderSectionHeader(title: String, colors: GenderThemeColors) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.primaryContainer)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colors.onPrimaryContainer,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )
    }
}

/** Tiny in-file helper to expose a boolean online/offline state to Compose. */
@Composable
private fun rememberIsOnline(): Boolean {
    val ctx = LocalContext.current.applicationContext
    var isOnline by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val cm = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        fun computeOnline(): Boolean {
            val active = cm.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(active) ?: return false
            return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        }

        isOnline = computeOnline()

        val cb = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) { isOnline = computeOnline() }
            override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) { isOnline = computeOnline() }
            override fun onLost(network: Network) { isOnline = computeOnline() }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            cm.registerDefaultNetworkCallback(cb)
        } else {
            val req = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            cm.registerNetworkCallback(req, cb)
        }

        onDispose { runCatching { cm.unregisterNetworkCallback(cb) } }
    }

    return isOnline
}

/* ---- You must already have these composables in your project ----
   RemotePerfumeCard(...)
   BottomNavigationBar(...)
   plus the data classes & fetchers you imported.
*/

@Composable
private fun EnhancedGenderToggle(
    selectedGender: String,
    onGenderChange: (String) -> Unit,
    menTheme: GenderThemeColors,
    womenTheme: GenderThemeColors
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            listOf("Men", "Women").forEach { gender ->
                val selected = selectedGender == gender
                val theme = if (gender == "Men") menTheme else womenTheme

                Button(
                    onClick = { onGenderChange(gender) },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selected) theme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (selected) theme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = if (selected) 6.dp else 2.dp
                    )
                ) {
                    Text(
                        text = "$gender's Fragrances",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}
