package com.example.gestaoalimentacao.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestaoalimentacao.data.model.Meal
import com.example.gestaoalimentacao.data.repository.MealRepository
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

    val meals: StateFlow<List<Meal>> = MealRepository.meals

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
        return MealRepository.getMealById(id)
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
        MealRepository.addMeal(newMeal)
    }

    fun updateMeal(meal: Meal) {
        MealRepository.updateMeal(meal)
    }

    fun deleteMeal(id: String) {
        MealRepository.deleteMeal(id)
    }

    fun toggleMealConsumed(id: String) {
        MealRepository.toggleMealConsumed(id)
    }
}

// Extension to avoid compilation error with WhileSubsubscribed typo if any
private fun SharingStarted.Companion.WhileSubsubscribed(stopTimeoutMillis: Long): SharingStarted {
    return SharingStarted.WhileSubscribed(stopTimeoutMillis)
}
