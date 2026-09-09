package com.example.gestaoalimentacao.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SoupKitchen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.gestaoalimentacao.ui.screens.*
import com.example.gestaoalimentacao.ui.viewmodel.AuthViewModel
import com.example.gestaoalimentacao.ui.viewmodel.MealViewModel
import com.example.gestaoalimentacao.ui.viewmodel.RecipeViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Main : Screen("main")
    object AddEditMeal : Screen("add_edit_meal?mealId={mealId}") {
        fun createRoute(mealId: String? = null) = if (mealId != null) "add_edit_meal?mealId=$mealId" else "add_edit_meal"
    }
    object NutritionalInfo : Screen("nutritional_info/{mealId}") {
        fun createRoute(mealId: String) = "nutritional_info/$mealId"
    }
}

sealed class BottomTab(val route: String, val title: String, val icon: ImageVector) {
    object Inicio : BottomTab("tab_inicio", "Início", Icons.Default.Home)
    object Receitas : BottomTab("tab_receitas", "Receitas", Icons.Default.SoupKitchen)
    object Perfil : BottomTab("tab_perfil", "Perfil", Icons.Default.Person)
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = viewModel(),
    mealViewModel: MealViewModel = viewModel(),
    recipeViewModel: RecipeViewModel = viewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val startDestination = if (currentUser != null) Screen.Main.route else Screen.Login.route

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                authViewModel = authViewModel,
                onRegisterSuccess = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Main.route) {
            MainContainerScreen(
                mealViewModel = mealViewModel,
                recipeViewModel = recipeViewModel,
                authViewModel = authViewModel,
                onNavigateToAddMeal = { mealId ->
                    navController.navigate(Screen.AddEditMeal.createRoute(mealId))
                },
                onNavigateToNutritionalInfo = { mealId ->
                    navController.navigate(Screen.NutritionalInfo.createRoute(mealId))
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.AddEditMeal.route,
            arguments = listOf(navArgument("mealId") {
                type = NavType.StringType
                nullable = true
            })
        ) { backStackEntry ->
            val mealId = backStackEntry.arguments?.getString("mealId")
            AddEditMealScreen(
                mealViewModel = mealViewModel,
                mealId = mealId,
                onSaveSuccess = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.NutritionalInfo.route,
            arguments = listOf(navArgument("mealId") { type = NavType.StringType })
        ) { backStackEntry ->
            val mealId = backStackEntry.arguments?.getString("mealId") ?: ""
            NutritionalInfoScreen(
                mealViewModel = mealViewModel,
                mealId = mealId,
                onBackClick = { navController.popBackStack() },
                onEditClick = { id ->
                    navController.navigate(Screen.AddEditMeal.createRoute(id))
                }
            )
        }
    }
}

@Composable
fun MainContainerScreen(
    mealViewModel: MealViewModel,
    recipeViewModel: RecipeViewModel,
    authViewModel: AuthViewModel,
    onNavigateToAddMeal: (String?) -> Unit,
    onNavigateToNutritionalInfo: (String) -> Unit,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableStateOf<BottomTab>(BottomTab.Inicio) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                val tabs = listOf(BottomTab.Inicio, BottomTab.Receitas, BottomTab.Perfil)
                tabs.forEach { tab ->
                    val isSelected = selectedTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                tint = if (isSelected) Color(0xFF0D52CE) else Color(0xFF94A3B8)
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color(0xFF0D52CE) else Color(0xFF94A3B8)
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color(0xFFE0E7FF)
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                BottomTab.Inicio -> {
                    DailyLogScreen(
                        mealViewModel = mealViewModel,
                        onMealClick = { mealId ->
                            onNavigateToNutritionalInfo(mealId)
                        },
                        onAddMealClick = {
                            onNavigateToAddMeal(null)
                        }
                    )
                }
                BottomTab.Receitas -> {
                    RecipeScreen(
                        recipeViewModel = recipeViewModel
                    )
                }
                BottomTab.Perfil -> {
                    ProfileScreen(
                        authViewModel = authViewModel,
                        onLogout = onLogout
                    )
                }
            }
        }
    }
}
