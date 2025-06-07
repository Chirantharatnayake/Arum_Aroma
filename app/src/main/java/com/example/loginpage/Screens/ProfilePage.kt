package com.example.loginpage.Screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.ViewCompat
import androidx.navigation.NavController
import com.example.loginpage.R

@Composable
fun ProfileScreen(navController: NavController) {
    // Get top padding considering the system status bar
    val view = LocalView.current
    val topPadding = with(LocalDensity.current) {
        (ViewCompat.getRootWindowInsets(view)?.systemGestureInsets?.top?.toDp() ?: 24.dp) + 36.dp
    }

    val colorScheme = MaterialTheme.colorScheme

    // Main container
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.surface)
    ) {
        // Content column
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()) // Enable scroll if needed
                .padding(top = topPadding, start = 20.dp, end = 20.dp, bottom = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Picture with radial gradient background ring
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
                Image(
                    painter = painterResource(id = R.drawable.profileimage),
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // User Name
            Text(
                text = "Akindu",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface
            )

            // User Email
            Text(
                text = "Akindu@gmail.com",
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.outline
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Edit Profile Button
            OutlinedButton(
                onClick = { /* Navigate to edit screen */ },
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = colorScheme.tertiaryContainer,
                    contentColor = colorScheme.onTertiaryContainer
                )
            ) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit")
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Edit Profile",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // User Stats Card (Orders, Favorites, Reviews)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    ProfileStat("Orders", "12", colorScheme)
                    ProfileStat("Favorites", "8", colorScheme)
                    ProfileStat("Reviews", "5", colorScheme)
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Logout Button
            Button(
                onClick = {
                    // Navigate to login and clear profile from back stack
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
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Logout",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        // Bottom Navigation
        BottomNavigationBar(
            navController = navController,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

// Component for showing individual stats (Orders, Favorites, Reviews)
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
