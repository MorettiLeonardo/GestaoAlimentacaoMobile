package com.example.gestaoalimentacao.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
data object Login

@Serializable
data class Register(val email: String = "")

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

@Serializable
data class AddEditRecipe(val recipeId: String? = null)

@Serializable
data object Agua

@Serializable
data class AddEditWater(val waterId: String? = null)

@Serializable
data object Peso

@Serializable
data class AddEditWeight(val weightId: String? = null)

@Serializable
data object Sugestoes
