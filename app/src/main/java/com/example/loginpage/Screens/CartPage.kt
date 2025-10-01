package com.example.loginpage.Screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.ViewCompat
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.loginpage.data.CartManager
import com.example.loginpage.data.LocalPerfume
import com.example.loginpage.model.Perfume
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun CartScreen(navController: NavController) {
    // Holds the list of cart items (resource-based) and remote (string-based) items
    val cartItems = remember { mutableStateListOf<Perfume>() }
    val remoteCartItems = remember { mutableStateListOf<LocalPerfume>() }

    // List of items currently being animated (for delete) — use IDs
    val animatingPerfumeIds = remember { mutableStateListOf<Int>() }
    val animatingRemoteIds = remember { mutableStateListOf<Int>() }

    // Coroutine scope for animations and delayed actions
    val scope = rememberCoroutineScope()

    // Holds quantity for each item by ID (applies to both lists)
    val quantities = remember { mutableStateMapOf<Int, Int>() }

    // Load cart items and initialize their quantities on first composition
    LaunchedEffect(Unit) {
        cartItems.clear()
        remoteCartItems.clear()
        cartItems.addAll(CartManager.getCartItems())
        remoteCartItems.addAll(CartManager.getRemoteCartItems())
        cartItems.forEach { quantities.putIfAbsent(it.id, 1) }
        remoteCartItems.forEach { quantities.putIfAbsent(it.id, 1) }
    }

    // Calculate total cart price (Double) across both lists
    val total = cartItems.sumOf { it.price * (quantities[it.id] ?: 1) } +
        remoteCartItems.sumOf { it.price * (quantities[it.id] ?: 1) }
    val totalFormatted = remember(total) { String.format(Locale.US, "%.2f", total) }

    // Get top system padding (e.g., status bar height)
    val view = LocalView.current
    val topPadding = with(LocalDensity.current) {
        ViewCompat.getRootWindowInsets(view)?.systemGestureInsets?.top?.toDp() ?: 24.dp
    }

    // Main container
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = topPadding + 12.dp, start = 16.dp, end = 16.dp)
        ) {
            // Top bar with back button and cart title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Your Cart",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = "Cart Icon",
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            val isEmpty = cartItems.isEmpty() && remoteCartItems.isEmpty()

            if (isEmpty) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Your cart is empty",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                // List of mixed cart items: render resource items first, then remote items
                LazyColumn(modifier = Modifier.weight(1f)) {
                    // Resource-based Perfume items
                    itemsIndexed(cartItems) { index, item ->
                        val isAnimating = animatingPerfumeIds.contains(item.id)
                        val rotation by animateFloatAsState(
                            targetValue = if (isAnimating) 360f else 0f,
                            label = "binRotation"
                        )

                        CartItemRow(
                            image = { painterResource(id = item.imageResId) },
                            name = stringResource(id = item.nameResId),
                            priceText = "Price: Rs. ${item.price}",
                            quantity = quantities[item.id] ?: 1,
                            onDecrease = {
                                val currentQty = quantities[item.id] ?: 1
                                if (currentQty > 1) quantities[item.id] = currentQty - 1
                            },
                            onIncrease = {
                                val currentQty = quantities[item.id] ?: 1
                                quantities[item.id] = currentQty + 1
                            },
                            onDelete = {
                                if (!isAnimating) {
                                    animatingPerfumeIds.add(item.id)
                                    scope.launch {
                                        delay(300)
                                        CartManager.removeFromCart(item.id)
                                        cartItems.removeAt(index)
                                        quantities.remove(item.id)
                                        animatingPerfumeIds.remove(item.id)
                                    }
                                }
                            },
                            rotation = rotation
                        )
                    }

                    // Remote LocalPerfume items (from Order page)
                    itemsIndexed(remoteCartItems) { index, item ->
                        val isAnimating = animatingRemoteIds.contains(item.id)
                        val rotation by animateFloatAsState(
                            targetValue = if (isAnimating) 360f else 0f,
                            label = "binRotationRemote"
                        )

                        CartItemRow(
                            image = {
                                if (item.imageResId != 0) painterResource(id = item.imageResId)
                                else null
                            },
                            asyncImageUrl = if (item.imageResId == 0) item.imageUrl else null,
                            name = item.name,
                            priceText = "Price: Rs. ${item.price}",
                            quantity = quantities[item.id] ?: 1,
                            onDecrease = {
                                val currentQty = quantities[item.id] ?: 1
                                if (currentQty > 1) quantities[item.id] = currentQty - 1
                            },
                            onIncrease = {
                                val currentQty = quantities[item.id] ?: 1
                                quantities[item.id] = currentQty + 1
                            },
                            onDelete = {
                                if (!isAnimating) {
                                    animatingRemoteIds.add(item.id)
                                    scope.launch {
                                        delay(300)
                                        CartManager.removeRemoteFromCart(item.id)
                                        remoteCartItems.removeAt(index)
                                        quantities.remove(item.id)
                                        animatingRemoteIds.remove(item.id)
                                    }
                                }
                            },
                            rotation = rotation
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Display total cost
                Text(
                    text = "Total: Rs. $totalFormatted",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(bottom = 10.dp)
                )

                // "Pay Now" button
                Button(
                    onClick = {
                        navController.navigate("payment/$totalFormatted")
                    },
                    enabled = !isEmpty,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Pay Now",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onTertiary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Bottom navigation bar
            BottomNavigationBar(
                navController = navController,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .fillMaxWidth()
            )
        }
    }
}

@Composable
private fun CartItemRow(
    image: (@Composable () -> Any?)? = null, // painterResource provider or null
    asyncImageUrl: String? = null,
    name: String,
    priceText: String,
    quantity: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    onDelete: () -> Unit,
    rotation: Float
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            // Product image
            if (asyncImageUrl != null) {
                AsyncImage(
                    model = asyncImageUrl,
                    contentDescription = name,
                    modifier = Modifier
                        .size(80.dp)
                        .padding(end = 12.dp)
                )
            } else if (image != null) {
                val pr = image()
                if (pr is androidx.compose.ui.graphics.painter.Painter) {
                    Image(
                        painter = pr,
                        contentDescription = name,
                        modifier = Modifier
                            .size(80.dp)
                            .padding(end = 12.dp)
                    )
                }
            }

            // Item details and quantity controls
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = priceText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.tertiary
                )

                // Quantity increase/decrease buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 6.dp)
                ) {
                    IconButton(onClick = onDecrease) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease Quantity")
                    }

                    Text(
                        text = "$quantity",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    IconButton(onClick = onIncrease) {
                        Icon(Icons.Default.Add, contentDescription = "Increase Quantity")
                    }
                }
            }

            // Delete button with animation
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove from Cart",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .size(28.dp)
                        .graphicsLayer {
                            rotationZ = rotation // Rotates icon during animation
                        }
                )
            }
        }
    }
}
