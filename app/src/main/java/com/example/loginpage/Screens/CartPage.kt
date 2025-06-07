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
import com.example.loginpage.data.CartManager
import com.example.loginpage.model.Perfume
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

@Composable
fun CartScreen(navController: NavController) {
    // Holds the list of cart items
    val cartItems = remember { mutableStateListOf<Perfume>() }

    // List of items currently being animated (for delete)
    val animatingItemIds = remember { mutableStateListOf<Int>() }

    // Coroutine scope for animations and delayed actions
    val scope = rememberCoroutineScope()

    // Holds quantity for each item by ID
    val quantities = remember { mutableStateMapOf<Int, Int>() }

    // Load cart items and initialize their quantities on first composition
    LaunchedEffect(Unit) {
        cartItems.clear()
        cartItems.addAll(CartManager.getCartItems())
        cartItems.forEach { quantities[it.id] = 1 }
    }

    // Calculate total cart price
    val total = cartItems.sumOf { it.price * (quantities[it.id] ?: 1) }

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

            // Show empty message if cart is empty
            if (cartItems.isEmpty()) {
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
                // List of cart items
                LazyColumn(modifier = Modifier.weight(1f)) {
                    itemsIndexed(cartItems) { index, item ->
                        val isAnimating = animatingItemIds.contains(item.id)

                        // Rotation animation when deleting item
                        val rotation by animateFloatAsState(
                            targetValue = if (isAnimating) 360f else 0f,
                            label = "binRotation"
                        )

                        // Each cart item card
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
                                Image(
                                    painter = painterResource(id = item.imageResId),
                                    contentDescription = stringResource(id = item.nameResId),
                                    modifier = Modifier
                                        .size(80.dp)
                                        .padding(end = 12.dp)
                                )

                                // Item details and quantity controls
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = stringResource(id = item.nameResId),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    Text(
                                        text = "Price: Rs. ${item.price}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.tertiary
                                    )

                                    // Quantity increase/decrease buttons
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(top = 6.dp)
                                    ) {
                                        IconButton(onClick = {
                                            val currentQty = quantities[item.id] ?: 1
                                            if (currentQty > 1) quantities[item.id] = currentQty - 1
                                        }) {
                                            Icon(Icons.Default.Remove, contentDescription = "Decrease Quantity")
                                        }

                                        Text(
                                            text = "${quantities[item.id] ?: 1}",
                                            style = MaterialTheme.typography.bodyLarge,
                                            modifier = Modifier.padding(horizontal = 8.dp)
                                        )

                                        IconButton(onClick = {
                                            val currentQty = quantities[item.id] ?: 1
                                            quantities[item.id] = currentQty + 1
                                        }) {
                                            Icon(Icons.Default.Add, contentDescription = "Increase Quantity")
                                        }
                                    }
                                }

                                // Delete button with animation
                                IconButton(onClick = {
                                    if (!isAnimating) {
                                        animatingItemIds.add(item.id)

                                        scope.launch {
                                            delay(300) // Animation duration
                                            CartManager.removeFromCart(item.id)
                                            cartItems.removeAt(index)
                                            quantities.remove(item.id)
                                            animatingItemIds.remove(item.id)
                                        }
                                    }
                                }) {
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
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Display total cost
                Text(
                    text = "Total: Rs. $total",
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
                        // Payment logic goes here
                    },
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
