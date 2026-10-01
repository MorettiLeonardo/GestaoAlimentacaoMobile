package com.example.gestaoalimentacao

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.gestaoalimentacao.ui.navigation.AppNavigation
import com.example.gestaoalimentacao.ui.navigation.Menu
import com.example.gestaoalimentacao.ui.navigation.menuItems
import com.example.gestaoalimentacao.ui.theme.GestaoAlimentacaoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GestaoAlimentacaoTheme {
                val navController = rememberNavController()

                val pilhaRotas by navController.currentBackStackEntryAsState()
                val rotaAtual = pilhaRotas?.destination

                val mostrarMenu = menuItems.any { rotaAtual?.hasRoute(it.route::class) == true }

                Scaffold(
                    bottomBar = {
                        if (mostrarMenu) {
                            Menu(navController = navController)
                        }
                    }
                ) { innerPadding ->
                    AppNavigation(
                        navController = navController,
                        modifier = if (mostrarMenu) Modifier.padding(innerPadding) else Modifier,
                        authViewModel = viewModel(),
                        mealViewModel = viewModel(),
                        recipeViewModel = viewModel()
                    )
                }
            }
        }
    }
}
