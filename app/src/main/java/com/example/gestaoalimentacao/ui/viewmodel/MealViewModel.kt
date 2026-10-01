package com.example.gestaoalimentacao.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestaoalimentacao.data.model.Meal
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class MealViewModel : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val targetCalories: Int = 2000

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

    private val _meals = MutableStateFlow(initialMeals)
    val meals: StateFlow<List<Meal>> = _meals.asStateFlow()

    val filteredMeals: StateFlow<List<Meal>> = combine(meals, searchQuery) { mealList, query ->
        if (query.isBlank()) {
            mealList
        } else {
            mealList.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.description.contains(query, ignoreCase = true) ||
                        it.category.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubsubscribed(5000),
        initialValue = emptyList()
    )

    val consumedCalories: StateFlow<Int> = meals.combine(MutableStateFlow(Unit)) { mealList, _ ->
        mealList.filter { it.isConsumed }.sumOf { it.calories }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubsubscribed(5000),
        initialValue = 0
    )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun getMealById(id: String): Meal? {
        return _meals.value.find { it.id == id }
    }

    fun addMeal(
        title: String,
        category: String,
        portionValue: Double,
        portionUnit: String,
        calories: Int,
        carbs: Double,
        protein: Double,
        fat: Double,
        reminderEnabled: Boolean,
        notes: String = ""
    ) {
        val newMeal = Meal(
            title = title.ifBlank { "Nova Refeição" },
            category = category,
            portionValue = portionValue,
            portionUnit = portionUnit,
            calories = calories,
            carbs = carbs,
            protein = protein,
            fat = fat,
            notes = notes,
            reminderEnabled = reminderEnabled,
            isConsumed = false,
            dateText = "Hoje"
        )
        _meals.value = listOf(newMeal) + _meals.value
    }

    fun updateMeal(meal: Meal) {
        _meals.value = _meals.value.map { if (it.id == meal.id) meal else it }
    }

    fun deleteMeal(id: String) {
        _meals.value = _meals.value.filter { it.id != id }
    }

    fun toggleMealConsumed(id: String) {
        _meals.value = _meals.value.map { if (it.id == id) it.copy(isConsumed = !it.isConsumed) else it }
    }
}

private fun SharingStarted.Companion.WhileSubsubscribed(stopTimeoutMillis: Long): SharingStarted {
    return SharingStarted.WhileSubscribed(stopTimeoutMillis)
}
