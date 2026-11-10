package com.example.gestaoalimentacao.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class WaterIntake(
    val id: String = UUID.randomUUID().toString(),
    val amountMl: Int,
    val timeText: String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
    val note: String = ""
)