package com.bono.mentalbot.ui.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bono.mentalbot.ui.auth.AuthScreen
import com.bono.mentalbot.ui.auth.AuthViewModel
import com.bono.mentalbot.ui.auth.NameScreen
import com.bono.mentalbot.ui.chat.ChatScreen
import com.bono.mentalbot.ui.history.HistoryScreen
import com.bono.mentalbot.ui.mood.MoodScreen
import com.bono.mentalbot.ui.technique.TechniqueScreen
import com.bono.mentalbot.ui.wellbeing.WellbeingHomeScreen
import com.bono.mentalbot.ui.wellbeing.WellbeingScreen

@Composable
fun NavGraph(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModel.factory(context)
    )
    val userName by authViewModel.userName.collectAsState()

    NavHost(
        navController = navController,
        startDestination = "auth"
    ) {
        composable("auth") {
            AuthScreen(
                onAuthSuccess = { isNewUser ->
                    if (isNewUser) {
                        navController.navigate("name") {
                            popUpTo("auth") { inclusive = true }
                        }
                    } else {
                        authViewModel.loadUserName()
                        navController.navigate("mood") {
                            popUpTo("auth") { inclusive = true }
                        }
                    }
                },
                viewModel = authViewModel
            )
        }

        composable("name") {
            NameScreen(
                onNameSaved = { name ->
                    authViewModel.saveUserName(name)
                    navController.navigate("mood") {
                        popUpTo("name") { inclusive = true }
                    }
                }
            )
        }

        composable("mood") {
            MoodScreen(
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme,
                userName = userName,
                onContinue = { mood ->
                    navController.navigate("wellbeinghome/$mood")
                }
            )
        }

        composable("wellbeinghome/{mood}") { backStackEntry ->
            val mood = backStackEntry.arguments?.getString("mood") ?: "neutral"
            WellbeingHomeScreen(
                userName = userName,
                mood = mood,
                onGoToChat = {
                    navController.navigate("chat/$mood/${Uri.encode("sin evaluación")}")
                },
                onGoToEvaluation = {
                    navController.navigate("wellbeing/$mood")
                },
                onGoToTechniques = {                              // ← agrega esto
                    navController.navigate("techniques/$mood")
                },
                onChangeMood = {
                    navController.navigate("mood") {
                        popUpTo("wellbeinghome/$mood") { inclusive = true }
                    }
                }
            )
        }

        composable("wellbeing/{mood}") { backStackEntry ->
            val mood = backStackEntry.arguments?.getString("mood") ?: "neutral"
            WellbeingScreen(
                userName = userName,
                mood = mood,
                onBack = { navController.popBackStack() },   // ← agrega esto
                onContinue = { wellbeingContext ->
                    navController.navigate("chat/$mood/${Uri.encode(wellbeingContext)}")
                }
            )
        }

        composable(
            route = "chat/{mood}/{wellbeingContext}",
            arguments = listOf(
                navArgument("mood") { type = NavType.StringType },
                navArgument("wellbeingContext") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val mood = backStackEntry.arguments?.getString("mood") ?: "neutral"
            val wellbeingContext = Uri.decode(
                backStackEntry.arguments?.getString("wellbeingContext") ?: ""
            )
            ChatScreen(
                mood = mood,
                userName = userName,
                wellbeingContext = wellbeingContext,
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme,
                onBack = { navController.popBackStack() },       // ← agrega esto
                onHistoryClick = { navController.navigate("history") },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate("auth") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable("history") {
            HistoryScreen(
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme,
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("techniques/{mood}") { backStackEntry ->
            val mood = backStackEntry.arguments?.getString("mood") ?: "neutral"
            TechniqueScreen(
                mood = mood,
                userName = userName,
                onBack = { navController.popBackStack() }
            )
        }
    }
}