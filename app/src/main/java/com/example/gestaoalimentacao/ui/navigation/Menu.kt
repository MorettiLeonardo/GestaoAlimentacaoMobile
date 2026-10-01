package com.example.gestaoalimentacao.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SoupKitchen
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

data class MenuItem(val route: Any, val title: String, val icon: ImageVector)

val menuItems = listOf(
    MenuItem(Inicio, "Início", Icons.Default.Home),
    MenuItem(Receitas, "Receitas", Icons.Default.SoupKitchen),
    MenuItem(Perfil, "Perfil", Icons.Default.Person)
)

@Composable
fun Menu(navController: NavHostController) {
    val pilhaRotas by navController.currentBackStackEntryAsState()
    val rotaAtual = pilhaRotas?.destination

    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        menuItems.forEach { item ->
            val isSelected = rotaAtual?.hasRoute(item.route::class) == true
            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(Inicio) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        tint = if (isSelected) Color(0xFF0D52CE) else Color(0xFF94A3B8)
                    )
                },
                label = {
                    Text(
                        text = item.title,
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
