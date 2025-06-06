package com.example.loginpage.Screens

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
import com.example.loginpage.data.CartManager
import com.example.loginpage.model.Perfume

@Composable
fun CartScreen(navController: NavController) {
    val cartItems = remember { mutableStateListOf<Perfume>() }
    val quantities = remember { mutableStateMapOf<Int, Int>() }

    LaunchedEffect(Unit) {
        cartItems.clear()
        cartItems.addAll(CartManager.getCartItems())
        cartItems.forEach { quantities[it.id] = 1 }
    }

    val total = cartItems.sumOf { it.price * (quantities[it.id] ?: 1) }

    val view = LocalView.current
    val topPadding = with(LocalDensity.current) {
        ViewCompat.getRootWindowInsets(view)?.systemGestureInsets?.top?.toDp() ?: 24.dp
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.surface)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = topPadding + 12.dp, start = 16.dp, end = 16.dp)
        ) {
            // Top Bar
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
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
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

            if (cartItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Your cart is empty", fontSize = 18.sp, color = MaterialTheme.colorScheme.outline)
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    itemsIndexed(cartItems) { index, item ->
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
                                Image(
                                    painter = painterResource(id = item.imageResId),
                                    contentDescription = stringResource(id = item.nameResId),
                                    modifier = Modifier
                                        .size(80.dp)
                                        .padding(end = 12.dp)
                                )

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = stringResource(id = item.nameResId),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Price: Rs. ${item.price}",
                                        color = MaterialTheme.colorScheme.tertiary,
                                        fontSize = 16.sp
                                    )

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
                                            fontSize = 18.sp,
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

                                IconButton(onClick = {
                                    CartManager.removeFromCart(item.id)
                                    cartItems.removeAt(index)
                                    quantities.remove(item.id)
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Remove from Cart",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Total: Rs. $total",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(bottom = 10.dp)
                )

                Button(
                    onClick = {
                        // Payment logic
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Pay Now", color = MaterialTheme.colorScheme.onTertiary, fontSize = 18.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            BottomNavigationBar(
                navController = navController,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .fillMaxWidth()
            )
        }
    }
}
