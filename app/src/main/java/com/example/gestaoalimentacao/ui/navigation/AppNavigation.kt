package com.example.gestaoalimentacao.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.gestaoalimentacao.ui.screens.*
import com.example.gestaoalimentacao.ui.viewmodel.AuthViewModel
import com.example.gestaoalimentacao.ui.viewmodel.MealViewModel
import com.example.gestaoalimentacao.ui.viewmodel.RecipeViewModel

@Composable
fun AppNavigation(
    navController: NavHostController,
    modifier: Modifier,
    authViewModel: AuthViewModel,
    mealViewModel: MealViewModel,
    recipeViewModel: RecipeViewModel
) {
    NavHost(
        navController = navController,
        startDestination = if (authViewModel.currentUser != null) Inicio else Login,
        modifier = modifier
    ) {
        composable<Login> {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Inicio) {
                        popUpTo(Login) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(Register) }
            )
        }

        composable<Register> {
            RegisterScreen(
                authViewModel = authViewModel,
                onRegisterSuccess = {
                    navController.navigate(Inicio) {
                        popUpTo(Login) { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        composable<Inicio> {
            DailyLogScreen(
                mealViewModel = mealViewModel,
                onMealClick = { mealId -> navController.navigate(NutritionalInfo(mealId)) },
                onAddMealClick = { navController.navigate(AddEditMeal()) }
            )
        }

        composable<Receitas> {
            RecipeScreen(recipeViewModel = recipeViewModel)
        }

        composable<Perfil> {
            ProfileScreen(
                authViewModel = authViewModel,
                onLogout = {
                    navController.navigate(Login) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable<AddEditMeal> { back ->
            val rota = back.toRoute<AddEditMeal>()
            AddEditMealScreen(
                mealViewModel = mealViewModel,
                mealId = rota.mealId,
                onSaveSuccess = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }

        composable<NutritionalInfo> { back ->
            val rota = back.toRoute<NutritionalInfo>()
            NutritionalInfoScreen(
                mealViewModel = mealViewModel,
                mealId = rota.mealId,
                onBackClick = { navController.popBackStack() },
                onEditClick = { id -> navController.navigate(AddEditMeal(id)) }
            )
        }
    }
}
