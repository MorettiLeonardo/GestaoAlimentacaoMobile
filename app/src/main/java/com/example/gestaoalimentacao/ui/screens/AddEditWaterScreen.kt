package com.example.gestaoalimentacao.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gestaoalimentacao.ui.viewmodel.WaterViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AddEditWaterScreen(
    waterViewModel: WaterViewModel,
    waterId: String? = null,
    voltar: () -> Unit
) {
    LaunchedEffect(waterId) {
        if (waterId != null) waterViewModel.buscarPorId(waterId)
        else waterViewModel.novoRegistro()
    }
    LaunchedEffect(waterViewModel.salvo) {
        if (waterViewModel.salvo) {
            waterViewModel.consumirSalvo()
            voltar()
        }
    }

    val existente = waterViewModel.registro

    // Os campos são preenchidos de novo quando o registro chega do banco.
    var quantidade by remember(existente) { mutableStateOf(existente?.amountMl?.toString() ?: "") }
    var horario by remember(existente) {
        mutableStateOf(
            existente?.timeText ?: SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        )
    }
    var anotacao by remember(existente) { mutableStateOf(existente?.note ?: "") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = if (waterId == null) "Novo registro de água" else "Alterar registro de água",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E293B)
        )

        OutlinedTextField(
            value = quantidade,
            onValueChange = { quantidade = it.filter { c -> c.isDigit() }.take(4) },
            label = { Text("Quantidade (ml)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = horario,
            onValueChange = { horario = it.filter { c -> c.isDigit() || c == ':' }.take(5) },
            label = { Text("Horário (HH:mm)") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = anotacao,
            onValueChange = { if (it.length <= 100) anotacao = it },
            label = { Text("Anotação (opcional)") },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        waterViewModel.mensagem?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp) }
        if (waterViewModel.carregando) Text("Aguarde...")

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = voltar,
                enabled = !waterViewModel.carregando,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Text(text = "Cancelar", fontWeight = FontWeight.SemiBold)
            }
            Button(
                onClick = { waterViewModel.salvar(quantidade, horario, anotacao) },
                enabled = !waterViewModel.carregando && !waterViewModel.salvo,
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