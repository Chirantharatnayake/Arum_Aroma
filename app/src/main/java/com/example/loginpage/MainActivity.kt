package com.example.loginpage

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.loginpage.Screens.*
import com.example.loginpage.ui.theme.LoginPageTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LoginPageTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainApp()
                }
            }
        }
    }
}

@Composable
fun MainApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "signup") {
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
                        popUpTo("signup") { inclusive = true }
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

        //Detail screen with argument
        composable("detail/{perfumeId}") { backStackEntry ->
            val perfumeId = backStackEntry.arguments?.getString("perfumeId")?.toIntOrNull()
            if (perfumeId != null) {
                PerfumeDetailScreen(navController = navController, perfumeId = perfumeId)
            }
        }
    }
}
