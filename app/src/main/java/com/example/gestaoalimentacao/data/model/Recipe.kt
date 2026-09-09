package com.example.gestaoalimentacao.data.model

import java.util.UUID

data class Recipe(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val prepTime: String,
    val calories: Int,
    val category: String,
    val ingredients: List<String>,
    val instructions: List<String>
)
