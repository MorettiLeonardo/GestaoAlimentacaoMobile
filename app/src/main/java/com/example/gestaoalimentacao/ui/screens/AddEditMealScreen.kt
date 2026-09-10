package com.example.gestaoalimentacao.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gestaoalimentacao.ui.viewmodel.MealViewModel

@Composable
fun AddEditMealScreen(
    mealViewModel: MealViewModel,
    mealId: String? = null,
    onSaveSuccess: () -> Unit,
    onCancel: () -> Unit
) {
    val existingMeal = remember(mealId) {
        if (mealId != null) mealViewModel.getMealById(mealId) else null
    }

    var title = existingMeal?.title ?: ""
    var selectedCategory = existingMeal?.category ?: ""
    var portionValueText = existingMeal?.portionValue?.toInt()?.toString() ?: ""
    var portionUnit = existingMeal?.portionUnit ?: ""
    var caloriesText = existingMeal?.calories?.toString() ?: ""
    var carbsText = existingMeal?.carbs?.toString() ?: ""
    var proteinText = existingMeal?.protein?.toString() ?: ""
    var fatText = existingMeal?.fat?.toString() ?: ""
    var notes = existingMeal?.notes ?: ""
    var reminderEnabled = existingMeal?.reminderEnabled ?: false


    var unitDropdownExpanded by remember { mutableStateOf(false) }
    val categories = listOf("Proteina", "Carboidrate", "Vegetal")

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 80.dp)
        ) {
            // Drag handle top line
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .background(Color(0xFFCBD5E1), RoundedCornerShape(2.dp))
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Screen Title
            Text(
                text = if (existingMeal == null) "Adicionar Alimento" else "Editar Alimento",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Nome de Alimento
            Text(
                text = "Nome de Alimento",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF334155)
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Categoria Chips
            Text(
                text = "Categoria",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF334155)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { category ->
                    val isSelected = category == selectedCategory
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = category },
                        label = {
                            Text(
                                text = category,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFDCFCE7),
                            selectedLabelColor = Color(0xFF166534),
                            containerColor = Color(0xFFF1F5F9),
                            labelColor = Color(0xFF475569)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Porção
            Text(
                text = "Porção",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF334155)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = portionValueText,
                    onValueChange = { portionValueText = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.Scale,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8)
                        )
                    },
                    modifier = Modifier.weight(1f)
                )

                // Unit Selector Dropdown
                Box {
                    OutlinedCard(
                        onClick = { unitDropdownExpanded = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(56.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(horizontal = 16.dp)
                        ) {
                            Text(text = portionUnit, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    }
                    DropdownMenu(
                        expanded = unitDropdownExpanded,
                        onDismissRequest = { unitDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("g") },
                            onClick = {
                                portionUnit = "g"
                                unitDropdownExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("ml") },
                            onClick = {
                                portionUnit = "ml"
                                unitDropdownExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("porção") },
                            onClick = {
                                portionUnit = "porção"
                                unitDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Calorias
            Text(
                text = "Calorias",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF334155)
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = if (caloriesText.endsWith(" kcal")) caloriesText else "$caloriesText kcal",
                onValueChange = { input ->
                    caloriesText = input.replace(" kcal", "").filter { it.isDigit() }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color(0xFFF1F5F9),
                    focusedContainerColor = Color(0xFFF1F5F9)
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Macros Header
            Text(
                text = "Macros",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF334155)
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Carboidrato
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Carboidrato", fontSize = 12.sp, color = Color(0xFF64748B))
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = carbsText,
                        onValueChange = { carbsText = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Proteina
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Proteina", fontSize = 12.sp, color = Color(0xFF64748B))
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = proteinText,
                        onValueChange = { proteinText = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Gordura
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Gordura", fontSize = 12.sp, color = Color(0xFF64748B))
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = fatText,
                        onValueChange = { fatText = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Reminder Switch
            Text(
                text = "Reminder",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF334155)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Lembrete para o Diário",
                    fontSize = 14.sp,
                    color = Color(0xFF64748B)
                )
                Switch(
                    checked = reminderEnabled,
                    onCheckedChange = { reminderEnabled = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF22C55E)
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons: Cancelar & Salvar
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color(0xFFF1F5F9),
                        contentColor = Color(0xFF475569)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text(text = "Cancelar", fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = {
                        val portionVal = portionValueText.toDoubleOrNull() ?: 150.0
                        val calVal = caloriesText.toIntOrNull() ?: 240
                        val carbVal = carbsText.toDoubleOrNull() ?: 0.0
                        val protVal = proteinText.toDoubleOrNull() ?: 0.0
                        val fatVal = fatText.toDoubleOrNull() ?: 0.0

                        if (existingMeal != null) {
                            val updated = existingMeal.copy(
                                title = title.ifBlank { existingMeal.title },
                                category = selectedCategory,
                                portionValue = portionVal,
                                portionUnit = portionUnit,
                                calories = calVal,
                                carbs = carbVal,
                                protein = protVal,
                                fat = fatVal,
                                notes = notes,
                                reminderEnabled = reminderEnabled
                            )
                            mealViewModel.updateMeal(updated)
                        } else {
                            mealViewModel.addMeal(
                                title = title,
                                category = selectedCategory,
                                portionValue = portionVal,
                                portionUnit = portionUnit,
                                calories = calVal,
                                carbs = carbVal,
                                protein = protVal,
                                fat = fatVal,
                                reminderEnabled = reminderEnabled,
                                notes = notes
                            )
                        }
                        onSaveSuccess()
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D52CE)),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text(text = "Salvar", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
