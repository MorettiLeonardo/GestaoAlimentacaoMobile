package com.example.gestaoalimentacao.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class WeightEntry(
    val id: String = UUID.randomUUID().toString(),
    val weightKg: Double,
    val dateText: String = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()),
    val photoPath: String = ""
)
