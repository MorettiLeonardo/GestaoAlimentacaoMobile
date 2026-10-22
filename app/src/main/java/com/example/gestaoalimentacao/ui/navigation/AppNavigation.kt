package com.example.gestaoalimentacao.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.toRoute
import com.example.gestaoalimentacao.auth.SessaoUsuario
import com.example.gestaoalimentacao.ui.screens.*

@Composable
fun AppNavigation(
    navController: NavHostController,
    modifier: Modifier
) {
    val rotaAtual by navController.currentBackStackEntryAsState()
    val destino = rotaAtual?.destination
    val usuarioAtual = SessaoUsuario.usuarioId

    // Controle de sessão: sem usuário logado, qualquer tela que não seja Login/Cadastro volta para o Login.
    LaunchedEffect(destino, usuarioAtual) {
        val publica = destino?.hasRoute<Login>() == true ||
                destino?.hasRoute<Register>() == true

        if (destino != null && !publica && usuarioAtual == null) {
            navController.navigate(Login) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Login,
        modifier = modifier
    ) {
        composable<Login> {
            LoginScreen(
                authViewModel = viewModel(),
                onLoginSuccess = {
                    navController.navigate(Inicio) {
                        popUpTo(Login) { inclusive = true }
                    }
                },
                onNavigateToRegister = { email ->
                    navController.navigate(Register(email)) { launchSingleTop = true }
                }
            )
        }

        composable<Register> { back ->
            val rota = back.toRoute<Register>()
            RegisterScreen(
                authViewModel = viewModel(),
                initialEmail = rota.email,
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
                mealViewModel = viewModel(),
                onMealClick = { mealId -> navController.navigate(NutritionalInfo(mealId)) },
                onAddMealClick = { navController.navigate(AddEditMeal()) }
            )
        }

        composable<Receitas> {
            RecipeScreen(
                recipeViewModel = viewModel(),
                onAddRecipeClick = { navController.navigate(AddEditRecipe()) },
                onEditRecipeClick = { id -> navController.navigate(AddEditRecipe(id)) }
            )
        }

        composable<Perfil> {
            ProfileScreen(
                authViewModel = viewModel(),
                onLogout = {
                    navController.navigate(Login) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable<AddEditRecipe> { back ->
            val rota = back.toRoute<AddEditRecipe>()
            AddEditRecipeScreen(
                recipeViewModel = viewModel(),
                recipeId = rota.recipeId,
                voltar = { navController.popBackStack() }
            )
        }

        composable<AddEditMeal> { back ->
            val rota = back.toRoute<AddEditMeal>()
            AddEditMealScreen(
                mealViewModel = viewModel(),
                mealId = rota.mealId,
                onSaveSuccess = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }

        composable<NutritionalInfo> { back ->
            val rota = back.toRoute<NutritionalInfo>()
            NutritionalInfoScreen(
                mealViewModel = viewModel(),
                mealId = rota.mealId,
                onBackClick = { navController.popBackStack() },
                onEditClick = { id -> navController.navigate(AddEditMeal(id)) }
            )
        }
    }
}
