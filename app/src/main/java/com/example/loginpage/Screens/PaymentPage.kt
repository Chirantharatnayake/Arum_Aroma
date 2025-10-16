package com.example.loginpage.Screens

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.widget.Toast
import com.example.loginpage.data.CartManager
import com.example.loginpage.data.LocalStorage
import java.util.Locale

// Simplified Payment Screen: Card details + Save Card + Place Order
@Composable
fun PaymentScreen(navController: NavController, amount: Double) {
    val cs = MaterialTheme.colorScheme
    val focus = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var nameOnCard by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    var expiry by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    var saveCard by remember { mutableStateOf(true) }
    var processing by remember { mutableStateOf(false) }
    var success by remember { mutableStateOf(false) }

    // Initialize from saved settings/data
    LaunchedEffect(Unit) {
        val globalSave = LocalStorage.loadSaveCardEnabled(context)
        saveCard = globalSave
        val saved = LocalStorage.loadCardDetails(context)
        if (saved != null) {
            nameOnCard = saved.name
            // ensure spacing formatting preserved
            cardNumber = saved.number
            expiry = saved.expiry
        }
    }

    // Keep global toggle in sync if user flips it here
    LaunchedEffect(saveCard) {
        LocalStorage.saveSaveCardEnabled(context, saveCard)
    }

    // Derived cleaned digits
    val digitsOnly = cardNumber.filter { it.isDigit() }
    val brand = remember(digitsOnly) { detectBrandSimple(digitsOnly) }
    val formattedCard = remember(digitsOnly) { digitsOnly.chunked(4).joinToString(" ") }

    // Validation
    val expiryRegex = remember { "^(0[1-9]|1[0-2])/[0-9]{2}$".toRegex() }
    val nameError = nameOnCard.isNotBlank() && nameOnCard.trim().length < 3
    val numberError = cardNumber.isNotBlank() && digitsOnly.length != 16
    val expiryError = expiry.isNotBlank() && !expiryRegex.matches(expiry)
    val cvvError = cvv.isNotBlank() && (cvv.length !in 3..4 || !cvv.all { it.isDigit() })

    val cardValid = !nameError && !numberError && !expiryError && !cvvError &&
            digitsOnly.length == 16 && expiryRegex.matches(expiry) && cvv.length in 3..4 && nameOnCard.trim().length >= 3

    val brandGradient = remember(brand, cs) { gradientForBrand(brand, cs) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(cs.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(WindowInsets.statusBars.asPaddingValues())
                .padding(horizontal = 20.dp)
                .padding(bottom = 16.dp, top = 8.dp)
        ) {
            // Top bar (now padded by status bar inset)
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = cs.onSurface
                    )
                }
                Text(
                    text = "Payment",
                    style = MaterialTheme.typography.titleLarge,
                    color = cs.onSurface,
                    modifier = Modifier.padding(start = 4.dp)
                )
                Spacer(Modifier.weight(1f))
                Icon(Icons.Default.Lock, contentDescription = null, tint = cs.primary)
            }

            Spacer(Modifier.height(18.dp))
            OrderTotalCard(amount)

            Spacer(Modifier.height(26.dp))
            Text(
                "Card Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = cs.onSurface
            )
            Spacer(Modifier.height(14.dp))

            MinimalCardPreview(
                gradient = brandGradient,
                brand = brand,
                number = if (formattedCard.isBlank()) "•••• •••• •••• ••••" else formattedCard,
                name = nameOnCard.ifBlank { "CARDHOLDER" },
                expiry = expiry.ifBlank { "MM/YY" }
            )

            Spacer(Modifier.height(22.dp))

            // Name (letters and spaces only)
            OutlinedTextField(
                value = nameOnCard,
                onValueChange = { raw ->
                    val cleaned = raw.filter { ch -> ch.isLetter() || ch.isWhitespace() }
                    nameOnCard = cleaned.uppercase()
                },
                label = { Text("Name on card") },
                singleLine = true,
                isError = nameError,
                supportingText = {
                    if (nameError) Text("Min 3 chars", color = cs.error, fontSize = 11.sp)
                },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                )
            )
            Spacer(Modifier.height(14.dp))

            // Card number (auto formats)
            OutlinedTextField(
                value = cardNumber,
                onValueChange = { raw ->
                    // digits only, max 16 digits, auto-group 4s
                    val cleaned = raw.filter { it.isDigit() }.take(16)
                    cardNumber = cleaned.chunked(4).joinToString(" ")
                },
                label = { Text("Card number") },
                singleLine = true,
                trailingIcon = { Icon(Icons.Default.CreditCard, null, tint = cs.primary) },
                isError = numberError,
                supportingText = {
                    when {
                        numberError -> Text("Must be 16 digits", color = cs.error, fontSize = 11.sp)
                        brand.isNotBlank() -> Text(brand, fontSize = 11.sp)
                    }
                },
                textStyle = TextStyle(letterSpacing = 1.sp),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                )
            )
            Spacer(Modifier.height(14.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                OutlinedTextField(
                    value = expiry,
                    onValueChange = { input ->
                        // digits only up to 4, format MM/YY
                        val digits = input.filter { it.isDigit() }.take(4)
                        expiry = when {
                            digits.length <= 2 -> digits
                            else -> digits.substring(0, 2) + "/" + digits.substring(2)
                        }
                    },
                    label = { Text("Expiry (MM/YY)") },
                    singleLine = true,
                    isError = expiryError,
                    supportingText = {
                        if (expiryError) Text("Use MM/YY (01-12)", color = cs.error, fontSize = 11.sp)
                    },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    )
                )
                OutlinedTextField(
                    value = cvv,
                    onValueChange = { v ->
                        // numbers only, 3-4 digits
                        val digits = v.filter { it.isDigit() }.take(4)
                        cvv = digits
                    },
                    label = { Text("CVV") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    isError = cvvError,
                    supportingText = {
                        if (cvvError) Text("3-4 digits", color = cs.error, fontSize = 11.sp)
                    },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    )
                )
            }

            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(checked = saveCard, onCheckedChange = { saveCard = it })
                Text(
                    "Save card for future",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 6.dp)
                )
            }

            Spacer(Modifier.height(30.dp))
            Button(
                onClick = {
                    // New expiry year >= 2025 validation (YY >= 25)
                    if (expiryRegex.matches(expiry)) {
                        val yearPart = expiry.substringAfter('/') // YY
                        val yearNum = yearPart.toIntOrNull()
                        if (yearNum != null && yearNum < 25) {
                            Toast.makeText(context, "Card expired", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                    }
                    focus.clearFocus()
                    processing = true
                    scope.launch {
                        delay(1000)
                        processing = false
                        success = true
                        // Persist card details if user opted in and card is valid
                        if (saveCard && cardValid) {
                            val digitsOnly = cardNumber.filter { it.isDigit() }
                            val formatted = if (digitsOnly.isNotEmpty()) digitsOnly.chunked(4).joinToString(" ") else cardNumber
                            LocalStorage.saveCardDetails(
                                ctx = context,
                                name = nameOnCard.trim(),
                                numberFormatted = formatted,
                                expiry = expiry,
                                brand = detectBrandSimple(digitsOnly)
                            )
                        }
                        // Clear cart after order placement
                        CartManager.clearCart()
                        // Show success toast before navigating away
                        Toast.makeText(context, "Your order has been placed successfully", Toast.LENGTH_SHORT).show()
                        delay(600)
                        navController.navigate("order") { popUpTo("cart") { inclusive = false } }
                    }
                },
                enabled = cardValid && !processing,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = cs.primary,
                    contentColor = Color.White,                  // force white text (enabled)
                    disabledContainerColor = cs.primary.copy(alpha = 0.45f),
                    disabledContentColor = Color.White.copy(alpha = 0.9f) // force white text (disabled)
                )
            ) {
                Text(
                    when {
                        processing -> "Processing..."
                        success -> "Placed"
                        else -> "Place Order"
                    },
                    style = MaterialTheme.typography.labelLarge
                    // No explicit color: inherits Button's contentColor (white)
                )
            }

            Spacer(Modifier.height(18.dp))
            SecurityFooter()
        }

        if (processing) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f)),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = cs.primary) }
        }
    }
}

