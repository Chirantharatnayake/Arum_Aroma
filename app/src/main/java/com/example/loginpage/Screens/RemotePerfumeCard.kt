// file: com/example/loginpage/Screens/RemotePerfumeCard.kt
package com.example.loginpage.Screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.loginpage.data.FavoriteManager
import com.example.loginpage.data.LocalPerfume
import com.example.loginpage.data.CartManager
import com.example.loginpage.util.rememberIsOnline

@Composable
fun RemotePerfumeCard(
    perfume: LocalPerfume,
    cardColor: androidx.compose.ui.graphics.Color,
    navController: NavController
) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()
    val favKey = "remote:${perfume.id}"

    val isOnline = rememberIsOnline()

    // reflect current persisted state
    val isFavoriteState = remember(favKey) {
        mutableStateOf(FavoriteManager.isFavoriteKey(context, favKey))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(12.dp))
            .clickable { navController.navigate("detail/${perfume.id}") },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(12.dp)) {

            if (perfume.imageResId != 0) {
                Image(
                    painter = painterResource(id = perfume.imageResId),
                    contentDescription = perfume.name,
                    modifier = Modifier.size(100.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                AsyncImage(
                    model = perfume.imageUrl,
                    contentDescription = perfume.name,
                    modifier = Modifier.size(100.dp),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    perfume.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (isDark) Color.White else Color.Black,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Rs. ${perfume.price}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isDark) Color(0xFFE0E0E0) else Color(0xFF424242),
                    fontWeight = FontWeight.Medium
                )
            }

            // ❤️ Favorite toggle - disabled when offline
            IconButton(
                onClick = {
                    val wasFavorite = isFavoriteState.value
                    FavoriteManager.toggleFavoriteKey(context, favKey)
                    isFavoriteState.value = !wasFavorite
                    Toast.makeText(
                        context,
                        if (!wasFavorite) "Added to favorites" else "Removed from favorites",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                enabled = isOnline
            ) {
                Icon(
                    imageVector = if (isFavoriteState.value) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavoriteState.value) Color.Red else MaterialTheme.colorScheme.outline
                )
            }

            // 🛒 Add to cart - disabled when offline
            IconButton(
                onClick = {
                    CartManager.addToCart(perfume)
                    Toast.makeText(context, "Added to cart", Toast.LENGTH_SHORT).show()
                },
                enabled = isOnline
            ) {
                Icon(
                    imageVector = Icons.Filled.ShoppingCart,
                    contentDescription = "Add to cart",
                    tint = Color(0xFF1565C0)
                )
            }
        }
    }
}
