package com.example.loginpage.Screens

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.loginpage.API.AmbientLightState
import com.example.loginpage.API.rememberProfileImageController
import com.example.loginpage.R
import com.example.loginpage.data.LocalStorage
import com.example.loginpage.ui.theme.ThemePreferenceState
import com.example.loginpage.data.FirebaseManager
import com.example.loginpage.API.BatteryApi
import com.example.loginpage.data.CartManager
import com.example.loginpage.data.FavoriteManager
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

@Composable
fun ProfileScreen(navController: NavController) {
    val colorScheme = MaterialTheme.colorScheme
    val controller = rememberProfileImageController()
    val context = LocalContext.current
    val isDark by ThemePreferenceState.isDarkState

    // Battery alerts persisted state
    var batteryAlertsEnabled by remember { mutableStateOf(false) }

    // Our own image state (what we render)
    var profileImage: ImageBitmap? by remember { mutableStateOf(null) }

    // Username/email loaded from local storage (fallback to Firestore once if missing)
    var displayName by remember { mutableStateOf<String?>(null) }
    var displayEmail by remember { mutableStateOf<String?>(null) }

    // Saved card state
    var saveCardEnabled by remember { mutableStateOf(false) }
    var savedCard by remember { mutableStateOf<LocalStorage.SavedCard?>(null) }

    // Load saved toggles once
    LaunchedEffect(Unit) {
        AmbientLightState.isEnabled = LocalStorage.loadAmbientEnabled(context)
        batteryAlertsEnabled = LocalStorage.loadBatteryAlertEnabled(context)
        saveCardEnabled = LocalStorage.loadSaveCardEnabled(context)
        savedCard = LocalStorage.loadCardDetails(context)
        if (batteryAlertsEnabled) {
            BatteryApi.start(context)
        }
    }

    // Restore saved profile image once
    LaunchedEffect(Unit) {
        LocalStorage.loadProfileBitmap(context)?.let { bmp ->
            profileImage = bmp.asImageBitmap()
        }
    }

    // Load saved profile info, then optionally try Firestore if missing
    LaunchedEffect(Unit) {
        displayName = LocalStorage.loadUserName(context)
        displayEmail = LocalStorage.loadUserEmail(context)
        if (displayName.isNullOrBlank() || displayEmail.isNullOrBlank()) {
            FirebaseManager.loadCurrentUserProfile { name, email ->
                name?.let { LocalStorage.saveUserName(context, it) }
                email?.let { LocalStorage.saveUserEmail(context, it) }
                displayName = name ?: displayName
                displayEmail = email ?: displayEmail
            }
        }
    }

    // When controller picks a new image, mirror it into our state and persist
    LaunchedEffect(controller.image) {
        controller.image?.let { picked ->
            profileImage = picked
            LocalStorage.saveProfileBitmap(context, picked.asAndroidBitmap())
        }
    }

    // Dialog visibility for editing profile
    var showEditDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(WindowInsets.statusBars.asPaddingValues())
                .padding(horizontal = 20.dp, vertical = 20.dp)
                .padding(bottom = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile picture with gradient ring
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .shadow(6.dp, CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(colorScheme.primary, colorScheme.secondary),
                            radius = 220f,
                            tileMode = TileMode.Clamp
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                val toShow = profileImage ?: controller.image
                if (toShow != null) {
                    Image(
                        bitmap = toShow,
                        contentDescription = "Profile Picture",
                        modifier = Modifier.size(110.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.profileimage),
                        contentDescription = "Profile Picture",
                        modifier = Modifier.size(110.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AssistChip(onClick = controller.captureFromCamera, label = { Text("Camera") })
                AssistChip(onClick = controller.pickFromGallery, label = { Text("Gallery") })
                // New: Remove profile picture option
                AssistChip(
                    onClick = {
                        LocalStorage.clearProfileBitmap(context)
                        controller.clear()
                        profileImage = null
                        Toast.makeText(context, "Profile photo removed", Toast.LENGTH_SHORT).show()
                    },
                    label = { Text("Remove") }
                )
            }

            // Moved: Username, email, and stats right after the photo and chips
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = displayName?.takeIf { it.isNotBlank() } ?: "Guest",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface
            )
            Text(
                text = displayEmail?.takeIf { !it.isNullOrBlank() } ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.outline
            )

            // Inserted: Edit Profile button directly below email
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = { showEditDialog = true },
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = colorScheme.tertiaryContainer,
                    contentColor = colorScheme.onTertiaryContainer
                )
            ) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit")
                Spacer(modifier = Modifier.size(8.dp))
                Text(text = "Edit Profile", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
            }

            // Edit Profile dialog (username only)
            if (showEditDialog) {
                var newName by remember(displayName, showEditDialog) { mutableStateOf(displayName.orEmpty()) }
                var saving by remember { mutableStateOf(false) }
                val canSave = !saving && newName.isNotBlank()

                AlertDialog(
                    onDismissRequest = { if (!saving) showEditDialog = false },
                    confirmButton = {
                        TextButton(onClick = {
                            if (!canSave) return@TextButton
                            saving = true
                            val nameArg = newName.takeIf { it.isNotBlank() }
                            FirebaseManager.updateUserProfile(
                                newUsername = nameArg,
                                newEmail = null,
                                newPassword = null,
                                currentPassword = null,
                                onSuccess = {
                                    saving = false
                                    nameArg?.let { LocalStorage.saveUserName(context, it); displayName = it }
                                    Toast.makeText(context, "Username updated", Toast.LENGTH_SHORT).show()
                                    showEditDialog = false
                                },
                                onError = { msg ->
                                    saving = false
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                }
                            )
                        }, enabled = canSave) {
                            Text(if (saving) "Saving…" else "Save")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { if (!saving) showEditDialog = false }) { Text("Cancel") }
                    },
                    title = { Text("Edit Username", style = MaterialTheme.typography.titleLarge) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = newName,
                                onValueChange = { newName = it },
                                label = { Text("Username") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                text = "Only your username can be changed here.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    },
                    shape = RoundedCornerShape(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Ambient Light Monitor — title + toggle
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ambient Light Monitor",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurfaceVariant
                    )
                    Switch(
                        checked = AmbientLightState.isEnabled,
                        onCheckedChange = { checked ->
                            AmbientLightState.isEnabled = checked
                            LocalStorage.saveAmbientEnabled(context, checked)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = colorScheme.onPrimary,
                            checkedTrackColor = colorScheme.primary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Battery Alerts — title + toggle
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Battery Alerts",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurfaceVariant
                    )
                    Switch(
                        checked = batteryAlertsEnabled,
                        onCheckedChange = { checked ->
                            batteryAlertsEnabled = checked
                            if (checked) BatteryApi.start(context) else BatteryApi.stop(context)
                            LocalStorage.saveBatteryAlertEnabled(context, checked)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = colorScheme.onPrimary,
                            checkedTrackColor = colorScheme.primary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Saved Card — global toggle + quick actions
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Save Card",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurfaceVariant
                        )
                        Switch(
                            checked = saveCardEnabled,
                            onCheckedChange = { checked ->
                                saveCardEnabled = checked
                                LocalStorage.saveSaveCardEnabled(context, checked)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = colorScheme.onPrimary,
                                checkedTrackColor = colorScheme.primary
                            )
                        )
                    }

                    Spacer(Modifier.height(8.dp))
                    if (savedCard != null) {
                        val sc = savedCard!!
                        val last4 = sc.number.filter { it.isDigit() }.takeLast(4)
                        Text(
                            text = "${sc.brand} •••• $last4  •  Expires ${sc.expiry}",
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(
                                onClick = {
                                    LocalStorage.clearSavedCard(context)
                                    savedCard = null
                                },
                                shape = RoundedCornerShape(10.dp)
                            ) { Text("Remove saved card") }
                        }
                    } else {
                        Text(
                            text = "No card saved. Enable and save at checkout.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dark Mode Toggle (your existing impl)
            DarkModeToggleCard(isDark = isDark, onToggle = { ThemePreferenceState.toggle(context) })

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    // Optional: clear local profile on logout
                    LocalStorage.clearUserProfile(context)
                    // Reload managers so data switches to guest scope immediately
                    CartManager.reloadForActiveUser()
                    FavoriteManager.reloadForActiveUser()
                    // Sign out from Firebase so next launch goes to login
                    FirebaseManager.signOut()
                    navController.navigate("login") {
                        popUpTo("profile") { inclusive = true }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorScheme.error,
                    contentColor = colorScheme.onError
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Icon(Icons.Default.Logout, contentDescription = "Logout")
                Spacer(modifier = Modifier.size(10.dp))
                Text(text = "Logout", style = MaterialTheme.typography.labelLarge)
            }
        }

        BottomNavigationBar(
            navController = navController,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun DarkModeToggleCard(isDark: Boolean, onToggle: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val trackGradient = if (isDark) listOf(scheme.primary, scheme.secondary) else listOf(scheme.tertiary, scheme.primary)
    val thumbOffset by animateFloatAsState(
        targetValue = if (isDark) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.6f),
        label = "thumb"
    )
    val containerColor by animateColorAsState(
        if (isDark) scheme.surfaceVariant.copy(alpha = 0.6f) else scheme.surfaceVariant,
        label = "container"
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(18.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isDark) "Dark Mode" else "Light Mode",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurface
                )
                Text(
                    text = if (isDark) "Relaxed low-light experience" else "Bright vibrant experience",
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.size(12.dp))
            Box(
                modifier = Modifier
                    .width(70.dp)
                    .height(38.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Brush.linearGradient(trackGradient))
            ) {
                val thumbSize = 30.dp
                val horizontalTravel = 70 - 38
                Box(
                    modifier = Modifier
                        .size(thumbSize)
                        .offset(x = (horizontalTravel * thumbOffset).dp, y = 4.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.92f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isDark) Icons.Default.DarkMode else Icons.Default.LightMode,
                        contentDescription = "theme icon",
                        tint = if (isDark) scheme.primary else scheme.tertiary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileStat(label: String, value: String, colorScheme: ColorScheme) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = colorScheme.outline
        )
    }
}
