package com.example.gestaoalimentacao.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gestaoalimentacao.data.model.WaterIntake
import com.example.gestaoalimentacao.ui.viewmodel.WaterViewModel

@Composable
fun WaterScreen(
    waterViewModel: WaterViewModel,
    onAddClick: () -> Unit,
    onEditClick: (String) -> Unit
) {
    LaunchedEffect(Unit) { waterViewModel.listar() }

    val registros = waterViewModel.registros
    var excluirId by rememberSaveable { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F9FC))
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "Água",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B),
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = "Acompanhe quanto você bebeu",
                fontSize = 13.sp,
                color = Color(0xFF64748B)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE0E7FF))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Total registrado", fontSize = 13.sp, color = Color(0xFF475569))
                    Text(
                        text = "${waterViewModel.totalMl} ml",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0D52CE)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                item {
                    waterViewModel.mensagem?.let { Text(it, color = Color(0xFF64748B)) }
                    if (registros.isEmpty()) Text("Nenhum registro de água ainda", color = Color(0xFF64748B))
                }
                items(registros, key = { it.id }) { registro ->
                    WaterCardItem(
                        registro = registro,
                        enabled = !waterViewModel.carregando,
                        onEditClick = { onEditClick(registro.id) },
                        onDeleteClick = { excluirId = registro.id }
                    )
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = onAddClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 8.dp),
            shape = RoundedCornerShape(24.dp),
            containerColor = Color(0xFF0D52CE),
            contentColor = Color.White
        ) {
            Text(
                text = "Novo registro",
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }

    val selecionado = registros.firstOrNull { it.id == excluirId }
    if (selecionado != null) {
        AlertDialog(
            onDismissRequest = { excluirId = null },
            title = { Text("Excluir registro?") },
            text = { Text("Excluir o registro de ${selecionado.amountMl} ml? Esta ação não pode ser desfeita.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        excluirId = null
                        waterViewModel.excluir(selecionado.id)
                    },
                    enabled = !waterViewModel.carregando
                ) { Text("Excluir") }
            },
            dismissButton = { TextButton(onClick = { excluirId = null }) { Text("Cancelar") } }
        )
    }
}

@Composable
fun WaterCardItem(
    registro: WaterIntake,
    enabled: Boolean,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${registro.amountMl} ml",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Text(
                    text = registro.timeText,
                    fontSize = 13.sp,
                    color = Color(0xFF64748B)
                )
            }

            if (registro.note.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = registro.note,
                    fontSize = 13.sp,
                    color = Color(0xFF64748B)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onEditClick, enabled = enabled) { Text("Alterar") }
                TextButton(onClick = onDeleteClick, enabled = enabled) {
                    Text("Excluir", color = Color(0xFFEF4444))
                }
            }
        }
    }
}