@Composable
private fun OrderTotalCard(amount: Double) {
    val cs = MaterialTheme.colorScheme
    val isDark = isSystemInDarkTheme()
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cs.surfaceVariant)
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(
                "Order Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = cs.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total", fontWeight = FontWeight.SemiBold, color = cs.onSurface)
                Text(
                    "Rs. ${String.format(Locale.US, "%.2f", amount)}",
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else cs.primary
                )
            }
        }
    }
}

@Composable
private fun MinimalCardPreview(
    gradient: List<Color>,
    brand: String,
    number: String,
    name: String,
    expiry: String
) {
    val overlay = Color.Black.copy(alpha = 0.18f)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(gradient))
                .padding(22.dp)
        ) {
            Box(Modifier.matchParentSize().background(overlay)) // subtle depth overlay
            Column(Modifier.fillMaxSize()) {
                Text(brand, color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
                Spacer(Modifier.height(20.dp))
                Text(
                    number,
                    color = Color.White,
                    fontSize = 20.sp,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.weight(1f))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("CARDHOLDER", color = Color.White.copy(alpha = 0.55f), fontSize = 10.sp)
                        Text(name, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("EXPIRES", color = Color.White.copy(alpha = 0.55f), fontSize = 10.sp)
                        Text(expiry, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

// Provide dynamic gradients per brand
private fun gradientForBrand(brand: String, cs: ColorScheme): List<Color> = when (brand) {
    "VISA" -> listOf(cs.primary, cs.secondary)
    "MASTERCARD" -> listOf(Color(0xFFf46b45), Color(0xFFeea849))
    "AMEX" -> listOf(Color(0xFF0f2027), Color(0xFF2c5364))
    "CARD" -> listOf(cs.tertiary, cs.primary)
    else -> listOf(cs.surfaceVariant, cs.surface)
}

private fun detectBrandSimple(digits: String): String = when {
    digits.startsWith("4") -> "VISA"
    digits.matches(Regex("5[1-5].*")) -> "MASTERCARD"
    digits.matches(Regex("3[47].*")) -> "AMEX"
    digits.length >= 4 -> "CARD"
    else -> ""
}

@Composable
private fun SecurityFooter() {
    val cs = MaterialTheme.colorScheme
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Lock, contentDescription = null, tint = cs.outline)
        Spacer(Modifier.width(6.dp))
        Text(
            "All transactions are secured & encrypted",
            style = MaterialTheme.typography.labelSmall,
            color = cs.outline
        )
    }
}
