package com.example.loginpage

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.loginpage.Screens.*
import com.example.loginpage.ui.theme.LoginPageTheme
import com.example.loginpage.util.ConnectivityToasts
import com.example.loginpage.data.LocalStorage
import com.example.loginpage.data.FavoriteManager
import com.example.loginpage.data.CartManager
import com.example.loginpage.ui.theme.ThemePreferenceState
import com.example.loginpage.data.FirebaseManager
import com.example.loginpage.API.GlobalBatteryAlert
import com.example.loginpage.API.BatteryApi
import com.example.loginpage.API.GlobalAmbientAlert

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize persistence-backed managers
        LocalStorage.init(applicationContext)
        FavoriteManager.init(applicationContext)
        CartManager.init(applicationContext)
        ThemePreferenceState.init(applicationContext)

        // Auto-start battery alerts if toggle was enabled previously
        if (LocalStorage.loadBatteryAlertEnabled(applicationContext)) {
            BatteryApi.start(applicationContext)
        }

        setContent {
            // Use only the persisted preference for theme so it survives restarts
            val dark by ThemePreferenceState.isDarkState
            LoginPageTheme(darkTheme = dark) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        MainApp()
                        Column(modifier = Modifier.align(Alignment.TopCenter)) {
                            GlobalBatteryAlert()
                            GlobalAmbientAlert()
                        }
                    }
                    ConnectivityToasts()
                }
            }
        }
    }
}

@Composable
fun MainApp() {
    val navController = rememberNavController()
    val startDestination = if (FirebaseManager.isLoggedIn()) "home" else "login"

    NavHost(navController = navController, startDestination = startDestination) {
        //Auth Screens
        composable("signup") {
            SignUpPage(onLoginClick = {
                navController.navigate("login")
            })
        }

        composable("login") {
            LoginPage(
                onSignUpClick = {
                    navController.navigate("signup")
                },
                onLoginSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        //Main App Screens
        composable("home") { HomePage(navController = navController) }
        composable("favorites") { FavouritesScreen(navController = navController) }
        composable("cart") { CartScreen(navController = navController) }
        composable("profile") { ProfileScreen(navController = navController) }
        composable("order") { OrderPage(navController = navController) }
        composable("payment/{amount}") { backStackEntry ->
            val amount = backStackEntry.arguments?.getString("amount")?.toDoubleOrNull() ?: 0.0
            PaymentScreen(navController = navController, amount = amount)
        }
        //Detail screen with argument
        composable("detail/{perfumeId}") { backStackEntry ->
            val perfumeId = backStackEntry.arguments?.getString("perfumeId")?.toIntOrNull()
            if (perfumeId != null) {
                PerfumeDetailScreen(navController = navController, perfumeId = perfumeId)
            }
        }
    }
}
