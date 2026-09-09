package com.example.gestaoalimentacao.data.model

import java.util.UUID

data class Meal(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val category: String = "Proteina",
    val portionValue: Double = 150.0,
    val portionUnit: String = "g",
    val calories: Int = 0,
    val carbs: Double = 0.0,
    val protein: Double = 0.0,
    val fat: Double = 0.0,
    val dateText: String = "Hoje",
    val isConsumed: Boolean = false,
    val notes: String = "",
    val reminderEnabled: Boolean = true
)
