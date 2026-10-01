package com.example.gestaoalimentacao.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
data object Login

@Serializable
data object Register

@Serializable
data object Inicio

@Serializable
data object Receitas

@Serializable
data object Perfil

@Serializable
data class AddEditMeal(val mealId: String? = null)

@Serializable
data class NutritionalInfo(val mealId: String)
