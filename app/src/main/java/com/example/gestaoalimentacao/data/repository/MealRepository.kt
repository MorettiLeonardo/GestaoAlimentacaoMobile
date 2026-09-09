package com.example.gestaoalimentacao.data.repository

import com.example.gestaoalimentacao.data.model.Meal
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

object MealRepository {
    private val initialMeals = listOf(
        Meal(
            id = "1",
            title = "Café de manhã",
            description = "Omelete, Pão integral, ☕, Café",
            category = "Proteina",
            portionValue = 200.0,
            portionUnit = "g",
            calories = 350,
            carbs = 25.0,
            protein = 18.0,
            fat = 10.0,
            dateText = "Oct 28, 8 AM",
            isConsumed = true,
            notes = "Sem açúcar no café.",
            reminderEnabled = true
        ),
        Meal(
            id = "2",
            title = "Almoço",
            description = "Arroz, feijão, frango grelhado, salada",
            category = "Proteina",
            portionValue = 400.0,
            portionUnit = "g",
            calories = 750,
            carbs = 60.0,
            protein = 45.0,
            fat = 12.0,
            dateText = "Oct 28, 7 PM",
            isConsumed = true,
            notes = "Frango bem temperado com ervas.",
            reminderEnabled = true
        ),
        Meal(
            id = "3",
            title = "Jantar",
            description = "Sopa de legumes",
            category = "Vegetal",
            portionValue = 300.0,
            portionUnit = "g",
            calories = 350,
            carbs = 40.0,
            protein = 10.0,
            fat = 5.0,
            dateText = "Oct 29, 7 PM",
            isConsumed = false,
            notes = "Leve antes de dormir.",
            reminderEnabled = true
        ),
        Meal(
            id = "4",
            title = "Frango Grelhado",
            description = "Peito de frango grelhado",
            category = "Proteina",
            portionValue = 150.0,
            portionUnit = "g",
            calories = 240,
            carbs = 0.0,
            protein = 15.0,
            fat = 0.3,
            dateText = "Oct 28, 12 PM",
            isConsumed = true,
            notes = "Sem óleo, bem temperado.",
            reminderEnabled = true
        )
    )

    private val _meals = MutableStateFlow<List<Meal>>(initialMeals)
    val meals: StateFlow<List<Meal>> = _meals.asStateFlow()

    fun getMealById(id: String): Meal? {
        return _meals.value.find { it.id == id }
    }

    fun addMeal(meal: Meal) {
        val currentList = _meals.value.toMutableList()
        currentList.add(0, meal)
        _meals.value = currentList
    }

    fun updateMeal(updatedMeal: Meal) {
        val currentList = _meals.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == updatedMeal.id }
        if (index != -1) {
            currentList[index] = updatedMeal
            _meals.value = currentList
        }
    }

    fun deleteMeal(id: String) {
        val currentList = _meals.value.toMutableList()
        currentList.removeAll { it.id == id }
        _meals.value = currentList
    }

    fun toggleMealConsumed(id: String) {
        val currentList = _meals.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == id }
        if (index != -1) {
            val meal = currentList[index]
            currentList[index] = meal.copy(isConsumed = !meal.isConsumed)
            _meals.value = currentList
        }
    }
}
